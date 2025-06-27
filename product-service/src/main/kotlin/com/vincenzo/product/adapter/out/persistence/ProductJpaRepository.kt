package com.vincenzo.product.adapter.out.persistence

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ProductJpaRepository : JpaRepository<ProductEntity, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id = :id")
    fun findByIdWithLock(@Param("id") id: Long): ProductEntity?
    
    @Modifying
    @Query("UPDATE ProductEntity p SET p.stockQuantity = :stock, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    fun updateStockQuantity(@Param("id") id: Long, @Param("stock") stock: Int): Int
    
    fun findByIdIn(ids: List<Long>): List<ProductEntity>
}
