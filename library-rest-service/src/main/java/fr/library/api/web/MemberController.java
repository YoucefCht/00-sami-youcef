package fr.library.api.web;

import fr.library.api.dto.CreateMemberRequest;
import fr.library.api.dto.MemberDto;
import fr.library.api.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/members")
public class MemberController {

    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberDto> list() {
        return memberService.listMembers().stream().map(MemberDto::from).toList();
    }

    @GetMapping("/{id}")
    public MemberDto get(@PathVariable("id") Long id) {
        return MemberDto.from(memberService.getMember(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberDto create(@Valid @RequestBody CreateMemberRequest request) {
        return MemberDto.from(memberService.createMember(request));
    }
}
