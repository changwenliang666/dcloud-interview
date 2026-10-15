package net.xdclass.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
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
@TableName("question")
@Schema(name = "QuestionDO", description = "")
public class QuestionDO implements Serializable {

    private static final long serialVersionUID = 1L;

      @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "面试轮次ID")
    @TableField("interview_id")
    private Long interviewId;

    @TableField("account_id")
    private Long accountId;

    @Schema(description = "轮次ID")
    @TableField("interview_round_id")
    private Long interviewRoundId;

    @Schema(description = "状态")
    @TableField("status")
    private String status;

    @Schema(description = "用户答案")
    @TableField("user_answer")
    private String userAnswer;

    @Schema(description = "题目点评")
    @TableField("evaluation")
    private String evaluation;

    @Schema(description = "题目内容")
    @TableField("content")
    private String content;

    @Schema(description = "CODING、SHORT_ANSWER")
    @TableField("type")
    private String type;

    @Schema(description = "BEGINNER、INTERMEDIATE、ADVANCED")
    @TableField("difficulty")
    private String difficulty;

    @Schema(description = "题目分类（如：Java基础、Spring框架、数据库等）")
    @TableField("category")
    private String category;

    @Schema(description = "标准答案")
    @TableField("standard_answer")
    private String standardAnswer;

    @Schema(description = "关键要点，JSON格式")
    @TableField("key_points")
    private String keyPoints;

    @Schema(description = "满分分值100")
    @TableField("max_score")
    private Integer maxScore;

    @Schema(description = "答题时间限制（分钟）")
    @TableField("time_limit")
    private Integer timeLimit;

    @Schema(description = "回答评分")
    @TableField("user_score")
    private Integer userScore;

    @TableField("gmt_create")
    private Date gmtCreate;

    @TableField("gmt_modified")
    private Date gmtModified;
}
