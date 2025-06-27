package com.vincenzo.order.adapter.out.external

import com.vincenzo.grpc.member.GetMemberRequest
import com.vincenzo.grpc.member.GetCashPointRequest
import com.vincenzo.grpc.member.MemberServiceGrpcKt
import com.vincenzo.order.application.port.out.MemberInfo
import com.vincenzo.order.application.port.out.MemberServiceClient
import net.devh.boot.grpc.client.inject.GrpcClient
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class MemberServiceClientAdapter : MemberServiceClient {
    
    @GrpcClient("member-service")
    private lateinit var memberServiceStub: MemberServiceGrpcKt.MemberServiceCoroutineStub
    
    override suspend fun getMember(memberId: Long): MemberInfo? {
        return try {
            val request = GetMemberRequest.newBuilder()
                .setMemberId(memberId)
                .build()
            
            val response = memberServiceStub.getMember(request)
            
            if (response.success && response.hasMember()) {
                val member = response.member
                MemberInfo(
                    id = member.id,
                    username = member.username,
                    email = member.email,
                    name = member.name,
                    cashpointBalance = BigDecimal.valueOf(member.cashpointBalance),
                    status = member.status
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    override suspend fun getCashpointBalance(memberId: Long): BigDecimal? {
        return try {
            val request = GetCashPointRequest.newBuilder()
                .setMemberId(memberId)
                .build()
            
            val response = memberServiceStub.getCashPoint(request)
            
            if (response.success) {
                BigDecimal.valueOf(response.balance)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
