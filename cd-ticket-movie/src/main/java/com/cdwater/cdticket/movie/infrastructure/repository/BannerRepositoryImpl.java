package com.cdwater.cdticket.movie.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.movie.domain.BannerRepository;
import com.cdwater.cdticket.movie.infrastructure.entity.Banner;
import com.cdwater.cdticket.movie.infrastructure.mapper.BannerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class BannerRepositoryImpl implements BannerRepository {

    private final BannerMapper bannerMapper;

    @Override
    public List<Banner> list() {
        return bannerMapper.selectList(new LambdaQueryWrapper<Banner>()
                .orderByAsc(Banner::getSort).orderByDesc(Banner::getId));
    }

    @Override
    public Banner findById(Long id) {
        return bannerMapper.selectById(id);
    }

    @Override
    public Banner save(Banner banner) {
        if (banner.getId() == null) {
            bannerMapper.insert(banner);
        } else {
            bannerMapper.updateById(banner);
        }
        return banner;
    }

    @Override
    public void deleteById(Long id) {
        bannerMapper.deleteById(id);
    }
}
