package com.church.churchapp.service;

import com.church.churchapp.dto.AccountingSummary;
import com.church.churchapp.entity.InKindDonation;
import com.church.churchapp.entity.Transaction;
import com.church.churchapp.entity.Member;
import com.church.churchapp.repository.InKindDonationRepository;
import com.church.churchapp.repository.TransactionRepository;
import com.church.churchapp.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class AccountingService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private InKindDonationRepository inKindDonationRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TwilioService twilioService;

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAllByOrderByDateDesc();
    }

    public List<Transaction> getTransactionsByMemberId(Long memberId) {
        return transactionRepository.findByMemberId(memberId);
    }

    public List<Transaction> getTransactionsByDate(String date) {
        return transactionRepository.findByDate(date);
    }

    public Transaction addTransaction(Transaction transaction) {
        // Generate Transaction ID if not provided. Use UUID to avoid collisions on parallel requests.
        if (transaction.getTransactionId() == null || transaction.getTransactionId().isEmpty()) {
            String shortId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            transaction.setTransactionId("TRX-" + System.currentTimeMillis() + "-" + shortId);
        }
        
        if (transaction.getDate() == null || transaction.getDate().isEmpty()) {
            transaction.setDate(LocalDate.now().toString());
        }
        
        Transaction savedTransaction = transactionRepository.save(transaction);

        // Send WhatsApp notification if the transaction is a subscription payment
        if ("Subscription".equalsIgnoreCase(savedTransaction.getType()) && savedTransaction.getMemberId() != null) {
            try {
                memberRepository.findById(savedTransaction.getMemberId()).ifPresent(member -> {
                    twilioService.sendSubscriptionNotification(member, savedTransaction);
                });
            } catch (Exception e) {
                // Log and swallow exception so it doesn't fail the primary database transaction
                org.slf4j.LoggerFactory.getLogger(AccountingService.class)
                        .error("Error launching Twilio WhatsApp subscription notification: {}", e.getMessage());
            }
        }

        return savedTransaction;
    }

    // --- Subscription-specific ---
    public List<Transaction> getSubscriptionsByPeriod(Integer forMonth, Integer forYear) {
        return transactionRepository.findByTypeAndForMonthAndForYear("Subscription", forMonth, forYear);
    }

    public java.util.Optional<Transaction> getMemberSubscriptionForPeriod(Long memberId, Integer forMonth, Integer forYear) {
        return transactionRepository.findByTypeAndMemberIdAndForMonthAndForYear("Subscription", memberId, forMonth, forYear);
    }

    public List<InKindDonation> getAllInKindDonations() {
        return inKindDonationRepository.findAllByOrderByDateDesc();
    }

    public List<InKindDonation> getInKindDonationsByMemberId(Long memberId) {
        return inKindDonationRepository.findByMemberId(memberId);
    }

    public InKindDonation addInKindDonation(InKindDonation donation) {
        if (donation.getDate() == null || donation.getDate().isEmpty()) {
            donation.setDate(LocalDate.now().toString());
        }
        return inKindDonationRepository.save(donation);
    }

    public AccountingSummary getSummary() {
        List<Transaction> transactions = transactionRepository.findAll();
        List<InKindDonation> inKindDonations = inKindDonationRepository.findAll();

        Double totalIncome = 0.0;
        Double totalExpense = 0.0;
        Double totalInKindValue = 0.0;

        for (Transaction t : transactions) {
            if ("Donation".equalsIgnoreCase(t.getType())) {
                continue;
            }
            if ("Expense".equalsIgnoreCase(t.getType())) {
                totalExpense += (t.getAmount() != null ? t.getAmount() : 0.0);
            } else {
                totalIncome += (t.getAmount() != null ? t.getAmount() : 0.0);
            }
        }

        for (InKindDonation d : inKindDonations) {
            if (d.getEstimatedValue() != null) {
                totalInKindValue += d.getEstimatedValue();
            }
        }

        AccountingSummary summary = new AccountingSummary();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setNetBalance(totalIncome - totalExpense);
        summary.setTotalInKindValue(totalInKindValue);

        return summary;
    }
}
