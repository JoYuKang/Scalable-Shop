package com.kang.ecommercedataplatform.member.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String loginId;

    /** 향후 암호화 예정 */
    @Column(nullable = false)
    private String password;

    private String name;
    private String email;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Builder
    public Member(String loginId, String password, String name, String email, MemberRole role) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
