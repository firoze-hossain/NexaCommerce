// HotDealRepository.java
package com.roze.nexacommerce.hotdeal.repository;

import com.roze.nexacommerce.hotdeal.entity.HotDeal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HotDealRepository extends JpaRepository<HotDeal, Long> {
    
    List<HotDeal> findByIsActiveTrue();
    
    @Query("SELECT hd FROM HotDeal hd WHERE hd.isActive = true AND hd.startDate <= :now AND hd.endDate >= :now")
    List<HotDeal> findActiveDeals(@Param("now") LocalDateTime now);
    
    Page<HotDeal> findByIsActiveTrue(Pageable pageable);
    
    List<HotDeal> findByProductId(Long productId);
    
    Optional<HotDeal> findByProductIdAndIsActiveTrue(Long productId);
    
    @Query("SELECT hd FROM HotDeal hd WHERE hd.isActive = true AND hd.stockLimit IS NOT NULL AND hd.soldCount < hd.stockLimit")
    List<HotDeal> findLimitedStockDeals();
    
    boolean existsByProductIdAndIsActiveTrue(Long productId);
}