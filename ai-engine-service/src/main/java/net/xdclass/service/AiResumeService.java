package net.xdclass.service;

import net.xdclass.controller.req.AnalyseResumeReq;
import net.xdclass.util.JsonData;

public interface AiResumeService {
    JsonData analyseResume(AnalyseResumeReq req);
}
