package net.xdclass;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@Slf4j
@SpringBootApplication
@EnableTransactionManagement
@MapperScan("net.xdclass.mapper")
@EnableCaching
@EnableScheduling
@EnableDiscoveryClient
@EnableFeignClients
public class AIServiceApplication {
    @SneakyThrows
    public static void main(String[] args) {
        SpringApplication.run(AIServiceApplication.class, args);
    }
}
