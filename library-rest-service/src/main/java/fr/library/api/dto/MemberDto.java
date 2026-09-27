package fr.library.api.dto;

import fr.library.api.entity.Member;

public record MemberDto(Long id, String name, String email) {

    public static MemberDto from(Member member) {
        return new MemberDto(member.getId(), member.getName(), member.getEmail());
    }
}
