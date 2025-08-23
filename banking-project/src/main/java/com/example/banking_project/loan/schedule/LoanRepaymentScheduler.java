package com.example.banking_project.loan.schedule;

import com.example.banking_project.account.model.Account;
import com.example.banking_project.account.service.AccountService;
import com.example.banking_project.loan.model.Loan;
import com.example.banking_project.loan.model.LoanStatus;
import com.example.banking_project.loan.repository.LoanRepository;
import com.example.banking_project.loan.view.LoanView;
import com.example.banking_project.transaction.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanRepaymentScheduler {

    private final LoanRepository loanRepository;
    private final AccountService accountService;
    private final TransactionService transactionService;

    // Всеки ден в 03:00
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void collectMonthlyInstallments() {
        LocalDate today = LocalDate.now();
        List<LoanView> dueLoans = loanRepository.findDueLoanViews(today);

        for (LoanView view : dueLoans) {
            try {
                processOneLoan(view);
            } catch (Exception e) {
                log.error("Repayment failed for loan {}: {}", view.getId(), e.getMessage(), e);
            }
        }
    }

    private void processOneLoan(LoanView view) {
        UUID loanId = view.getId();
        UUID repaymentAccountId = view.getRepaymentAccountId();

        Account account = accountService.getAccountById(repaymentAccountId);
        BigDecimal balance = account.getBalance();
        BigDecimal monthlyPayment = view.getMonthlyPayment();
        BigDecimal remainingAmount = view.getRemainingAmount();

        BigDecimal amountToDebit = monthlyPayment.min(remainingAmount);
        if (amountToDebit.compareTo(BigDecimal.ZERO) <= 0) {
            advanceOrClose(loanId, BigDecimal.ZERO);
            return;
        }

        if (balance.compareTo(amountToDebit) >= 0) {
            // 1) Записваме разход + дебит на сметката (вътре в recordExpense)
            String repaymentIban = view.getRepaymentIban() != null ? view.getRepaymentIban() : account.getIban();
            transactionService.recordExpense(
                    view.getUserId(),
                    repaymentIban,
                    amountToDebit,
                    "Loan monthly installment for loan " + loanId
            );

            // 2) Обновяваме заема
            advanceOrClose(loanId, amountToDebit);

        } else {
            // Недостатъчни средства → пропуск и изместване на падежа
            Loan loan = loanRepository.findById(loanId).orElseThrow();
            loan.setMissedPayments(loan.getMissedPayments() + 1);
            loan.setNextDateOfPayment(loan.getNextDateOfPayment().plusMonths(1));
            loanRepository.save(loan);

            log.warn("Insufficient funds for loan {} (acc {}): needed {}, had {}",
                    loanId, account.getIban(), amountToDebit, balance);
        }
    }

    private void advanceOrClose(UUID loanId, BigDecimal paidNow) {
        Loan loan = loanRepository.findById(loanId).orElseThrow();

        if (paidNow.compareTo(BigDecimal.ZERO) > 0) {
            loan.setRemainingAmount(loan.getRemainingAmount().subtract(paidNow));
        }

        if (loan.getRemainingAmount().compareTo(BigDecimal.ZERO) <= 0) {
            loan.setRemainingAmount(BigDecimal.ZERO);
            loan.setLoanStatus(LoanStatus.PAID_OFF);
        } else {
            loan.setNextDateOfPayment(loan.getNextDateOfPayment().plusMonths(1));
        }

        loanRepository.save(loan);
    }
}
