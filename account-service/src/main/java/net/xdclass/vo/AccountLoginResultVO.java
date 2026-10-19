package net.xdclass.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLoginResultVO {

    private boolean success;

    private String token ;

    private AccountDTO accountDTO;

    private String errorMessage;

    private String qrCodeStatus;

    private String sceneId;
}