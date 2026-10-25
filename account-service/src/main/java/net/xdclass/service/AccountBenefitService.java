package net.xdclass.service;

import net.xdclass.dto.BenefitGrantMessage;
import org.springframework.stereotype.Service;

@Service
public interface AccountBenefitService {
    void grantBenefit(BenefitGrantMessage message);
}
