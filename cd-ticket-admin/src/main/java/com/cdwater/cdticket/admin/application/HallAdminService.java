package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.dto.HallSaveCommand;
import com.cdwater.cdticket.admin.application.dto.HallVO;
import com.cdwater.cdticket.admin.domain.HallRepository;
import com.cdwater.cdticket.admin.infrastructure.convert.HallConvert;
import com.cdwater.cdticket.admin.domain.entity.Hall;
import com.cdwater.cdticket.admin.infrastructure.mapper.HallUsageMapper;
import com.cdwater.cdticket.admin.common.AdminAuthorizer;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class HallAdminService {

    private final HallRepository hallRepository;
    private final HallUsageMapper hallUsageMapper;
    private final AdminAuthorizer adminAuthorizer;

    public List<HallVO> list() {
        Long cinemaId = adminAuthorizer.currentAdmin().getCinemaId();
        return hallRepository.listByCinemaId(cinemaId).stream().map(HallConvert.INSTANCE::toVO).toList();
    }

    public void create(HallSaveCommand command) {
        Long cinemaId = adminAuthorizer.currentAdmin().getCinemaId();
        Hall hall = HallConvert.INSTANCE.toEntity(command);
        hall.setCinemaId(cinemaId);
        hallRepository.save(hall);
    }

    public void update(Long id, HallSaveCommand command) {
        Hall hall = requireHall(id);
        adminAuthorizer.requireScope(hall.getCinemaId());
        Hall target = HallConvert.INSTANCE.toEntity(command);
        target.setId(hall.getId());
        target.setCinemaId(hall.getCinemaId());
        hallRepository.save(target);
    }

    public void delete(Long id) {
        Hall hall = requireHall(id);
        adminAuthorizer.requireScope(hall.getCinemaId());
        if (hallUsageMapper.countByHallId(id) > 0) {
            throw new BizException(ResultCode.HALL_HAS_SCREENING);
        }
        hallRepository.deleteById(id);
    }

    /** 供 screening 模块校验影厅归属 */
    public HallVO getHall(Long id) {
        Hall hall = hallRepository.findById(id);
        return hall == null ? null : HallConvert.INSTANCE.toVO(hall);
    }

    private Hall requireHall(Long id) {
        Hall hall = hallRepository.findById(id);
        if (hall == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return hall;
    }
}
