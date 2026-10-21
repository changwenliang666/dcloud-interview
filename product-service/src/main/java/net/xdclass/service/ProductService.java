package net.xdclass.service;

import net.xdclass.vo.BenefitVO;
import net.xdclass.vo.PackageBenefitVO;
import net.xdclass.vo.ProductPackageVO;

import java.util.List;

public interface ProductService {
    List<BenefitVO> getBenefit();
    BenefitVO getBenefitById(Long id);
    List<ProductPackageVO> getPackageBenefit();
    ProductPackageVO getPackageById(Long id);

}
