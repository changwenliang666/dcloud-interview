package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author 小滴课堂-二当家小D,
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("interview_round")
@Schema(name = "InterviewRoundDO", description = "")
public class InterviewRoundDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("interview_id")
    private Long interviewId;

    @TableField("account_id")
    private Long accountId;

    @Schema(description = "第几轮面试")
    @TableField("round_number")
    private Integer roundNumber;

    @Schema(description = "本轮面试类型")
    @TableField("round_type")
    private String roundType;

    @Schema(description = "面试状态：IN_PROGRESS、EVALUATING、COMPLETED")
    @TableField("status")
    private String status;

    @Schema(description = "本轮面试的主要考察内容")
    @TableField("description")
    private String description;

    @Schema(description = "面试总题目数量")
    @TableField("total_question")
    private Integer totalQuestion;

    @Schema(description = "已经回答的题目")
    @TableField("answered_questions")
    private Integer answeredQuestions;

    @Schema(description = "总体评分")
    @TableField("overall_score")
    private BigDecimal overallScore;

    @Schema(description = "面试总结和评价")
    @TableField("summary")
    private String summary;

    @Schema(description = "优势分析")
    @TableField("strength")
    private String strength;

    @Schema(description = "需要改进的地方")
    @TableField("improvement")
    private String improvement;

    @Schema(description = "具体建议")
    @TableField("suggestion")
    private String suggestion;

    @TableField("gmt_modified")
    private Date gmtModified;

    @TableField("gmt_create")
    private Date gmtCreate;
}
