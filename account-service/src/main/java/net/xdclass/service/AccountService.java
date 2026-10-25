package net.xdclass.service;

import net.xdclass.controller.req.AccountLoginReq;
import net.xdclass.controller.req.SendCheckCodeReq;
import net.xdclass.dto.AccountLoginResultVO;
import net.xdclass.dto.AccountVO;
import net.xdclass.util.JsonData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {
    JsonData sendCheckCode(SendCheckCodeReq req);
    AccountLoginResultVO login(AccountLoginReq req);
    List<AccountVO> getAllUser(String status);
    JsonData uploadAvatar(MultipartFile file);
}
