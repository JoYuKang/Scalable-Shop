package com.kang.ecommercedataplatform.order.infrastructure;

import com.kang.ecommercedataplatform.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberJpaRepository extends JpaRepository<Member, Long> {
}
