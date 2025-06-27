package com.vincenzo.payment.adapter.out.external

import com.vincenzo.grpc.member.DeductCashPointRequest
import com.vincenzo.grpc.member.MemberServiceGrpcKt
import com.vincenzo.grpc.member.RefundCashPointRequest
import com.vincenzo.payment.application.port.out.MemberServiceClient
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MemberServiceClientAdapter : MemberServiceClient {
    
    @GrpcClient("member-service")
    private lateinit var memberServiceStub: MemberServiceGrpcKt.MemberServiceCoroutineStub
    
    override suspend fun deductCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Boolean {
        return try {
            val request = DeductCashPointRequest.newBuilder()
                .setMemberId(memberId)
                .setAmount(amount.toDouble())
                .setTransactionId(transactionId)
                .build()
            
            val response = memberServiceStub.deductCashPoint(request)
            response.success
        } catch (e: Exception) {
            false
        }
    }
    
    override suspend fun refundCashpoint(memberId: Long, amount: BigDecimal, transactionId: String): Boolean {
        return try {
            val request = RefundCashPointRequest.newBuilder()
                .setMemberId(memberId)
                .setAmount(amount.toDouble())
                .setTransactionId(transactionId)
                .build()
            
            val response = memberServiceStub.refundCashPoint(request)
            response.success
        } catch (e: Exception) {
            false
        }
    }
}
