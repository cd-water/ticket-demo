package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.convert.HallConvert;
import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.HallUsageMapper;
import com.cdwater.cdticket.admin.security.AdminAuthorizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallAdminService {

    private final HallMapper hallMapper;
    private final HallUsageMapper hallUsageMapper;
    private final AdminAuthorizer adminAuthorizer;

    public List<HallVO> list() {
        Long cinemaId = adminAuthorizer.currentAdmin().getCinemaId();
        return hallMapper.selectList(new LambdaQueryWrapper<Hall>()
                .eq(Hall::getCinemaId, cinemaId)
                .orderByAsc(Hall::getId)).stream().map(HallConvert.INSTANCE::toVO).toList();
    }

    public void create(HallSaveRequest req) {
        Long cinemaId = adminAuthorizer.currentAdmin().getCinemaId();
        Hall hall = HallConvert.INSTANCE.toEntity(req);
        hall.setCinemaId(cinemaId);
        hallMapper.insert(hall);
    }

    public void update(Long id, HallSaveRequest req) {
        Hall hall = requireHall(id);
        adminAuthorizer.requireScope(hall.getCinemaId());
        Hall target = HallConvert.INSTANCE.toEntity(req);
        target.setId(hall.getId());
        target.setCinemaId(hall.getCinemaId());
        hallMapper.updateById(target);
    }

    public void delete(Long id) {
        Hall hall = requireHall(id);
        adminAuthorizer.requireScope(hall.getCinemaId());
        if (hallUsageMapper.countByHallId(id) > 0) {
            throw new BizException(ResultCode.HALL_HAS_SCREENING);
        }
        hallMapper.deleteById(id);
    }

    /** 供 screening 模块校验影厅归属 */
    public HallVO getHall(Long id) {
        Hall hall = hallMapper.selectById(id);
        return hall == null ? null : HallConvert.INSTANCE.toVO(hall);
    }

    private Hall requireHall(Long id) {
        Hall hall = hallMapper.selectById(id);
        if (hall == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return hall;
    }
}
