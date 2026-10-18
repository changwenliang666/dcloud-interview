package net.xdclass.service;

import net.xdclass.controller.rq.AccountLoginReq;
import net.xdclass.controller.rq.SendCheckCodeRq;
import net.xdclass.dto.AccountLoginResultDTO;
import net.xdclass.model.AccountDO;
import net.xdclass.util.JsonData;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AccountService {
    JsonData sendCheckCode(SendCheckCodeRq rq);
    AccountLoginResultDTO login(AccountLoginReq rq);
    List<AccountDO> getAllUser(String status);
    JsonData uploadAvatar(MultipartFile file);
}
