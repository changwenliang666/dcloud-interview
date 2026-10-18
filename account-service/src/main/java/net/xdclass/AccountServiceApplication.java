package net.xdclass;

import io.github.cdimascio.dotenv.Dotenv;
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
public class AccountServiceApplication {
    @SneakyThrows
    public static void main(String[] args) {
        // 在 Spring Boot 项目启动前，加载 .env 文件,将其内容设置为系统属性
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
