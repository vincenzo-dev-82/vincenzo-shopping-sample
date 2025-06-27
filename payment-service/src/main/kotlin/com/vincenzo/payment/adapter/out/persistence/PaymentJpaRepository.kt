package com.vincenzo.payment.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface PaymentJpaRepository : JpaRepository<PaymentEntity, Long> {
    
    @Query("SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.paymentMethods WHERE p.paymentKey = :paymentKey")
    fun findByPaymentKeyWithMethods(@Param("paymentKey") paymentKey: String): PaymentEntity?
    
    @Query("SELECT p FROM PaymentEntity p LEFT JOIN FETCH p.paymentMethods WHERE p.orderId = :orderId")
    fun findByOrderIdWithMethods(@Param("orderId") orderId: Long): PaymentEntity?
}
