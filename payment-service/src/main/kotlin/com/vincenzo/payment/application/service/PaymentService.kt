package com.vincenzo.payment.application.service

import com.vincenzo.payment.application.port.`in`.*
import com.vincenzo.payment.application.port.out.*
import com.vincenzo.payment.domain.event.*
import com.vincenzo.payment.domain.model.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentProcessors: List<PaymentProcessor>,
    private val eventPublisher: EventPublisher
) : PaymentUseCase {
    
    override suspend fun processPayment(request: ProcessPaymentRequest): ProcessPaymentResult {
        return try {
            // 1. 결제 방법 생성
            val paymentMethods = request.paymentMethods.map { methodRequest ->
                PaymentMethod(
                    methodType = methodRequest.methodType,
                    amount = methodRequest.amount,
                    additionalInfo = methodRequest.additionalInfo
                )
            }
            
            // 2. 결제 객체 생성 (도메인 규칙 검증 포함)
            val payment = Payment(
                paymentKey = request.paymentKey,
                orderId = request.orderId,
                totalAmount = request.totalAmount,
                paymentType = request.paymentType,
                paymentMethods = paymentMethods
            )
            
            // 3. 결제 저장
            val savedPayment = paymentRepository.save(payment)
            
            // 4. 각 결제 방법별로 병렬 처리
            val processResults = coroutineScope {
                paymentMethods.map { paymentMethod ->
                    async {
                        val processor = findProcessor(paymentMethod.methodType)
                        processor.process(paymentMethod, request.memberId)
                    }
                }.awaitAll()
            }
            
            // 5. 결과 검증
            val failedResults = processResults.filter { !it.success }
            
            if (failedResults.isNotEmpty()) {
                // 실패한 결제가 있으면 성공한 결제들을 롤백
                rollbackSuccessfulPayments(paymentMethods, processResults, request.memberId)
                
                val failedPayment = savedPayment.fail()
                paymentRepository.save(failedPayment)
                
                val failureReason = failedResults.joinToString(", ") { it.message }
                val event = PaymentFailedEvent(
                    paymentKey = request.paymentKey,
                    orderId = request.orderId,
                    amount = request.totalAmount,
                    reason = failureReason
                )
                eventPublisher.publishPaymentEvent(event)
                
                ProcessPaymentResult(
                    success = false,
                    message = "결제 처리 실패: $failureReason"
                )
            } else {
                // 모든 결제가 성공
                val completedMethods = paymentMethods.zip(processResults) { method, result ->
                    method.complete(result.externalTransactionId)
                }
                
                val completedPayment = savedPayment.copy(
                    paymentMethods = completedMethods
                ).complete()
                
                val finalPayment = paymentRepository.save(completedPayment)
                
                val event = PaymentCompletedEvent(
                    paymentKey = request.paymentKey,
                    orderId = request.orderId,
                    amount = request.totalAmount,
                    memberId = request.memberId
                )
                eventPublisher.publishPaymentEvent(event)
                
                ProcessPaymentResult(
                    success = true,
                    message = "결제가 성공적으로 완료되었습니다.",
                    payment = finalPayment
                )
            }
        } catch (e: Exception) {
            ProcessPaymentResult(
                success = false,
                message = e.message ?: "결제 처리 중 오류가 발생했습니다."
            )
        }
    }
    
    override suspend fun cancelPayment(paymentKey: String, reason: String?): Payment {
        val payment = paymentRepository.findByPaymentKey(paymentKey)
            ?: throw IllegalArgumentException("존재하지 않는 결제입니다.")
        
        // 각 결제 방법별로 취소 처리
        coroutineScope {
            payment.paymentMethods.map { paymentMethod ->
                async {
                    if (paymentMethod.status == PaymentMethodStatus.COMPLETED) {
                        val processor = findProcessor(paymentMethod.methodType)
                        // memberId는 실제로는 payment에서 가져와야 하지만, 현재 모델에 없으므로 0으로 임시 처리
                        processor.cancel(paymentMethod, 0L)
                    }
                }
            }.awaitAll()
        }
        
        val cancelledPayment = payment.cancel()
        val savedPayment = paymentRepository.save(cancelledPayment)
        
        val event = PaymentCancelledEvent(
            paymentKey = paymentKey,
            orderId = payment.orderId,
            amount = payment.totalAmount,
            reason = reason
        )
        eventPublisher.publishPaymentEvent(event)
        
        return savedPayment
    }
    
    @Transactional(readOnly = true)
    override fun getPaymentStatus(paymentKey: String): Payment? {
        return paymentRepository.findByPaymentKey(paymentKey)
    }
    
    /**
     * 결제 방법에 맞는 프로세서 찾기
     */
    private fun findProcessor(methodType: PaymentMethodType): PaymentProcessor {
        return paymentProcessors.find { it.supports(methodType) }
            ?: throw IllegalArgumentException("지원하지 않는 결제 방법입니다: $methodType")
    }
    
    /**
     * 성공한 결제들을 롤백
     */
    private suspend fun rollbackSuccessfulPayments(
        paymentMethods: List<PaymentMethod>,
        processResults: List<PaymentProcessResult>,
        memberId: Long
    ) {
        coroutineScope {
            paymentMethods.zip(processResults) { method, result ->
                async {
                    if (result.success) {
                        try {
                            val processor = findProcessor(method.methodType)
                            processor.cancel(method, memberId)
                        } catch (e: Exception) {
                            // 롤백 실패는 로그만 남기고 계속 진행
                        }
                    }
                }
            }.awaitAll()
        }
    }
}
