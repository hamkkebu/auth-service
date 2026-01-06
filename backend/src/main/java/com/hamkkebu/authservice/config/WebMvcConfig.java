package com.hamkkebu.authservice.config;

import com.hamkkebu.authservice.resolver.AuthCurrentUserArgumentResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

/**
 * Web MVC 설정
 *
 * <p>ArgumentResolver 등록을 위한 설정 클래스입니다.</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final AuthCurrentUserArgumentResolver authCurrentUserArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authCurrentUserArgumentResolver);
    }
}
