package com.marcosdias.miniifood.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("tc")
@EnabledIfEnvironmentVariable(named = "RUN_TESTCONTAINERS", matches = "true")
class ContainersSmokeTest extends ContainersIntegrationBaseTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    void shouldBootWithPostgresAndRedisContainers() {
        assertThat(cacheManager).isNotNull();
        assertThat(postgres.isRunning()).isTrue();
        assertThat(redis.isRunning()).isTrue();
    }
}

