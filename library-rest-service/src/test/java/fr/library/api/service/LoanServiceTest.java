package fr.library.api.service;

import fr.library.api.client.CatalogClient;
import fr.library.api.dto.BookDto;
import fr.library.api.entity.Loan;
import fr.library.api.entity.LoanStatus;
import fr.library.api.entity.Member;
import fr.library.api.exception.BusinessRuleException;
import fr.library.api.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanServiceTest {

    @Mock
    private LoanRepository loanRepository;
    @Mock
    private MemberService memberService;
    @Mock
    private CatalogClient catalogClient;

    private LoanService loanService;
    private final Member alice = new Member("Alice", "alice@example.com");

    @BeforeEach
    void setUp() {
        loanService = new LoanService(loanRepository, memberService, catalogClient, 3, 21);
    }

    @Test
    void borrowReservesCopyAndSavesLoan() {
        when(memberService.getMember(1L)).thenReturn(alice);
        when(loanRepository.countByMemberIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(0L);
        when(catalogClient.reserveCopy(10L)).thenReturn(new BookDto(10L, "isbn", "Germinal", "Zola", 1, 0));
        when(loanRepository.saveAndFlush(any(Loan.class))).thenAnswer(inv -> inv.getArgument(0));

        Loan loan = loanService.borrow(1L, 10L);

        assertEquals("Germinal", loan.getBookTitle());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertEquals(LocalDate.now().plusDays(21), loan.getDueDate());
    }

    @Test
    void borrowRefusedWhenTooManyActiveLoans() {
        when(memberService.getMember(1L)).thenReturn(alice);
        when(loanRepository.countByMemberIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(3L);

        assertThrows(BusinessRuleException.class, () -> loanService.borrow(1L, 10L));
        verify(catalogClient, never()).reserveCopy(any());
    }

    @Test
    void borrowReleasesCopyWhenLocalSaveFails() {
        when(memberService.getMember(1L)).thenReturn(alice);
        when(loanRepository.countByMemberIdAndStatus(1L, LoanStatus.ACTIVE)).thenReturn(0L);
        when(catalogClient.reserveCopy(10L)).thenReturn(new BookDto(10L, "isbn", "Germinal", "Zola", 1, 0));
        when(loanRepository.saveAndFlush(any(Loan.class))).thenThrow(new RuntimeException("DB down"));

        assertThrows(RuntimeException.class, () -> loanService.borrow(1L, 10L));
        verify(catalogClient).releaseCopy(10L);
    }

    @Test
    void returnBookReleasesCopyAndMarksLoanReturned() {
        Loan loan = new Loan(alice, 10L, "Germinal", LocalDate.now(), LocalDate.now().plusDays(21));
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));

        Loan result = loanService.returnBook(5L);

        assertEquals(LoanStatus.RETURNED, result.getStatus());
        verify(catalogClient).releaseCopy(10L);
    }

    @Test
    void returnBookRefusedWhenAlreadyReturned() {
        Loan loan = new Loan(alice, 10L, "Germinal", LocalDate.now(), LocalDate.now().plusDays(21));
        loan.markReturned(LocalDate.now());
        when(loanRepository.findById(5L)).thenReturn(Optional.of(loan));

        assertThrows(BusinessRuleException.class, () -> loanService.returnBook(5L));
    }
}
