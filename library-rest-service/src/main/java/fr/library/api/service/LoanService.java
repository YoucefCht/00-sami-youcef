package fr.library.api.service;

import fr.library.api.client.CatalogClient;
import fr.library.api.dto.BookDto;
import fr.library.api.entity.Loan;
import fr.library.api.entity.LoanStatus;
import fr.library.api.entity.Member;
import fr.library.api.exception.BusinessRuleException;
import fr.library.api.exception.ResourceNotFoundException;
import fr.library.api.repository.LoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Logique métier des emprunts. Orchestration entre la base locale (adhérents, emprunts)
 * et le serveur RPC (stock d'exemplaires).
 */
@Service
public class LoanService {

    private static final Logger log = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final MemberService memberService;
    private final CatalogClient catalogClient;
    private final int maxActiveLoans;
    private final int loanDurationDays;

    @Autowired
    public LoanService(LoanRepository loanRepository,
                       MemberService memberService,
                       CatalogClient catalogClient,
                       @Value("${library.max-active-loans:3}") int maxActiveLoans,
                       @Value("${library.loan-duration-days:21}") int loanDurationDays) {
        this.loanRepository = loanRepository;
        this.memberService = memberService;
        this.catalogClient = catalogClient;
        this.maxActiveLoans = maxActiveLoans;
        this.loanDurationDays = loanDurationDays;
    }

    @Transactional(readOnly = true)
    public List<Loan> listLoans(Long memberId, LoanStatus status) {
        if (memberId != null) {
            memberService.getMember(memberId); // 404 si l'adhérent n'existe pas
            return loanRepository.findByMemberId(memberId).stream()
                    .filter(loan -> status == null || loan.getStatus() == status)
                    .toList();
        }
        return status != null ? loanRepository.findByStatus(status) : loanRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Loan getLoan(Long id) {
        return loanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Emprunt introuvable : id=" + id));
    }

    @Transactional
    public Loan borrow(Long memberId, Long bookId) {
        Member member = memberService.getMember(memberId);

        long active = loanRepository.countByMemberIdAndStatus(memberId, LoanStatus.ACTIVE);
        if (active >= maxActiveLoans) {
            throw new BusinessRuleException("L'adhérent " + member.getName()
                    + " a déjà " + active + " emprunts en cours (maximum " + maxActiveLoans + ")");
        }

        // Appel RPC : décrémente le stock (lève une exception si aucun exemplaire n'est disponible)
        BookDto book = catalogClient.reserveCopy(bookId);

        try {
            LocalDate today = LocalDate.now();
            Loan loan = loanRepository.saveAndFlush(
                    new Loan(member, bookId, book.title(), today, today.plusDays(loanDurationDays)));
            log.info("Emprunt créé : id={}, adhérent={}, livre='{}'", loan.getId(), memberId, book.title());
            return loan;
        } catch (RuntimeException e) {
            // Compensation : l'enregistrement local a échoué, on rend l'exemplaire au catalogue
            log.error("Échec de l'enregistrement de l'emprunt, annulation de la réservation du livre {}", bookId, e);
            catalogClient.releaseCopy(bookId);
            throw e;
        }
    }

    @Transactional
    public Loan returnBook(Long loanId) {
        Loan loan = getLoan(loanId);
        if (loan.getStatus() == LoanStatus.RETURNED) {
            throw new BusinessRuleException("L'emprunt " + loanId + " a déjà été rendu");
        }
        catalogClient.releaseCopy(loan.getBookId());
        loan.markReturned(LocalDate.now());
        log.info("Emprunt rendu : id={}, livre='{}'", loanId, loan.getBookTitle());
        return loan;
    }
}
