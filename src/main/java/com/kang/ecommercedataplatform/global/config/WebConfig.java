package com.kang.ecommercedataplatform.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** 모든 @RestController에 /api 접두사를 여기서 한 번만 붙인다 — 각 컨트롤러가 중복해서 하드코딩하지 않게. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String API_PREFIX = "/api";

    @Override
    public void configurePathMatch(PathMatchConfigurer configurer) {
        configurer.addPathPrefix(API_PREFIX, target -> target.isAnnotationPresent(RestController.class));
    }
}
