package org.example.demo_ssr_v1_1._core.config;

import lombok.RequiredArgsConstructor;
import org.example.demo_ssr_v1_1._core.interceptor.LoginInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 설정 클래스
 * @C, @S, @R, @Com..
 */
//@Component 클래스 내부에서 @Bean 어노테이션을 사용해야 된다면 @Configuration 사용해야 한다.
@Configuration // 내부도 IoC 대상 여부 확인
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    // DI 처리
    private final LoginInterceptor loginInterceptor;

    // ps. 인터셉터는 당연히 여러개 등록 가능 함...
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1. 설정에 LoginInterceptor 를 등록하는 코드
        // 2. 인터셉터가 동작할 URL 패턴 지정
        // 3. 어떤 URL 요청이 로그인 여부를 필요할지 확인 해야 함.
        //      /board/**   <-- 일단 이 엔드포인트 다 검사 시킬 꺼야
        //      /user/**    <-- 일단 이 엔드포인트 다 검사 시킬 꺼야
        //      -> 단, 특정 URL 은 제외 시킬꺼야
        registry.addInterceptor(loginInterceptor)
                // /**  <-- 모든 URL 제외 대상이 됨. 일단 사용 안함
                .addPathPatterns("/board/**", "/user/**", "/reply/**")
                .excludePathPatterns(
                        "/login",
                        "/join",
                        "/logout",
                        "/board/list",
                        "/",
                        "/board/{id:\\d+}",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.io",
                        "h2-console/**"
            );
            // ||d+ 는 정규표현식으로 1개 이상의 숫자를 의미한다.
            // /board/1, board/1234 <-- 허용
            // /board/abc 같은 경우 매칭 되지 않음
    }
}
