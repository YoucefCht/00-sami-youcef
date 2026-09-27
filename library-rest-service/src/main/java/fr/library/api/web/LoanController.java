package fr.library.api.web;

import fr.library.api.dto.CreateLoanRequest;
import fr.library.api.dto.LoanDto;
import fr.library.api.entity.LoanStatus;
import fr.library.api.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final LoanService loanService;

    @Autowired
    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    /** GET /api/loans?memberId=1&status=ACTIVE (filtres optionnels) */
    @GetMapping
    public List<LoanDto> list(@RequestParam(name = "memberId", required = false) Long memberId,
                              @RequestParam(name = "status", required = false) LoanStatus status) {
        return loanService.listLoans(memberId, status).stream().map(LoanDto::from).toList();
    }

    @GetMapping("/{id}")
    public LoanDto get(@PathVariable("id") Long id) {
        return LoanDto.from(loanService.getLoan(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LoanDto borrow(@Valid @RequestBody CreateLoanRequest request) {
        return LoanDto.from(loanService.borrow(request.memberId(), request.bookId()));
    }

    @PutMapping("/{id}/return")
    public LoanDto returnBook(@PathVariable("id") Long id) {
        return LoanDto.from(loanService.returnBook(id));
    }
}
