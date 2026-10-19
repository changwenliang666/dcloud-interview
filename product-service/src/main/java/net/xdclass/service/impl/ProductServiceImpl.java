package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import net.xdclass.enums.StatusEnum;
import net.xdclass.mapper.BenefitMapper;
import net.xdclass.mapper.PackageBenefitMapper;
import net.xdclass.mapper.ProductPackageMapper;
import net.xdclass.model.BenefitDO;
import net.xdclass.model.PackageBenefitDO;
import net.xdclass.model.ProductPackageDO;
import net.xdclass.service.ProductService;
import net.xdclass.util.SpringBeanUtil;
import net.xdclass.vo.BenefitVO;
import net.xdclass.vo.PackageBenefitVO;
import net.xdclass.vo.ProductPackageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private BenefitMapper benefitMapper;
    @Autowired
    private PackageBenefitMapper packageBenefitMapper;
    @Autowired
    private ProductPackageMapper productPackageMapper;

    @Override
    public List<BenefitVO> getBenefit() {
        LambdaQueryWrapper<BenefitDO> queryWrapper = new LambdaQueryWrapper<BenefitDO>()
                .eq(BenefitDO::getStatus, StatusEnum.ON.name());
        return benefitMapper.selectList(queryWrapper).stream().map(this::toBenefitVO).toList();
    }

    private BenefitVO toBenefitVO(BenefitDO benefit) {
        BenefitVO vo = SpringBeanUtil.copyProperties(benefit, BenefitVO.class);
        vo.setHot(benefit.getIsHot());
        vo.setRecommended(benefit.getIsRecommended());
        return vo;
    }

    @Override
    public List<ProductPackageVO> getPackageBenefit() {
        LambdaQueryWrapper<ProductPackageDO> queryWrapper = new LambdaQueryWrapper<ProductPackageDO>()
                .eq(ProductPackageDO::getStatus, StatusEnum.ON.name());
        List<ProductPackageDO> productList = productPackageMapper.selectList(queryWrapper);

        return productList.stream().map(productPackage -> {
            ProductPackageVO vo = SpringBeanUtil.copyProperties(productPackage, ProductPackageVO.class);
            vo.setRecommended(productPackage.getIsRecommended());
            vo.setBenefitList(listPackageBenefit(productPackage.getId()));
            return vo;
        }).toList();
    }

    private List<PackageBenefitVO> listPackageBenefit(Long packageId) {
        List<PackageBenefitDO> relationList = packageBenefitMapper.selectList(
                new LambdaQueryWrapper<PackageBenefitDO>()
                        .eq(PackageBenefitDO::getProductPackageId, packageId)
        );
        if (relationList.isEmpty()) {
            return List.of();
        }

        List<String> benefitCodeList = relationList.stream().map(PackageBenefitDO::getBenefitCode).toList();
        Map<String, BenefitDO> benefitMap = benefitMapper.selectList(
                new LambdaQueryWrapper<BenefitDO>().in(BenefitDO::getBenefitCode, benefitCodeList)
        ).stream().collect(Collectors.toMap(BenefitDO::getBenefitCode, Function.identity(), (left, right) -> left));

        return relationList.stream()
                .map(relation -> toPackageBenefitVO(relation, benefitMap.get(relation.getBenefitCode())))
                .filter(Objects::nonNull)
                .toList();
    }

    private PackageBenefitVO toPackageBenefitVO(PackageBenefitDO relation, BenefitDO benefit) {
        if (benefit == null) {
            return null;
        }
        PackageBenefitVO vo = SpringBeanUtil.copyProperties(benefit, PackageBenefitVO.class);
        vo.setHot(benefit.getIsHot());
        vo.setRecommended(benefit.getIsRecommended());
        vo.setCount(relation.getCount());
        return vo;
    }
}
