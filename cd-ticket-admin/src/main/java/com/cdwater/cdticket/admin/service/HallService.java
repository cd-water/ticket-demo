package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.HallUsageMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallService {

    private final HallMapper hallMapper;
    private final HallUsageMapper hallUsageMapper;

    public List<HallVO> list() {
        Long cinemaId = SecurityUtils.getCinemaId();
        return hallMapper.selectList(new LambdaQueryWrapper<Hall>()
                .eq(Hall::getCinemaId, cinemaId)
                .orderByAsc(Hall::getId)).stream().map(HallService::toVO).toList();
    }

    /** 新增/修改（id=null → 新增） */
    public void save(HallSaveRequest req) {
        Hall target = toEntity(req);
        if (req.getId() == null) {
            target.setCinemaId(SecurityUtils.getCinemaId());
            hallMapper.insert(target);
        } else {
            Hall hall = requireHall(req.getId());
            SecurityUtils.requireScope(hall.getCinemaId());
            target.setCinemaId(hall.getCinemaId());
            hallMapper.updateById(target);
        }
    }

    public void delete(Long id) {
        Hall hall = requireHall(id);
        SecurityUtils.requireScope(hall.getCinemaId());
        if (hallUsageMapper.countByHallId(id) > 0) {
            throw new BizException(ResultCode.HALL_HAS_SCREENING);
        }
        hallMapper.deleteById(id);
    }

    public HallVO getHall(Long id) {
        Hall hall = hallMapper.selectById(id);
        return hall == null ? null : toVO(hall);
    }

    private Hall requireHall(Long id) {
        Hall hall = hallMapper.selectById(id);
        if (hall == null) throw new BizException(ResultCode.NOT_FOUND);
        return hall;
    }

    private static HallVO toVO(Hall h) {
        HallVO v = new HallVO();
        v.setId(h.getId());
        v.setCinemaId(h.getCinemaId());
        v.setName(h.getName());
        v.setSeatRows(h.getSeatRows());
        v.setSeatCols(h.getSeatCols());
        v.setStatus(h.getStatus());
        return v;
    }

    private static Hall toEntity(HallSaveRequest req) {
        Hall h = new Hall();
        h.setId(req.getId());
        h.setName(req.getName());
        h.setSeatRows(req.getSeatRows());
        h.setSeatCols(req.getSeatCols());
        return h;
    }
}