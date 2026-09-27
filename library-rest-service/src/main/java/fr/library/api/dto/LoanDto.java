package fr.library.api.dto;

import fr.library.api.entity.Loan;
import fr.library.api.entity.LoanStatus;

import java.time.LocalDate;

public record LoanDto(Long id, Long memberId, String memberName, Long bookId, String bookTitle,
                      LocalDate loanDate, LocalDate dueDate, LocalDate returnDate, LoanStatus status) {

    public static LoanDto from(Loan loan) {
        return new LoanDto(loan.getId(), loan.getMember().getId(), loan.getMember().getName(),
                loan.getBookId(), loan.getBookTitle(), loan.getLoanDate(), loan.getDueDate(),
                loan.getReturnDate(), loan.getStatus());
    }
}
