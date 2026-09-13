package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.BannerConvert;
import com.cdwater.cdticket.admin.dto.banner.BannerSaveRequest;
import com.cdwater.cdticket.admin.dto.banner.BannerVO;
import com.cdwater.cdticket.admin.entity.Banner;
import com.cdwater.cdticket.admin.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerAdminService {

    private final BannerMapper bannerMapper;

    public List<BannerVO> list() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .orderByAsc(Banner::getSort).orderByDesc(Banner::getId))
                .stream().map(BannerConvert.INSTANCE::toVO).toList();
    }

    public void create(BannerSaveRequest req) {
        bannerMapper.insert(BannerConvert.INSTANCE.toEntity(req));
    }

    public void update(Long id, BannerSaveRequest req) {
        if (bannerMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        Banner target = BannerConvert.INSTANCE.toEntity(req);
        target.setId(id);
        bannerMapper.updateById(target);
    }

    public void delete(Long id) {
        if (bannerMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        bannerMapper.deleteById(id);
    }
}
