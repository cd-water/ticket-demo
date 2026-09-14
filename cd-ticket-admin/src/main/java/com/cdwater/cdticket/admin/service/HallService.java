package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.HallUsageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HallService {
    private final HallMapper hallMapper;
    private final HallUsageMapper hallUsageMapper;

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
            Hall hall = requireHall(req.getId());
            target.setCinemaId(hall.getCinemaId());
            hallMapper.updateById(target);
        }
    }

    public void delete(Long id) {
        requireHall(id);
        if (hallUsageMapper.countByHallId(id) > 0) {
            throw new BizException(ResultCode.HALL_HAS_SCREENING);
        }
        hallMapper.deleteById(id);
    }

    public HallVO getHall(Long id) {
        Hall hall = hallMapper.selectById(id);
        return hall == null ? null : toVO(hall);
    }

    public Map<Long, String> mapNamesByIds(Collection<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return hallMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Hall::getId, Hall::getName));
    }

    public Hall requireHall(Long id) {
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
        return h;
    }
}
