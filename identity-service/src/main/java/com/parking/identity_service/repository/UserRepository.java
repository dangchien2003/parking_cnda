package com.parking.identity_service.repository;

import com.parking.identity_service.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);

    List<User> findAllByUidIn(List<String> ids);

    List<User> findAllByIsBlocked(int block, Pageable pageable);

    List<User> findAllByEmailIn(List<String> emails);
}
