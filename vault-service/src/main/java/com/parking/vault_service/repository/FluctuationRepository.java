package com.parking.vault_service.repository;

import com.parking.vault_service.entity.Fluctuation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FluctuationRepository extends JpaRepository<Fluctuation, String> {
    Page<Fluctuation> findAllByReason(String reason, Pageable pageable);

    Page<Fluctuation> findAllByReasonAndOwnerId(String reason, String owner, Pageable pageable);

    Page<Fluctuation> findAllByReasonNotIn(List<String> reasons, Pageable pageable);

    Page<Fluctuation> findAllByReasonNotInAndOwnerId(List<String> reasons, String owner, Pageable pageable);

    Page<Fluctuation> findAllByOwnerId(String ownerId,  Pageable pageable);

    List<Fluctuation> findByTransactionAndOwnerIdAndCreateAtIsBetween(String transaction, String ownerId, long start, long end);
}
