package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallMapper hallMapper;

    public List<HallVO> listByCinema(Long cinemaId) {
        return hallMapper.selectList(new LambdaQueryWrapper<Hall>()
                .eq(Hall::getCinemaId, cinemaId)
                .orderByAsc(Hall::getId)).stream().map(HallService::toVO).toList();
    }

    public void save(HallSaveRequest req) {
        Hall target = toEntity(req);
        if (req.getId() == null) {
            hallMapper.insert(target);
        } else {
            if (hallMapper.selectById(req.getId()) == null) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            hallMapper.updateById(target);
        }
    }

    private static HallVO toVO(Hall h) {
        HallVO v = new HallVO();
        v.setId(h.getId());
        v.setCinemaId(h.getCinemaId());
        v.setName(h.getName());
        v.setSeatRows(h.getSeatRows());
        v.setSeatCols(h.getSeatCols());
        v.setStatus(h.getStatus());
        v.setCreateTime(h.getCreateTime());
        v.setUpdateTime(h.getUpdateTime());
        return v;
    }

    private static Hall toEntity(HallSaveRequest req) {
        Hall h = new Hall();
        h.setId(req.getId());
        h.setCinemaId(req.getCinemaId());
        h.setName(req.getName());
        h.setSeatRows(req.getSeatRows());
        h.setSeatCols(req.getSeatCols());
        h.setStatus(req.getStatus());
        return h;
    }
}
