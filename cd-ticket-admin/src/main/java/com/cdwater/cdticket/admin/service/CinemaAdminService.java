package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.CinemaConvert;
import com.cdwater.cdticket.admin.dto.cinema.CinemaSaveRequest;
import com.cdwater.cdticket.admin.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.entity.Cinema;
import com.cdwater.cdticket.admin.mapper.CinemaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CinemaAdminService {

    private final CinemaMapper cinemaMapper;

    public PageResult<CinemaVO> page(int page, int size, String name) {
        if (page < 1 || size < 1 || size > 100) {
            throw new BizException(ResultCode.BAD_REQUEST);
        }
        var p = cinemaMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Cinema>()
                .like(name != null && !name.isBlank(), Cinema::getName, name)
                .orderByDesc(Cinema::getCreateTime));
        return PageResult.of(p.convert(CinemaConvert.INSTANCE::toVO));
    }

    public void create(CinemaSaveRequest req) {
        cinemaMapper.insert(CinemaConvert.INSTANCE.toEntity(req));
    }

    public void update(Long id, CinemaSaveRequest req) {
        requireCinema(id);
        Cinema target = CinemaConvert.INSTANCE.toEntity(req);
        target.setId(id);
        cinemaMapper.updateById(target);
    }

    public void delete(Long id) {
        requireCinema(id);
        cinemaMapper.deleteById(id);
    }

    /** 仅平台管理员可用，返回 {id, name} 列表 */
    public List<CinemaVO> listAll() {
        return cinemaMapper.selectList(new LambdaQueryWrapper<Cinema>().orderByAsc(Cinema::getId))
                .stream().map(CinemaConvert.INSTANCE::toVO).toList();
    }

    /** 供 admin 模块校验影院存在（创建影院管理员时）；不存在返回 null */
    public CinemaVO getCinema(Long id) {
        Cinema cinema = cinemaMapper.selectById(id);
        return cinema == null ? null : CinemaConvert.INSTANCE.toVO(cinema);
    }

    private Cinema requireCinema(Long id) {
        Cinema cinema = cinemaMapper.selectById(id);
        if (cinema == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return cinema;
    }
}
