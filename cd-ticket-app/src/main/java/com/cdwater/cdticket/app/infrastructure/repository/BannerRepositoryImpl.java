package com.cdwater.cdticket.app.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.app.domain.model.Banner;
import com.cdwater.cdticket.app.domain.repository.BannerRepository;
import com.cdwater.cdticket.app.infrastructure.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepository {

    private final BannerMapper bannerMapper;

    @Override
    public List<Banner> listEnabled() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .eq(Banner::getStatus, 1)
                .orderByAsc(Banner::getSort));
    }
}
