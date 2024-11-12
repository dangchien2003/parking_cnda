package com.parking.vault_service.repository;

import com.parking.vault_service.entity.Deposit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositRepository extends JpaRepository<Deposit, String> {
    Page<Deposit> findAllByActionAtIsNullAndCancelAtIsNull(Pageable pageable);

    Page<Deposit> findAllByActionAtIsNotNull(Pageable pageable);

    Page<Deposit> findAllByCancelAtIsNotNull(Pageable pageable);

    Optional<Deposit> findByIdAndOwnerId(String id, String owner);

    Page<Deposit> findAllByOwnerIdAndActionAtIsNotNull(String ownerId, Pageable pageable);

    Page<Deposit> findAllByOwnerIdAndActionAtIsNull(String ownerId, Pageable pageable);

    List<Deposit> findByIdInAndActionAtIsNull(List<String> idDeposits);

    int countByOwnerIdAndCancelAtIsNullAndActionAtIsNull(String owner);

    Page<Deposit> findAllByOwnerIdAndCancelAtIsNotNull(String owner, Pageable pageable);

    List<Deposit> findByIdInAndActionAtIsNullAndCancelAtIsNull(List<String> depositsId);

    @Query("SELECT SUM(amount) AS totalAmount FROM Deposit WHERE ownerId = :ownerId")
    Integer calculateTotalAmountWhereOwnerId(@Param("ownerId") String ownerId);

  @Query("SELECT SUM(amount) AS totalAmount FROM Deposit WHERE actionAt IS NULL AND ownerId = :ownerId")
    Integer calculateTotalWaitApproveWhereOwnerId(@Param("ownerId") String ownerId);

    List<Deposit> findAllByOwnerId(String owner, Pageable pageable);

    List<Deposit> findAllByCreateAtIsBetweenAndOwnerId(long start, long end, String ownerId, Pageable pageable);
    List<Deposit> findAllByCreateAtIsBetweenAndOwnerIdAndActionAtIsNull(long start, long end, String ownerId, Pageable pageable);
    List<Deposit> findAllByCreateAtIsBetweenAndOwnerIdAndActionAtIsNotNull(long start, long end, String ownerId, Pageable pageable);

}
