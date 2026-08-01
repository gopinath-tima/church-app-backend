package com.church.churchapp.dto;

import lombok.Data;

@Data
public class AccountingSummary {
    private Double totalIncome;
    private Double totalExpense;
    private Double netBalance;
    private Double totalInKindValue;
}
