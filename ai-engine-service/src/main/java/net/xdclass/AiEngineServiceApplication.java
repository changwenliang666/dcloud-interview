package net.xdclass;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AiEngineServiceApplication {
    public static void main(String[] args) {
        // 在 Spring Boot 项目启动前，加载 .env 文件,将其内容设置为系统属性
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
        SpringApplication.run(AiEngineServiceApplication.class, args);
    }
}
