package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.seat.SeatCellRequest;
import com.cdwater.cdticket.admin.dto.seat.SeatCellVO;
import com.cdwater.cdticket.admin.dto.seat.SeatGridVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.entity.SeatConfig;
import com.cdwater.cdticket.admin.mapper.SeatConfigMapper;
import com.cdwater.cdticket.admin.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeatConfigService {
    private final HallService hallService;
    private final SeatConfigMapper configMapper;

    public SeatGridVO getGrid(Long hallId) {
        Hall hall = hallService.requireHall(hallId);
        SecurityUtils.requireScope(hall.getCinemaId());
        List<SeatConfig> configs = configMapper.selectList(new LambdaQueryWrapper<SeatConfig>()
                .eq(SeatConfig::getHallId, hallId));
        Map<String, Integer> statusBySeat = configs.stream()
                .collect(Collectors.toMap(s -> s.getSeatRow() + "_" + s.getSeatCol(),
                        SeatConfig::getStatus, (a, b) -> a));
        List<SeatCellVO> cells = new ArrayList<>();
        for (int row = 1; row <= hall.getSeatRows(); row++) {
            for (int col = 1; col <= hall.getSeatCols(); col++) {
                int status = statusBySeat.getOrDefault(row + "_" + col, 1);
                cells.add(new SeatCellVO(row, col, row + "排" + col + "座", status));
            }
        }
        return new SeatGridVO(hall.getSeatRows(), hall.getSeatCols(), cells);
    }

    @Transactional
    public void replace(Long hallId, List<SeatCellRequest> seats) {
        Hall hall = hallService.requireHall(hallId);
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
        configMapper.deleteByHallId(hallId);
        configMapper.insert(entities);
    }
}
