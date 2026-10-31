package net.xdclass.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "简历分析结果")
public class ResumeAnalysisResultDTO implements Serializable {


    @Schema(description = "基础定位")
    private BasicPosition basicPosition;

    @Schema(description = "技能维度分析")
    private SkillAnalysis skillAnalysis;

    @Schema(description = "学历维度分析")
    private EducationAnalysis educationAnalysis;

    @Schema(description = "项目维度分析")
    private ProjectAnalysis projectAnalysis;

    @Schema(description = "对标差距")
    private BenchmarkGap benchmarkGap;

    @Schema(description = "改进建议")
    private ImprovementSuggestion improvementSuggestion;

    @Schema(description = "整体总结")
    private OverallSummary overallSummary;

    @Schema(description = "技能标签列表")
    private List<String> skillTags;

    @Schema(description = "整体评价（Markdown格式）")
    private String evaluation;

    // ======================== 内部类1：程序员级别枚举 ========================
    /**
     * 程序员级别枚举（统一级别取值规范）
     */
    public enum ProgrammerLevel {
        JUNIOR("初级（1-3年）"),
        INTERMEDIATE("中级（3-5年）"),
        SENIOR("高级（5-8年）"),
        SENIOR_EXPERT("资深级（8年以上）");

        private final String desc;

        ProgrammerLevel(String desc) {
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }
    }


    // ======================== 内部类2：基础定位维度 ========================
    /**
     * 基础定位：级别判定+岗位适配性
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BasicPosition implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "程序员级别")
        private ProgrammerLevel level;

        @Schema(description = "级别判断依据")
        private String levelJudgmentBasis;

        @Schema(description = "岗位适配程度")
        private String positionFitDegree;

        @Schema(description = "潜在差距")
        private String potentialGap;
    }

    // ======================== 内部类3：技能维度分析 ========================
    /**
     * 技能维度分析：优点+缺点+细节佐证
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "优点列表")
        private List<String> advantages;

        @Schema(description = "缺点列表")
        private List<String> disadvantages;

        @Schema(description = "细节佐证")
        private List<String> detailsEvidence;
    }


    // ======================== 内部类4：学历维度分析 ========================
    /**
     * 学历维度分析：优点+缺点+细节佐证
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EducationAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "优点列表")
        private List<String> advantages;

        @Schema(description = "缺点列表")
        private List<String> disadvantages;

        @Schema(description = "细节佐证")
        private List<String> detailsEvidence;
    }

    // ======================== 内部类5：项目维度分析 ========================
    /**
     * 项目维度分析：优点+缺点+细节佐证
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProjectAnalysis implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "优点列表")
        private List<String> advantages;

        @Schema(description = "缺点列表")
        private List<String> disadvantages;

        @Schema(description = "细节佐证")
        private List<String> detailsEvidence;
    }



    // ======================== 内部类6：对标差距维度 ========================
    /**
     * 对标差距：同级别对标+同岗位对标
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BenchmarkGap implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "同级别标准")
        private List<String> sameLevelStandard;

        @Schema(description = "同级别差距点")
        private List<String> sameLevelGapPoints;

        @Schema(description = "差距程度")
        private String gapDegree;

        @Schema(description = "同岗位要求")
        private List<String> samePositionRequirements;

        @Schema(description = "同岗位差距")
        private String samePositionGap;
    }

    // ======================== 内部类7：改进建议维度 ========================
    /**
     * 改进建议：紧急项+长期项+优先级
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ImprovementSuggestion implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "紧急改进项")
        private List<String> emergencyItems;

        @Schema(description = "长期改进项")
        private List<String> longTermItems;

        @Schema(description = "优先级顺序")
        private List<String> priorityOrder;
    }


    // ======================== 内部类8：整体总结维度 ========================
    /**
     * 整体总结：核心竞争力+短板+适配建议
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OverallSummary implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "核心竞争力")
        private String coreCompetence;

        @Schema(description = "核心短板")
        private List<String> coreShortcomings;

        @Schema(description = "适配建议")
        private List<String> adaptationSuggestions;
    }
}
