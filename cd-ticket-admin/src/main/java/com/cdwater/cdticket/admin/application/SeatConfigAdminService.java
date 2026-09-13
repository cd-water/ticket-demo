package com.cdwater.cdticket.admin.application;

import com.cdwater.cdticket.admin.application.dto.SeatCellCommand;
import com.cdwater.cdticket.admin.application.dto.SeatCellVO;
import com.cdwater.cdticket.admin.application.dto.SeatGridVO;
import com.cdwater.cdticket.admin.domain.HallRepository;
import com.cdwater.cdticket.admin.domain.SeatConfigRepository;
import com.cdwater.cdticket.admin.domain.entity.Hall;
import com.cdwater.cdticket.admin.domain.entity.SeatConfig;
import com.cdwater.cdticket.admin.common.AdminAuthorizer;
import com.cdwater.cdticket.admin.common.ResultCode;
import com.cdwater.cdticket.admin.common.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatConfigAdminService {

    private final HallRepository hallRepository;
    private final SeatConfigRepository configRepository;
    private final AdminAuthorizer adminAuthorizer;

    /** 返回 rows×cols 全量网格；未配置的格子默认 status=1（启用） */
    public SeatGridVO getGrid(Long hallId) {
        Hall hall = requireHall(hallId);
        adminAuthorizer.requireScope(hall.getCinemaId());
        var configs = configRepository.listByHallId(hallId);
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

    /** 整体替换：事务内删旧插新（按当前 rows×cols 校验坐标） */
    @Transactional
    public void replace(Long hallId, List<SeatCellCommand> seats) {
        Hall hall = requireHall(hallId);
        adminAuthorizer.requireScope(hall.getCinemaId());
        for (SeatCellCommand cell : seats) {
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
        configRepository.deleteByHallId(hallId);
        configRepository.batchInsert(entities);
    }

    private Hall requireHall(Long id) {
        Hall hall = hallRepository.findById(id);
        if (hall == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return hall;
    }
}
