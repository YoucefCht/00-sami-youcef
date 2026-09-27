package fr.library.api.repository;

import fr.library.api.entity.Loan;
import fr.library.api.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByMemberId(Long memberId);

    List<Loan> findByStatus(LoanStatus status);

    long countByMemberIdAndStatus(Long memberId, LoanStatus status);
}
