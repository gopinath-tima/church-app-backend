package com.church.churchapp.controller;

import com.church.churchapp.dto.AccountingSummary;
import com.church.churchapp.entity.InKindDonation;
import com.church.churchapp.entity.Transaction;
import com.church.churchapp.service.AccountingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/accounting")
@PreAuthorize("hasAnyRole('ACCOUNTING', 'ADMIN', 'SUPER_ADMIN', 'SUPER_PLUS_ADMIN')")
public class AccountingController {

    @Autowired
    private AccountingService accountingService;

    @GetMapping("/transactions")
    public ResponseEntity<List<Transaction>> getAllTransactions() {
        return ResponseEntity.ok(accountingService.getAllTransactions());
    }

    @GetMapping("/transactions/member/{memberId}")
    public ResponseEntity<List<Transaction>> getMemberTransactions(@PathVariable Long memberId) {
        return ResponseEntity.ok(accountingService.getTransactionsByMemberId(memberId));
    }

    @PostMapping("/transactions")
    public ResponseEntity<Transaction> addTransaction(@RequestBody Transaction transaction) {
        return ResponseEntity.ok(accountingService.addTransaction(transaction));
    }

    // --- Subscription Endpoints ---

    /**
     * GET /api/accounting/subscriptions?month=5&year=2026
     * Returns all subscription transactions for a given month and year.
     */
    @GetMapping("/subscriptions")
    public ResponseEntity<List<Transaction>> getSubscriptionsByPeriod(
            @RequestParam Integer month,
            @RequestParam Integer year) {
        return ResponseEntity.ok(accountingService.getSubscriptionsByPeriod(month, year));
    }

    /**
     * GET /api/accounting/subscriptions/check?memberId=42&month=5&year=2026
     * Returns the subscription transaction if paid, or 404 if not paid.
     */
    @GetMapping("/subscriptions/check")
    public ResponseEntity<?> checkMemberSubscription(
            @RequestParam Long memberId,
            @RequestParam Integer month,
            @RequestParam Integer year) {
        Optional<Transaction> result = accountingService.getMemberSubscriptionForPeriod(memberId, month, year);
        if (result.isPresent()) {
            return ResponseEntity.ok(result.get());
        } else {
            return ResponseEntity.ok(Map.of("paid", false));
        }
    }

    // --- In-Kind Endpoints ---

    @GetMapping("/inkind")
    public ResponseEntity<List<InKindDonation>> getAllInKindDonations() {
        return ResponseEntity.ok(accountingService.getAllInKindDonations());
    }

    @GetMapping("/inkind/member/{memberId}")
    public ResponseEntity<List<InKindDonation>> getMemberInKind(@PathVariable Long memberId) {
        return ResponseEntity.ok(accountingService.getInKindDonationsByMemberId(memberId));
    }

    @PostMapping("/inkind")
    public ResponseEntity<InKindDonation> addInKindDonation(@RequestBody InKindDonation donation) {
        return ResponseEntity.ok(accountingService.addInKindDonation(donation));
    }

    @GetMapping("/summary")
    public ResponseEntity<AccountingSummary> getSummary() {
        return ResponseEntity.ok(accountingService.getSummary());
    }
}
