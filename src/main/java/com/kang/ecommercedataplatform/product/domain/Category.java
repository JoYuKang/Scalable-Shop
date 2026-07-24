package com.kang.ecommercedataplatform.product.domain;

import com.kang.ecommercedataplatform.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "category")
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    /** 예: /1/4/12 - 조상 경로를 문자열로 캐싱해 하위 카테고리 조회를 단순화 */
    private String path;

    @Builder
    public Category(String name, Category parent, String path) {
        this.name = name;
        this.parent = parent;
        this.path = path;
    }
}
