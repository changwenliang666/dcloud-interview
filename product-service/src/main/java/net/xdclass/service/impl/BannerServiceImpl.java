package net.xdclass.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import net.xdclass.vo.BannerDTO;
import net.xdclass.enums.BizCodeEnum;
import net.xdclass.exception.BizException;
import net.xdclass.mapper.BannerMapper;
import net.xdclass.model.BannerDO;
import net.xdclass.service.BannerService;
import net.xdclass.util.SpringBeanUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Banner 查询服务实现
 */
@Service
@Slf4j
public class BannerServiceImpl implements BannerService {

    @Resource
    private BannerMapper bannerMapper;

    @Override
    public BannerDTO findById(Long id) {
        BannerDO bannerDO = bannerMapper.selectById(id);
        if (bannerDO == null) {
            log.warn("Banner不存在, id:{}", id);
            throw new BizException(BizCodeEnum.BANNER_NOT_EXIST);
        }
        return convertToDTO(bannerDO);
    }

    @Override
    public List<BannerDTO> list(String location, String status) {
        LambdaQueryWrapper<BannerDO> queryWrapper = new LambdaQueryWrapper<BannerDO>()
                .eq(StringUtils.hasText(location), BannerDO::getLocation, location)
                .eq(StringUtils.hasText(status), BannerDO::getStatus, status)
                .orderByDesc(BannerDO::getGmtCreate);
        List<BannerDO> bannerList = bannerMapper.selectList(queryWrapper);
        log.info("查询Banner列表, location:{}, status:{}, size:{}", location, status, bannerList.size());
        return bannerList.stream().map(this::convertToDTO).toList();
    }

    private BannerDTO convertToDTO(BannerDO bannerDO) {
        return SpringBeanUtil.copyProperties(bannerDO, BannerDTO.class);
    }
}
