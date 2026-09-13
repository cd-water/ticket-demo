package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.banner.BannerSaveRequest;
import com.cdwater.cdticket.admin.dto.banner.BannerVO;
import com.cdwater.cdticket.admin.entity.Banner;
import com.cdwater.cdticket.admin.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {
    private final BannerMapper bannerMapper;

    public List<BannerVO> list() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .orderByAsc(Banner::getSort).orderByDesc(Banner::getId))
                .stream().map(BannerService::toVO).toList();
    }

    public void save(BannerSaveRequest req) {
        Banner banner = toEntity(req);
        if (req.getId() == null) {
            bannerMapper.insert(banner);
        } else {
            if (bannerMapper.selectById(req.getId()) == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            bannerMapper.updateById(banner);
        }
    }

    public void delete(Long id) {
        if (bannerMapper.selectById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        bannerMapper.deleteById(id);
    }

    private static BannerVO toVO(Banner b) {
        BannerVO v = new BannerVO();
        v.setId(b.getId());
        v.setImage(b.getImage());
        v.setLinkUrl(b.getLinkUrl());
        v.setSort(b.getSort());
        v.setStatus(b.getStatus());
        v.setCreateTime(b.getCreateTime());
        v.setUpdateTime(b.getUpdateTime());
        return v;
    }

    private static Banner toEntity(BannerSaveRequest req) {
        Banner b = new Banner();
        b.setId(req.getId());
        b.setImage(req.getImage());
        b.setLinkUrl(req.getLinkUrl());
        b.setSort(req.getSort());
        b.setStatus(req.getStatus());
        return b;
    }
}
