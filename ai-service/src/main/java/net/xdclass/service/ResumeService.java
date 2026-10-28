package net.xdclass.service;

import net.xdclass.dto.ResumeDTO;
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
}
