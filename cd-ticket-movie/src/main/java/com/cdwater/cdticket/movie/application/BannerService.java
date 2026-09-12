package com.cdwater.cdticket.movie.application;

import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.common.exception.BizException;
import com.cdwater.cdticket.movie.application.dto.BannerSaveCommand;
import com.cdwater.cdticket.movie.application.dto.BannerVO;
import com.cdwater.cdticket.movie.domain.BannerRepository;
import com.cdwater.cdticket.movie.infrastructure.convert.BannerConvert;
import com.cdwater.cdticket.movie.infrastructure.entity.Banner;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BannerService {

    private final BannerRepository bannerRepository;

    public List<BannerVO> list() {
        return bannerRepository.list().stream().map(BannerConvert.INSTANCE::toVO).toList();
    }

    public void create(BannerSaveCommand command) {
        bannerRepository.save(BannerConvert.INSTANCE.toEntity(command));
    }

    public void update(Long id, BannerSaveCommand command) {
        if (bannerRepository.findById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        Banner target = BannerConvert.INSTANCE.toEntity(command);
        target.setId(id);
        bannerRepository.save(target);
    }

    public void delete(Long id) {
        if (bannerRepository.findById(id) == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        bannerRepository.deleteById(id);
    }
}
