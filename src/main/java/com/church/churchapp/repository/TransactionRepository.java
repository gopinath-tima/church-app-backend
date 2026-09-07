package com.church.churchapp.repository;

import com.church.churchapp.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByOrderByDateDesc();
    List<Transaction> findByMemberId(Long memberId);
    List<Transaction> findByTypeAndForMonthAndForYear(String type, Integer forMonth, Integer forYear);
    Optional<Transaction> findByTypeAndMemberIdAndForMonthAndForYear(String type, Long memberId, Integer forMonth, Integer forYear);
    List<Transaction> findByDate(String date);
}
