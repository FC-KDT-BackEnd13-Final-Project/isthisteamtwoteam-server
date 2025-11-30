package org.etmetmy.bn_server.config;

import org.etmetmy.bn_server.web.RoleAuthorizationInterceptor;
import org.etmetmy.bn_server.web.UserAuthorizationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{


//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new UserAuthorizationInterceptor())
//                .order(1)
//                .addPathPatterns("/api/v1/**")
//                .excludePathPatterns(
//                        "/api/v1/login",
//                        "/api/v1/logout",
//                        "/api/v1/home"
//                        );
//
//        registry.addInterceptor(new RoleAuthorizationInterceptor())
//                .order(2)
//                .addPathPatterns(
//                        "/api/v1/users/**",
//                        "/api/v1/developers/**",
//                        "/api/v1/admin/**"
//                );
//    }
}
