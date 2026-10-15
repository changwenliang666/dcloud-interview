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
 * @author WenliangChang
 * @since 2026-10-15
 */
@Getter
@Setter
@TableName("interview")
@Schema(name = "InterviewDO", description = "")
public class InterviewDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "面试标题")
    @TableField("title")
    private String title;

    @Schema(description = "描述")
    @TableField("description")
    private String description;

    @Schema(description = "面试类型，包括专项面、深度面、简历普通面")
    @TableField("type")
    private String type;

    @Schema(description = "拓展字段")
    @TableField("extend_content")
    private String extendContent;

    @Schema(description = "用户求职画像")
    @TableField("profile")
    private String profile;

    @Schema(description = "账号ID")
    @TableField("account_id")
    private Long accountId;

    @Schema(description = "简历ID")
    @TableField("resume_id")
    private Long resumeId;

    @Schema(description = "面试状态：GENERATING、IN_PROGRESS、EVALUATING")
    @TableField("status")
    private String status;

    @Schema(description = "总体评分（0-100分）")
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

    @Schema(description = "技术能力评分")
    @TableField("technical_skills")
    private BigDecimal technicalSkills;

    @Schema(description = "逻辑思维评分")
    @TableField("logical_thinking")
    private BigDecimal logicalThinking;

    @Schema(description = "表达能力评分")
    @TableField("communication")
    private BigDecimal communication;

    @Schema(description = "解决问题能力评分")
    @TableField("problem_solving")
    private BigDecimal problemSolving;

    @Schema(description = "及格题目数（>=60分）")
    @TableField("passed_question")
    private Integer passedQuestion;

    @Schema(description = "优秀题目数（>=80分）")
    @TableField("excellent_question")
    private Integer excellentQuestion;

    @TableField("gmt_create")
    private Date gmtCreate;

    @TableField("gmt_modified")
    private Date gmtModified;
}
