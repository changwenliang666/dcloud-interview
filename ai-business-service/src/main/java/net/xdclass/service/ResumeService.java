package net.xdclass.service;

import net.xdclass.controller.req.ResumeAnalyseCallbackReq;
import net.xdclass.dto.ResumeCheckStatusDTO;
import net.xdclass.dto.ResumeDTO;
import net.xdclass.enums.ResumeStatus;
import net.xdclass.req.BenefitCheckReq;
import net.xdclass.util.JsonData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ResumeService {

    ResumeDTO uploadAndParseResume(MultipartFile file);

    /**
     * 查询当前登录用户的简历列表
     */
    List<ResumeDTO> list();

    /**
     * 根据简历ID和账户ID查询简历详情
     */
    ResumeDTO findById(Long id, Long accountId);

    /**
     * 删除当前用户的简历
     */
    boolean deleteById(Long id);

    /**
     * 生成简历预览地址
     */
    String preview(Long id);

    /**
     * Ai 分析简历
     */
    JsonData analyse(BenefitCheckReq req);

    /**
     * 延迟检查读取简历状态。checkLevel 为 3 或 4 时，仍在分析中的简历会标记为分析超时。
     */
    ResumeCheckStatusDTO resolveBenefitCheckStatus(Long id, Long accountId, String messageId, Integer checkLevel);

    /**
     * Serverless 分析完成后的回调。
     */
    void handleAnalyseCallback(String token, ResumeAnalyseCallbackReq req);

    /**
     * 分析成功后回调结案。调用 account-service 失败时保留已分析状态，交给延迟检查补记完成。
     */
    void finishAnalyse(Long resumeId, Long accountId, String messageId, String evaluation, String skillTags);

    /**
     * 分析失败后回调退次。调用 account-service 失败时保留失败状态，交给延迟检查补退次数。
     */
    void failAnalyse(Long resumeId, Long accountId, String messageId, ResumeStatus failStatus);
}
