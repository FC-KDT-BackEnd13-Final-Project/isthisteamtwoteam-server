package org.etmetmy.bn_server.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 비동기 처리를 위한 설정
 * Activity Log 등 비동기 이벤트 처리에 사용
 */
@Configuration
@EnableAsync
public class AsyncConfig {
}