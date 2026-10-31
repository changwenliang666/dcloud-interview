package net.xdclass.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import net.xdclass.controller.req.AnalyseResumeReq;
import net.xdclass.service.AiResumeService;
import net.xdclass.util.JsonData;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai-engine/resume")
@RequiredArgsConstructor
public class AiResumeController {

    private final AiResumeService aiResumeService;


    @PostMapping("/analyse")
    public JsonData analyseResume(@Valid @RequestBody AnalyseResumeReq req) {
        return aiResumeService.analyseResume(req);
    }
}
