package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.cinema.CinemaSaveRequest;
import com.cdwater.cdticket.admin.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.entity.Cinema;
import com.cdwater.cdticket.admin.mapper.CinemaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CinemaService {
    private final CinemaMapper cinemaMapper;

    public PageResult<CinemaVO> page(int page, int size, String name, Integer status) {
        IPage<Cinema> p = cinemaMapper.selectPage(Page.of(page, size), new LambdaQueryWrapper<Cinema>()
                .like(name != null && !name.isBlank(), Cinema::getName, name)
                .eq(status != null, Cinema::getStatus, status)
                .orderByDesc(Cinema::getId));
        return PageResult.of(p.convert(CinemaService::toVO));
    }

    public void save(CinemaSaveRequest req) {
        Cinema cinema = toEntity(req);
        if (req.getId() == null) {
            cinemaMapper.insert(cinema);
        } else {
            if (cinemaMapper.selectById(req.getId()) == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            cinemaMapper.updateById(cinema);
        }
    }

    public CinemaVO getCinema(Long id) {
        Cinema cinema = cinemaMapper.selectById(id);
        if (cinema == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return toVO(cinema);
    }

    private static CinemaVO toVO(Cinema c) {
        CinemaVO v = new CinemaVO();
        v.setId(c.getId());
        v.setName(c.getName());
        v.setAddress(c.getAddress());
        v.setStatus(c.getStatus());
        v.setCreateTime(c.getCreateTime());
        v.setUpdateTime(c.getUpdateTime());
        return v;
    }

    private static Cinema toEntity(CinemaSaveRequest req) {
        Cinema c = new Cinema();
        c.setId(req.getId());
        c.setName(req.getName());
        c.setAddress(req.getAddress());
        c.setStatus(req.getStatus());
        return c;
    }
}
