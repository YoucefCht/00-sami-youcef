package fr.library.api.service;

import fr.library.api.dto.CreateMemberRequest;
import fr.library.api.entity.Member;
import fr.library.api.exception.BusinessRuleException;
import fr.library.api.exception.ResourceNotFoundException;
import fr.library.api.repository.MemberRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MemberService {

    private static final Logger log = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;

    @Autowired
    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Member> listMembers() {
        return memberRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Adhérent introuvable : id=" + id));
    }

    @Transactional
    public Member createMember(CreateMemberRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessRuleException("Un adhérent avec l'email " + request.email() + " existe déjà");
        }
        Member saved = memberRepository.save(new Member(request.name(), request.email()));
        log.info("Adhérent créé : id={}, nom='{}'", saved.getId(), saved.getName());
        return saved;
    }
}
