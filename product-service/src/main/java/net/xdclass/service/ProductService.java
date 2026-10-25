package net.xdclass.service;

import net.xdclass.dto.BenefitVO;
import net.xdclass.dto.ProductPackageVO;

import java.util.List;

public interface ProductService {
    List<BenefitVO> getBenefit();
    BenefitVO getBenefitById(Long id);
    List<ProductPackageVO> getPackageBenefit();
    ProductPackageVO getPackageById(Long id);

}
