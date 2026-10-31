package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import io.swagger.v3.core.util.Json;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.config.AiEngineProperties;
import net.xdclass.constant.ResumeAnalyseCheckLevel;
import net.xdclass.controller.req.ResumeAnalyseCallbackReq;
import net.xdclass.dto.AccountDTO;
import net.xdclass.dto.BenefitCheckResultDTO;
import net.xdclass.dto.ResumeCheckStatusDTO;
import net.xdclass.dto.ResumeDTO;
import net.xdclass.enums.BenefitEnum;
import net.xdclass.enums.BenefitTaskStateEnum;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.ResumeStatus;
import net.xdclass.exception.BizException;
import net.xdclass.feign.AccountBenefitFeign;
import net.xdclass.interceptor.AccountLoginInterceptor;
import net.xdclass.mapper.ResumeMapper;
import net.xdclass.model.ResumeDO;
import net.xdclass.req.BenefitCheckReq;
import net.xdclass.service.BenefitTaskCallback;
import net.xdclass.service.OssService;
import net.xdclass.service.ResumeService;
import net.xdclass.strategy.BenefitMessageStrategy;
import net.xdclass.strategy.BenefitMessageStrategyFactory;
import net.xdclass.util.FileUtil;
import net.xdclass.util.JsonData;
import net.xdclass.util.SpringBeanUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.extractor.WordExtractor;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final OssService ossService;
    private final ResumeMapper resumeMapper;
    private final AccountBenefitFeign accountBenefitFeign;
    private final BenefitMessageStrategyFactory benefitMessageStrategyFactory;
    private final BenefitTaskCallback benefitTaskCallback;
    private final AiEngineProperties aiEngineProperties;


    /**
     * 从MultipartFile中提取文本内容
     * 直接使用文件的InputStream，避免上传后再下载的冗余操作
     */
    private String extractTextFromMultipartFile(MultipartFile file, String fileType) throws IOException {
        log.debug("开始从上传文件提取内容，文件：{}，类型：{}", file.getOriginalFilename(), fileType);

        try (InputStream inputStream = file.getInputStream()) {
            return switch (fileType.toLowerCase()) {
                case "pdf" -> {
                    try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {
                        if (document.isEncrypted()) {
                            throw new RuntimeException("PDF文件已加密，无法提取文本");
                        }
                        PDFTextStripper stripper = new PDFTextStripper();
                        yield stripper.getText(document);
                    }
                }
                case "doc" -> {
                    try (HWPFDocument document = new HWPFDocument(inputStream);
                         WordExtractor extractor = new WordExtractor(document)) {
                        yield extractor.getText();
                    }
                }
                case "docx" -> {
                    try (XWPFDocument document = new XWPFDocument(inputStream);
                         XWPFWordExtractor extractor = new XWPFWordExtractor(document)) {
                        yield extractor.getText();
                    }
                }
                default -> throw new RuntimeException("不支持的文件类型：" + fileType);
            };
        } catch (IOException e) {
            log.error("提取文件内容失败：{}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public ResumeDTO uploadAndParseResume(MultipartFile file) {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        log.info("开始上传并解析简历，用户：{}，文件：{}", accountDTO.getUsername(), file.getOriginalFilename());

        // 验证文件
        FileUtil.validateFile(file);

        String fileType = FileUtil.getFileType(file.getOriginalFilename());
        String content = null;

        // 1. 先解析文件内容（直接从MultipartFile的InputStream）
        try {
            content = extractTextFromMultipartFile(file, fileType);
            log.info("简历内容解析成功，文件：{}，内容长度：{}", file.getOriginalFilename(), content.length());
        } catch (Exception e) {
            log.error("简历解析失败，文件：{}，错误：{}", file.getOriginalFilename(), e.getMessage(), e);
            throw new RuntimeException("简历解析失败：" + e.getMessage());
        }

        // 2. 上传文件到OSS
        String fileUrl = ossService.uploadSecureFile(file, "resume");
        log.info("文件上传到OSS成功，URL：{}", fileUrl);

        // 3. 保存到数据库（包含解析后的内容）
        ResumeDO resumeDO = ResumeDO.builder()
                .accountId(accountDTO.getId())
                .filename(file.getOriginalFilename())
                .filePath(fileUrl)
                .fileType(fileType)
                .content(content)
                .status(ResumeStatus.UPLOADED.name())
                .build();
        resumeMapper.insert(resumeDO);
        log.info("简历保存成功，ID：{}", resumeDO.getId());

        return SpringBeanUtil.copyProperties(resumeDO, ResumeDTO.class);
    }

    @Override
    public List<ResumeDTO> list() {
        Long accountId = currentAccountId();
        List<ResumeDO> resumeList = resumeMapper.selectList(new LambdaQueryWrapper<ResumeDO>()
                .eq(ResumeDO::getAccountId, accountId)
                .orderByDesc(ResumeDO::getGmtCreate));
        log.info("查询简历列表, accountId:{}, size:{}", accountId, resumeList.size());
        return SpringBeanUtil.copyProperties(resumeList, ResumeDTO.class);
    }

    @Override
    public ResumeDTO findById(Long id, Long accountId) {
        Long currentAccountId = currentAccountId();
        if (!currentAccountId.equals(accountId)) {
            log.warn("无权查看简历, id:{}, accountId:{}, currentAccountId:{}", id, accountId, currentAccountId);
            throw new BizException(BizCodeEnum.ACCOUNT_NO_AUTH);
        }
        ResumeDO resumeDO = findOwnedResume(id, accountId);
        if (resumeDO == null) {
            return null;
        }
        return SpringBeanUtil.copyProperties(resumeDO, ResumeDTO.class);
    }

    @Override
    public boolean deleteById(Long id) {
        Long accountId = currentAccountId();
        ResumeDO resumeDO = findOwnedResume(id, accountId);
        int rows = resumeMapper.deleteById(resumeDO.getId());
        if (rows <= 0) {
            log.error("删除简历失败, id:{}, accountId:{}", id, accountId);
            throw new BizException(BizCodeEnum.RESUME_NOT_EXIST);
        }
        boolean deleted = ossService.deleteFile(resumeDO.getFilePath());
        if (!deleted) {
            log.warn("简历记录已删除，OSS文件删除失败, id:{}, filePath:{}", id, resumeDO.getFilePath());
        }
        log.info("简历删除成功, id:{}, accountId:{}", id, accountId);
        return true;
    }

    @Override
    public String preview(Long id) {
        ResumeDO resumeDO = findOwnedResume(id, currentAccountId());
        String previewUrl = ossService.generatePreviewUrl(resumeDO.getFilePath());
        if (StringUtils.isBlank(previewUrl)) {
            log.error("生成简历预览地址失败, id:{}, filePath:{}", id, resumeDO.getFilePath());
            throw new BizException(BizCodeEnum.RESUME_PREVIEW_FAIL);
        }
        log.info("生成简历预览地址成功, id:{}", id);
        return previewUrl;
    }

    private ResumeDO findOwnedResume(Long id, Long accountId) {
        ResumeDO resumeDO = resumeMapper.selectOne(new LambdaQueryWrapper<ResumeDO>()
                .eq(ResumeDO::getId, id)
                .eq(ResumeDO::getAccountId, accountId));
        if (resumeDO == null) {
            log.warn("简历不存在, id:{}, accountId:{}", id, accountId);
        }
        return resumeDO;
    }

    private Long currentAccountId() {
        AccountDTO accountDTO = AccountLoginInterceptor.threadLocal.get();
        if (accountDTO == null || accountDTO.getId() == null) {
            throw new BizException(BizCodeEnum.ACCOUNT_UNLOGIN);
        }
        return accountDTO.getId();
    }

    /**
     * 原子更新简历状态，返回受影响行数。
     * 进入分析中时，仅非 IN_PROCESS 允许更新，避免重复分析。
     * 写入其他状态时，仅允许从 IN_PROCESS 迁出，避免覆盖已完成结果。
     */
    private int updateResumeStatus(Long resumeId, Long accountId, ResumeStatus targetStatus) {
        LambdaUpdateWrapper<ResumeDO> wrapper = new LambdaUpdateWrapper<ResumeDO>()
                .set(ResumeDO::getStatus, targetStatus.name())
                .eq(ResumeDO::getId, resumeId)
                .eq(ResumeDO::getAccountId, accountId);
        if (ResumeStatus.IN_PROCESS == targetStatus) {
            wrapper.ne(ResumeDO::getStatus, ResumeStatus.IN_PROCESS.name());
        } else {
            wrapper.eq(ResumeDO::getStatus, ResumeStatus.IN_PROCESS.name());
        }
        return resumeMapper.update(wrapper);
    }

    /**
     * 失败后回写简历状态。回写失败只记日志，避免盖住 analyse 的原始错误。
     */
    private void rollbackResumeStatus(Long resumeId, Long accountId, ResumeStatus targetStatus) {
        try {
            int rows = updateResumeStatus(resumeId, accountId, targetStatus);
            if (rows != 1) {
                log.warn("回写简历状态未生效, resumeId:{}, accountId:{}, targetStatus:{}, rows:{}",
                        resumeId, accountId, targetStatus, rows);
                return;
            }
            log.info("回写简历状态成功, resumeId:{}, accountId:{}, targetStatus:{}",
                    resumeId, accountId, targetStatus);
        } catch (Exception e) {
            log.error("回写简历状态失败, resumeId:{}, accountId:{}, targetStatus:{}",
                    resumeId, accountId, targetStatus, e);
        }
    }

    @Override
    public JsonData analyse(BenefitCheckReq req) {
        // 检查简历是否存在
        ResumeDTO resumeDTO = findById(req.getBusinessId(), req.getAccountId());
        if (resumeDTO == null) {
            log.info("简历分析失败，简历不存在，简历id:{},用户id:{}", req.getBusinessId(), req.getAccountId());
            return JsonData.buildError("简历不存在");
        }
        // 更新简历状态为分析中，仅非 IN_PROCESS 状态允许更新，避免重复分析
        int updateRows = updateResumeStatus(resumeDTO.getId(), req.getAccountId(), ResumeStatus.IN_PROCESS);

        if (updateRows <= 0) {
            log.info("简历分析失败，简历正在分析中, 简历id:{}, 用户id:{}", req.getBusinessId(), req.getAccountId());
            return JsonData.buildError("简历正在分析中");
        }
        String messageId = null;
        try {
            JsonData checkBenefitResult = accountBenefitFeign.checkAndDeductBenefit(req);
            if (checkBenefitResult.getCode() != 0) {
                ResumeStatus rollbackStatus = Objects.equals(checkBenefitResult.getCode(), BizCodeEnum.BENEFIT_NOT_ENOUGH.getCode())
                        ? ResumeStatus.BENEFIT_NOT_ENOUGH
                        : ResumeStatus.UNKOWN_ERROR;
                rollbackResumeStatus(resumeDTO.getId(), req.getAccountId(), rollbackStatus);
                String errorMsg = rollbackStatus == ResumeStatus.BENEFIT_NOT_ENOUGH ? "权益不足，分析简历失败" : "分析简历失败";
                return JsonData.buildError(errorMsg);
            }
            BenefitCheckResultDTO deductResult = checkBenefitResult.getData(BenefitCheckResultDTO.class);
            messageId = deductResult.getMessageId();
            log.info("权益扣减成功，messageId={}", messageId);

            BenefitMessageStrategy strategy = benefitMessageStrategyFactory.getStrategy(req.getBenefitCode());
            strategy.sendBusinessMessage(messageId, req.getBusinessId(), req.getAccountId());

            Map<String, Object> result = new HashMap<>();
            result.put("resumeId", resumeDTO.getId());
            result.put("status", "analysing");
            result.put("message", "简历正在分析中，请稍候...");
            return JsonData.buildSuccess(result);
        } catch (Exception e) {
            log.error("分析简历失败, resumeId:{}, accountId:{}", resumeDTO.getId(), req.getAccountId(), e);
            rollbackResumeStatus(resumeDTO.getId(), req.getAccountId(), ResumeStatus.UNKOWN_ERROR);
            if (StringUtils.isNotBlank(messageId)) {
                benefitTaskCallback.notify(messageId, BenefitTaskStateEnum.CANCEL);
            }
            return JsonData.buildError("分析简历失败");
        }
    }

    @Override
    public ResumeCheckStatusDTO resolveBenefitCheckStatus(Long id, Long accountId, String messageId, Integer checkLevel) {
        ResumeDO resumeDO = findOwnedResume(id, accountId);
        if (resumeDO == null || resumeDO.getStatus() == null) {
            throw new BizException(BizCodeEnum.RESUME_NOT_EXIST);
        }
        if (ResumeStatus.isProcessing(resumeDO.getStatus())
                && checkLevel != null
                && checkLevel <= ResumeAnalyseCheckLevel.REDISPATCH
                && StringUtils.isBlank(resumeDO.getAnalyseMessageId())) {
            redispatchAnalyse(messageId, id, accountId);
        }
        if (ResumeAnalyseCheckLevel.isForceTimeout(checkLevel) && markAnalyzeTimeout(id, accountId)) {
            return ResumeCheckStatusDTO.builder()
                    .status(ResumeStatus.ANALYZE_TIMEOUT.name())
                    .dispatched(Integer.valueOf(1).equals(resumeDO.getAnalyseAccepted()))
                    .build();
        }
        ResumeDO latest = findOwnedResume(id, accountId);
        if (latest == null || latest.getStatus() == null) {
            throw new BizException(BizCodeEnum.RESUME_NOT_EXIST);
        }
        return ResumeCheckStatusDTO.builder()
                .status(latest.getStatus())
                .dispatched(Integer.valueOf(1).equals(latest.getAnalyseAccepted()))
                .build();
    }

    @Override
    public void handleAnalyseCallback(String token, ResumeAnalyseCallbackReq req) {
        if (!callbackTokenMatches(token)) {
            log.warn("简历分析回调令牌不匹配, resumeId={}, messageId={}", req.getResumeId(), req.getMessageId());
            throw new BizException(BizCodeEnum.ACCOUNT_NO_AUTH);
        }
        if (Boolean.TRUE.equals(req.getSuccess())) {
            String skillTags = null;
            if (req.getSkillTags() != null && !req.getSkillTags().isEmpty()) {
                skillTags = req.getSkillTags().stream()
                        .filter(StringUtils::isNotBlank)
                        .collect(Collectors.joining(","));
            }
            finishAnalyse(req.getResumeId(), req.getAccountId(), req.getMessageId(), req.getEvaluation(), skillTags);
            return;
        }
        failAnalyse(req.getResumeId(), req.getAccountId(), req.getMessageId(), ResumeStatus.AI_ERROR);
    }

    @Override
    public void finishAnalyse(Long resumeId, Long accountId, String messageId, String evaluation, String skillTags) {
        int rows = resumeMapper.update(new LambdaUpdateWrapper<ResumeDO>()
                .set(ResumeDO::getStatus, ResumeStatus.ANALYZED.name())
                .set(StringUtils.isNotBlank(evaluation), ResumeDO::getEvaluation, evaluation)
                .set(StringUtils.isNotBlank(skillTags), ResumeDO::getSkillTags, skillTags)
                .eq(ResumeDO::getId, resumeId)
                .eq(ResumeDO::getAccountId, accountId)
                .eq(ResumeDO::getStatus, ResumeStatus.IN_PROCESS.name()));
        if (rows != 1) {
            log.info("简历未从分析中完成，跳过完成回调, resumeId:{}, messageId:{}", resumeId, messageId);
            return;
        }
        if (benefitTaskCallback.notify(messageId, BenefitTaskStateEnum.FINISH) == BenefitTaskCallback.Result.REJECTED) {
            withdrawAnalyzedResult(resumeId, accountId);
        }
    }

    @Override
    public void failAnalyse(Long resumeId, Long accountId, String messageId, ResumeStatus failStatus) {
        if (failStatus == null || !ResumeStatus.isResumeAnalyzeError(failStatus.name())) {
            throw new BizException(BizCodeEnum.SYSTEM_ERROR);
        }
        int rows = updateResumeStatus(resumeId, accountId, failStatus);
        if (rows != 1) {
            log.info("简历状态未从分析中迁出，跳过失败回调, resumeId:{}, messageId:{}, target:{}",
                    resumeId, messageId, failStatus);
            return;
        }
        BenefitTaskCallback.Result result = benefitTaskCallback.notify(messageId, BenefitTaskStateEnum.CANCEL);
        if (result == BenefitTaskCallback.Result.CALL_FAILED) {
            log.warn("分析失败后退次回调异常，等待延迟检查补偿, messageId:{}", messageId);
        }
    }

    private boolean markAnalyzeTimeout(Long id, Long accountId) {
        int rows = resumeMapper.update(new LambdaUpdateWrapper<ResumeDO>()
                .set(ResumeDO::getStatus, ResumeStatus.ANALYZE_TIMEOUT.name())
                .eq(ResumeDO::getId, id)
                .eq(ResumeDO::getAccountId, accountId)
                .in(ResumeDO::getStatus, ResumeStatus.IN_PROCESS.name(), ResumeStatus.UPLOADED.name()));
        if (rows == 1) {
            log.info("简历分析超时，标记失败, resumeId:{}, accountId:{}", id, accountId);
            return true;
        }
        return false;
    }

    private String currentStatus(Long resumeId, Long accountId) {
        ResumeDO resumeDO = findOwnedResume(resumeId, accountId);
        return resumeDO == null ? null : resumeDO.getStatus();
    }

    /**
     * 次数已经退回，不能再把分析结果留给用户。
     */
    private void withdrawAnalyzedResult(Long resumeId, Long accountId) {
        int rows = resumeMapper.update(new LambdaUpdateWrapper<ResumeDO>()
                .set(ResumeDO::getStatus, ResumeStatus.ANALYZE_TIMEOUT.name())
                .set(ResumeDO::getEvaluation, null)
                .eq(ResumeDO::getId, resumeId)
                .eq(ResumeDO::getAccountId, accountId)
                .eq(ResumeDO::getStatus, ResumeStatus.ANALYZED.name()));
        if (rows == 1) {
            log.info("权益已补偿，撤回分析结果, resumeId:{}", resumeId);
            return;
        }
        log.info("撤回分析结果未生效, resumeId:{}", resumeId);
    }

    private void redispatchAnalyse(String messageId, Long resumeId, Long accountId) {
        try {
            BenefitMessageStrategy strategy = benefitMessageStrategyFactory.getStrategy(BenefitEnum.RESUME_ANALYSE.name());
            strategy.sendBusinessMessage(messageId, resumeId, accountId);
            log.info("补发简历分析消息, messageId={}, resumeId={}", messageId, resumeId);
        } catch (Exception e) {
            log.error("补发简历分析消息失败, messageId={}, resumeId={}", messageId, resumeId, e);
        }
    }

    private boolean callbackTokenMatches(String token) {
        String expected = aiEngineProperties.getCallbackToken();
        if (StringUtils.isBlank(expected) || token == null) {
            return false;
        }
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), token.getBytes(StandardCharsets.UTF_8));
    }
}
