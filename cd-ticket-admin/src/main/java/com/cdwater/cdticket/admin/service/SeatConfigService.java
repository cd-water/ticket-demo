package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.seat.SeatCellRequest;
import com.cdwater.cdticket.admin.dto.seat.SeatCellVO;
import com.cdwater.cdticket.admin.dto.seat.SeatGridVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.entity.SeatConfig;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.SeatConfigMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatConfigService {

    private final HallMapper hallMapper;
    private final SeatConfigMapper configMapper;

    public SeatGridVO getGrid(Long hallId) {
        Hall hall = requireHall(hallId);
        SecurityUtils.requireScope(hall.getCinemaId());
        List<SeatConfig> configs = configMapper.selectList(new LambdaQueryWrapper<SeatConfig>()
                .eq(SeatConfig::getHallId, hallId)
                .orderByAsc(SeatConfig::getSeatRow)
                .orderByAsc(SeatConfig::getSeatCol));
        List<SeatCellVO> cells = new ArrayList<>();
        for (int row = 1; row <= hall.getSeatRows(); row++) {
            final int r = row;
            for (int col = 1; col <= hall.getSeatCols(); col++) {
                final int c = col;
                int status = configs.stream()
                        .filter(s -> s.getSeatRow() == r && s.getSeatCol() == c)
                        .findFirst().map(SeatConfig::getStatus).orElse(1);
                cells.add(new SeatCellVO(r, c, r + "排" + c + "座", status));
            }
        }
        return new SeatGridVO(hall.getSeatRows(), hall.getSeatCols(), cells);
    }

    @Transactional
    public void replace(Long hallId, List<SeatCellRequest> seats) {
        Hall hall = requireHall(hallId);
        SecurityUtils.requireScope(hall.getCinemaId());
        for (SeatCellRequest cell : seats) {
            if (cell.getRow() < 1 || cell.getRow() > hall.getSeatRows()
                    || cell.getCol() < 1 || cell.getCol() > hall.getSeatCols()) {
                throw new BizException("座位坐标超出影厅范围（" + hall.getSeatRows() + "排" + hall.getSeatCols() + "座）",
                        ResultCode.BAD_REQUEST.getCode());
            }
            if (cell.getStatus() != 0 && cell.getStatus() != 1) {
                throw new BizException("座位状态非法", ResultCode.BAD_REQUEST.getCode());
            }
        }
        List<SeatConfig> entities = seats.stream().map(c -> {
            SeatConfig s = new SeatConfig();
            s.setHallId(hallId);
            s.setSeatRow(c.getRow());
            s.setSeatCol(c.getCol());
            s.setSeatNo(c.getRow() + "排" + c.getCol() + "座");
            s.setStatus(c.getStatus());
            return s;
        }).toList();
        configMapper.delete(new LambdaQueryWrapper<SeatConfig>().eq(SeatConfig::getHallId, hallId));
        for (SeatConfig seat : entities) {
            configMapper.insert(seat);
        }
    }

    private Hall requireHall(Long id) {
        Hall hall = hallMapper.selectById(id);
        if (hall == null) throw new BizException(ResultCode.NOT_FOUND);
        return hall;
    }

}
