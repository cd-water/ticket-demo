package com.cdwater.cdticket.cinema.application;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.cinema.application.dto.CinemaSaveCommand;
import com.cdwater.cdticket.cinema.application.dto.CinemaVO;
import com.cdwater.cdticket.cinema.domain.CinemaRepository;
import com.cdwater.cdticket.cinema.infrastructure.convert.CinemaConvert;
import com.cdwater.cdticket.cinema.infrastructure.entity.Cinema;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.ResultCode;
import com.cdwater.cdticket.common.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;

    public PageResult<CinemaVO> page(int page, int size, String name) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        var p = cinemaRepository.pageByName(Page.of(page, size), name);
        return PageResult.of(p.convert(CinemaConvert.INSTANCE::toVO));
    }

    public void create(CinemaSaveCommand command) {
        cinemaRepository.save(CinemaConvert.INSTANCE.toEntity(command));
    }

    public void update(Long id, CinemaSaveCommand command) {
        requireCinema(id);
        Cinema target = CinemaConvert.INSTANCE.toEntity(command);
        target.setId(id);
        cinemaRepository.save(target);
    }

    public void delete(Long id) {
        requireCinema(id);
        cinemaRepository.deleteById(id);
    }

    /** 供 admin 模块校验影院存在（创建影院管理员时）；不存在返回 null */
    public CinemaVO getCinema(Long id) {
        Cinema cinema = cinemaRepository.findById(id);
        return cinema == null ? null : CinemaConvert.INSTANCE.toVO(cinema);
    }

    private Cinema requireCinema(Long id) {
        Cinema cinema = cinemaRepository.findById(id);
        if (cinema == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return cinema;
    }
}
