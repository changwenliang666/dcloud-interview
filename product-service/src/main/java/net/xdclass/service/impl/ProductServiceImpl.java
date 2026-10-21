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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ProductServiceImpl implements ProductService {
    private final BenefitMapper benefitMapper;
    private final PackageBenefitMapper packageBenefitMapper;
    private final ProductPackageMapper productPackageMapper;

    public ProductServiceImpl(BenefitMapper benefitMapper, PackageBenefitMapper packageBenefitMapper, ProductPackageMapper productPackageMapper) {
        this.benefitMapper = benefitMapper;
        this.packageBenefitMapper = packageBenefitMapper;
        this.productPackageMapper = productPackageMapper;
    }

    @Override
    public List<BenefitVO> getBenefit() {
        LambdaQueryWrapper<BenefitDO> queryWrapper = new LambdaQueryWrapper<BenefitDO>()
                .eq(BenefitDO::getStatus, StatusEnum.ON.name());
        return SpringBeanUtil.copyProperties(benefitMapper.selectList(queryWrapper), BenefitVO.class);
    }

    @Override
    public BenefitVO getBenefitById(Long id) {
        LambdaQueryWrapper<BenefitDO> queryWrapper = new LambdaQueryWrapper<BenefitDO>().eq(BenefitDO::getId, id);
        BenefitDO benefitDO = benefitMapper.selectOne(queryWrapper);
        BenefitVO benefitVO = null;
        if (benefitDO != null) {
            benefitVO = SpringBeanUtil.copyProperties(benefitDO, BenefitVO.class);
        }
        return benefitVO;
    }

    @Override
    public List<ProductPackageVO> getPackageBenefit() {
        LambdaQueryWrapper<ProductPackageDO> queryWrapper = new LambdaQueryWrapper<ProductPackageDO>()
                .eq(ProductPackageDO::getStatus, StatusEnum.ON.name());
        List<ProductPackageDO> productList = productPackageMapper.selectList(queryWrapper);

        return productList.stream().map(productPackage -> {
            ProductPackageVO vo = SpringBeanUtil.copyProperties(productPackage, ProductPackageVO.class);
            vo.setBenefitList(listPackageBenefit(productPackage.getId()));
            return vo;
        }).toList();
    }

    /**
     * 根据套餐id查询套餐信息
     * @param id
     * @return
     */
    @Override
    public ProductPackageVO getPackageById(Long id) {
        LambdaQueryWrapper<ProductPackageDO> queryWrapper = new LambdaQueryWrapper<ProductPackageDO>().eq(ProductPackageDO::getId,id);
        ProductPackageVO productPackageVO = null;
        ProductPackageDO productPackageDO = productPackageMapper.selectOne(queryWrapper);
        if(productPackageDO != null) {
            productPackageVO = SpringBeanUtil.copyProperties(productPackageDO,ProductPackageVO.class);
        }
        return productPackageVO;
    }

    /**
     * 查出某个套餐包含哪些权益，次数来自关联表，名称价格等来自权益表
     */
    private List<PackageBenefitVO> listPackageBenefit(Long packageId) {
        List<PackageBenefitDO> relationList = packageBenefitMapper.selectList(
                new LambdaQueryWrapper<PackageBenefitDO>()
                        .eq(PackageBenefitDO::getProductPackageId, packageId)
        );
        if (relationList.isEmpty()) {
            return List.of();
        }

        List<String> benefitCodeList = new ArrayList<>();
        for (PackageBenefitDO relation : relationList) {
            benefitCodeList.add(relation.getBenefitCode());
        }
        List<BenefitDO> benefitList = benefitMapper.selectList(
                new LambdaQueryWrapper<BenefitDO>().in(BenefitDO::getBenefitCode, benefitCodeList)
        );
        Map<String, BenefitDO> benefitMap = new HashMap<>();
        for (BenefitDO benefit : benefitList) {
            benefitMap.put(benefit.getBenefitCode(), benefit);
        }

        List<PackageBenefitVO> resultList = new ArrayList<>();
        for (PackageBenefitDO relation : relationList) {
            BenefitDO benefit = benefitMap.get(relation.getBenefitCode());
            if (benefit == null) {
                continue;
            }
            PackageBenefitVO vo = SpringBeanUtil.copyProperties(benefit, PackageBenefitVO.class);
            vo.setCount(relation.getCount());
            resultList.add(vo);
        }
        return resultList;
    }
}
