package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "InterviewCreateMessageDTO",description = "简历分析队列消息DTO")
public class InterviewCreateMessageDTO  {
    @Schema(description = "面试id")
    private Long interviewId;
    @Schema(description = "账号id")
    private Long accountId;
    @Schema(description = "消息id")
    private String messageId;
}
