package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.dto.AccountDTO;
import net.xdclass.dto.ResumeDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.enums.ResumeStatus;
import net.xdclass.exception.BizException;
import net.xdclass.interceptor.AccountLoginInterceptor;
import net.xdclass.mapper.ResumeMapper;
import net.xdclass.model.ResumeDO;
import net.xdclass.service.OssService;
import net.xdclass.service.ResumeService;
import net.xdclass.util.FileUtil;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ResumeServiceImpl implements ResumeService {

    private final OssService ossService;
    private final ResumeMapper resumeMapper;


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
            throw new BizException(BizCodeEnum.RESUME_NOT_EXIST);
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
}
