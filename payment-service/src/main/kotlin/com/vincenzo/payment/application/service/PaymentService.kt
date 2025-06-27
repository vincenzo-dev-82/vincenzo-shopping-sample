package com.vincenzo.payment.application.service

import com.vincenzo.payment.application.port.`in`.*
import com.vincenzo.payment.application.port.out.*
import com.vincenzo.payment.domain.event.*
import com.vincenzo.payment.domain.model.*
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
        try {
            // 1. 결제 정보 생성
            val paymentMethods = request.paymentMethods.map { methodRequest ->
                PaymentMethod(
                    methodType = methodRequest.methodType,
                    amount = methodRequest.amount,
                    additionalInfo = methodRequest.additionalInfo
                )
            }
            
            val payment = Payment(
                paymentKey = request.paymentKey,
                orderId = request.orderId,
                totalAmount = request.totalAmount,
                paymentType = request.paymentType,
                paymentMethods = paymentMethods
            )
            
            // 2. 결제 저장 (대기 상태)
            val savedPayment = paymentRepository.save(payment)
            
            // 3. 각 결제 방법별 처리
            val processedMethods = mutableListOf<PaymentMethod>()
            val completedMethods = mutableListOf<PaymentMethod>()
            
            for (method in savedPayment.paymentMethods) {
                val processor = findProcessor(method.methodType)
                if (processor == null) {
                    // 지원하지 않는 결제 방법 시 보상 트랜잭션 수행
                    rollbackCompletedMethods(completedMethods, request.memberId)
                    
                    val failedPayment = savedPayment.fail()
                    paymentRepository.save(failedPayment)
                    
                    publishFailedEvent(failedPayment, "지원하지 않는 결제 방법: ${method.methodType}")
                    
                    return ProcessPaymentResult(
                        success = false,
                        message = "지원하지 않는 결제 방법입니다: ${method.methodType}"
                    )
                }
                
                val result = processor.process(method, request.memberId)
                if (!result.success) {
                    // 결제 실패 시 보상 트랜잭션 수행
                    rollbackCompletedMethods(completedMethods, request.memberId)
                    
                    val failedPayment = savedPayment.fail()
                    paymentRepository.save(failedPayment)
                    
                    publishFailedEvent(failedPayment, result.message)
                    
                    return ProcessPaymentResult(
                        success = false,
                        message = result.message
                    )
                }
                
                val completedMethod = method.complete(result.externalTransactionId)
                processedMethods.add(completedMethod)
                completedMethods.add(completedMethod)
            }
            
            // 4. 모든 결제 방법 성공 시 결제 완료
            val completedPayment = savedPayment.complete()
            val finalPayment = paymentRepository.save(completedPayment)
            
            // 5. 성공 이벤트 발행
            val event = PaymentCompletedEvent(
                paymentKey = finalPayment.paymentKey,
                orderId = finalPayment.orderId,
                amount = finalPayment.totalAmount,
                memberId = request.memberId
            )
            eventPublisher.publishPaymentEvent(event)
            
            return ProcessPaymentResult(
                success = true,
                message = "결제가 성공적으로 완료되었습니다.",
                payment = finalPayment
            )
            
        } catch (e: Exception) {
            return ProcessPaymentResult(
                success = false,
                message = e.message ?: "결제 처리 중 오류가 발생했습니다."
            )
        }
    }
    
    override suspend fun cancelPayment(paymentKey: String, reason: String?): Payment {
        val payment = paymentRepository.findByPaymentKey(paymentKey)
            ?: throw IllegalArgumentException("존재하지 않는 결제입니다.")
        
        if (payment.status == PaymentStatus.COMPLETED) {
            // 완료된 결제의 경우 보상 트랜잭션 수행
            val completedMethods = payment.paymentMethods.filter { it.status == PaymentMethodStatus.COMPLETED }
            // memberId를 얻기 위해 추가 로직이 필요할 수 있음 (예: 주문 서비스에서 조회)
            // 여기서는 간단히 0L로 처리
            rollbackCompletedMethods(completedMethods, 0L)
        }
        
        val cancelledPayment = payment.cancel()
        val savedPayment = paymentRepository.save(cancelledPayment)
        
        val event = PaymentCancelledEvent(
            paymentKey = savedPayment.paymentKey,
            orderId = savedPayment.orderId,
            amount = savedPayment.totalAmount,
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
     * 결제 방법에 따른 처리기 찾기
     */
    private fun findProcessor(methodType: PaymentMethodType): PaymentProcessor? {
        return paymentProcessors.find { it.supports(methodType) }
    }
    
    /**
     * 완료된 결제 방법들에 대한 보상 트랜잭션 수행
     */
    private suspend fun rollbackCompletedMethods(completedMethods: List<PaymentMethod>, memberId: Long) {
        for (method in completedMethods) {
            try {
                val processor = findProcessor(method.methodType)
                processor?.cancel(method, memberId)
            } catch (e: Exception) {
                // 보상 트랜잭션 실패 로깅 (실제 서비스에서는 알림 및 수동 처리 필요)
                println("보상 트랜잭션 실패: ${method.methodType}, ${e.message}")
            }
        }
    }
    
    /**
     * 결제 실패 이벤트 발행
     */
    private fun publishFailedEvent(payment: Payment, reason: String) {
        val event = PaymentFailedEvent(
            paymentKey = payment.paymentKey,
            orderId = payment.orderId,
            amount = payment.totalAmount,
            reason = reason
        )
        eventPublisher.publishPaymentEvent(event)
    }
}
