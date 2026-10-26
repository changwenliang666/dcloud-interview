package net.xdclass.service;

import net.xdclass.dto.BenefitGrantMessage;

public interface AccountBenefitService {
    void grantBenefit(BenefitGrantMessage message);
}
