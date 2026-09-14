package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.hall.SeatCellRequest;
import com.cdwater.cdticket.admin.dto.hall.SeatGridVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.entity.SeatConfig;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import com.cdwater.cdticket.admin.mapper.SeatConfigMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeatConfigServiceTest {

    @Mock
    private HallMapper hallMapper;
    @Mock
    private SeatConfigMapper configMapper;
    @InjectMocks
    private SeatConfigService seatConfigService;

    private Hall buildHall(int rows, int cols) {
        Hall h = new Hall();
        h.setId(1L);
        h.setSeatRows(rows);
        h.setSeatCols(cols);
        return h;
    }

    private SeatCellRequest buildCell(int row, int col, int status) {
        SeatCellRequest cell = new SeatCellRequest();
        cell.setRow(row);
        cell.setCol(col);
        cell.setStatus(status);
        return cell;
    }

    @Test
    void getGrid_hallNotFound_throws404() {
        when(hallMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> seatConfigService.getGrid(1L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void getGrid_noConfigs_defaultsAllToAvailable() {
        when(hallMapper.selectById(1L)).thenReturn(buildHall(2, 3));
        when(configMapper.selectList(any())).thenReturn(List.of());

        SeatGridVO grid = seatConfigService.getGrid(1L);

        assertThat(grid.getRows()).isEqualTo(2);
        assertThat(grid.getCols()).isEqualTo(3);
        assertThat(grid.getSeats()).hasSize(6);
        assertThat(grid.getSeats()).allMatch(s -> s.getStatus() == 1);
        assertThat(grid.getSeats().get(0).getSeatNo()).isEqualTo("1排1座");
        assertThat(grid.getSeats().get(5).getSeatNo()).isEqualTo("2排3座");
    }

    @Test
    void getGrid_appliesConfiguredStatus() {
        when(hallMapper.selectById(1L)).thenReturn(buildHall(2, 2));
        SeatConfig disabled = new SeatConfig();
        disabled.setSeatRow(1);
        disabled.setSeatCol(2);
        disabled.setStatus(0);
        when(configMapper.selectList(any())).thenReturn(List.of(disabled));

        SeatGridVO grid = seatConfigService.getGrid(1L);

        assertThat(cellStatus(grid, 1, 2)).isZero();
        assertThat(cellStatus(grid, 1, 1)).isEqualTo(1);
        assertThat(cellStatus(grid, 2, 2)).isEqualTo(1);
    }

    private int cellStatus(SeatGridVO grid, int row, int col) {
        return grid.getSeats().stream()
                .filter(s -> s.getRow() == row && s.getCol() == col)
                .findFirst()
                .orElseThrow()
                .getStatus();
    }

    @Test
    void replace_hallNotFound_throws404() {
        when(hallMapper.selectById(1L)).thenReturn(null);

        assertThatThrownBy(() -> seatConfigService.replace(1L, List.of()))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
    }

    @Test
    void replace_outOfRange_throwsBadRequest() {
        when(hallMapper.selectById(1L)).thenReturn(buildHall(2, 3));

        assertThatThrownBy(() -> seatConfigService.replace(1L, List.of(buildCell(3, 1, 1))))
                .isInstanceOf(BizException.class)
                .hasMessageContaining("座位坐标超出影厅范围")
                .extracting("code")
                .isEqualTo(400);
    }

    @Test
    void replace_invalidStatus_throwsBadRequest() {
        when(hallMapper.selectById(1L)).thenReturn(buildHall(2, 3));

        assertThatThrownBy(() -> seatConfigService.replace(1L, List.of(buildCell(1, 1, 5))))
                .isInstanceOf(BizException.class)
                .hasMessage("座位状态非法")
                .extracting("code")
                .isEqualTo(400);
        verify(configMapper, never()).delete(any());
    }

    @Test
    void replace_success_deletesThenInserts() {
        when(hallMapper.selectById(1L)).thenReturn(buildHall(2, 3));

        seatConfigService.replace(1L, List.of(buildCell(1, 1, 0), buildCell(2, 3, 1)));

        verify(configMapper).delete(any());
        ArgumentCaptor<List<SeatConfig>> captor = ArgumentCaptor.forClass(List.class);
        verify(configMapper).insert(captor.capture());
        List<SeatConfig> saved = captor.getValue();
        assertThat(saved).hasSize(2);

        SeatConfig first = saved.get(0);
        assertThat(first.getHallId()).isEqualTo(1L);
        assertThat(first.getSeatRow()).isEqualTo(1);
        assertThat(first.getSeatCol()).isEqualTo(1);
        assertThat(first.getSeatNo()).isEqualTo("1排1座");
        assertThat(first.getStatus()).isZero();

        SeatConfig second = saved.get(1);
        assertThat(second.getSeatRow()).isEqualTo(2);
        assertThat(second.getSeatCol()).isEqualTo(3);
        assertThat(second.getSeatNo()).isEqualTo("2排3座");
        assertThat(second.getStatus()).isEqualTo(1);
    }
}
