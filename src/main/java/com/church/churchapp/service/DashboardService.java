package com.church.churchapp.service;

import com.church.churchapp.repository.MemberRepository;
import com.church.churchapp.repository.FamilyRepository;
import com.church.churchapp.repository.EventRepository;
import com.church.churchapp.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class DashboardService {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private FamilyRepository familyRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    public Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();

        long totalMembers = memberRepository.count();
        long totalFamilies = familyRepository.count();
        long totalEvents = eventRepository.count();

        // Calculate total transactions
        long totalTransactions = transactionRepository.count();

        stats.put("totalMembers", totalMembers);
        stats.put("totalFamilies", totalFamilies);
        stats.put("totalEvents", totalEvents);
        stats.put("totalTransactions", totalTransactions);

        return stats;
    }
}
