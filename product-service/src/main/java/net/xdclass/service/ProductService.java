package net.xdclass.service;

import net.xdclass.vo.BenefitVO;
import net.xdclass.vo.ProductPackageVO;

import java.util.List;

public interface ProductService {
    List<BenefitVO> getBenefit();
    List<ProductPackageVO> getPackageBenefit();
}
