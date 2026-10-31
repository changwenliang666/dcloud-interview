package net.xdclass.feign;

import net.xdclass.util.JsonData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ai-business-service", contextId = "aiBusinessServiceStatusFeign")
public interface AiBusinessServiceStatusFegin {

    /**
     * 延迟检查读取简历状态。checkLevel 为 3 或 4 时，仍在分析中的简历会被标记为分析超时。
     */
    @PostMapping("/api/v1/resume/internal/check-status")
    JsonData checkStatus(@RequestParam("id") Long id,
                         @RequestParam("accountId") Long accountId,
                         @RequestParam("messageId") String messageId,
                         @RequestParam("checkLevel") Integer checkLevel);
}
