package com.church.churchapp.repository;

import com.church.churchapp.entity.InKindDonation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InKindDonationRepository extends JpaRepository<InKindDonation, Long> {
    List<InKindDonation> findAllByOrderByDateDesc();
    List<InKindDonation> findByMemberId(Long memberId);
}
