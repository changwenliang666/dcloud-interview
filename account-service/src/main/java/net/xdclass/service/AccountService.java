package net.xdclass.service;

import net.xdclass.dto.SendCheckCodeDTO;
import net.xdclass.util.JsonData;

public interface AccountService {
    JsonData sendCheckCode(SendCheckCodeDTO sendCheckCodeDTO);
}
