package net.xdclass.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.dto.ResumeDTO;
import net.xdclass.service.ResumeService;
import net.xdclass.util.JsonData;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resume/")
@Slf4j
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传简历", description = "上传简历文件（PDF/DOC/DOCX），并自动解析内容")
    public JsonData uploadResume(
            @Parameter(description = "简历文件", required = true)
            @RequestPart("file") MultipartFile file) {
        ResumeDTO resume = resumeService.uploadAndParseResume(file);
        return JsonData.buildSuccess(resume);
    }

    @GetMapping("/list")
    @Operation(summary = "查询简历列表", description = "根据当前登录用户查询简历列表")
    public JsonData list() {
        log.info("查询当前用户简历列表");
        return JsonData.buildSuccess(resumeService.list());
    }

    @GetMapping("/detail")
    @Operation(summary = "查看简历详情", description = "根据简历ID和账户ID查询简历详情")
    public JsonData detail(
            @Parameter(description = "简历ID", required = true) @RequestParam("id") Long id,
            @Parameter(description = "账户ID", required = true) @RequestParam("accountId") Long accountId) {
        log.info("查询简历详情, id:{}, accountId:{}", id, accountId);
        return JsonData.buildSuccess(resumeService.findById(id, accountId));
    }

    @PostMapping("/delete")
    @Operation(summary = "删除简历", description = "根据简历ID删除当前用户的简历")
    public JsonData delete(@Parameter(description = "简历ID", required = true) @RequestParam("id") Long id) {
        log.info("删除简历, id:{}", id);
        return JsonData.buildSuccess(resumeService.deleteById(id));
    }

    @GetMapping("/preview")
    @Operation(summary = "预览简历", description = "根据简历ID签发OSS临时预览地址")
    public JsonData preview(@Parameter(description = "简历ID", required = true) @RequestParam("id") Long id) {
        log.info("预览简历, id:{}", id);
        return JsonData.buildSuccess(resumeService.preview(id));
    }

}
