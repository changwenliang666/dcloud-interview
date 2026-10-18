package net.xdclass.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountLoginResultDTO {

    private boolean success;

    private String token ;

    private AccountDTO accountDTO;

    private String errorMessage;

    private String qrCodeStatus;

    private String sceneId;
}