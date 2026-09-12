package com.cdwater.cdticket.cinema.application;

import com.cdwater.cdticket.cinema.application.dto.SeatCellCommand;
import com.cdwater.cdticket.cinema.application.dto.SeatGridVO;
import com.cdwater.cdticket.cinema.domain.HallRepository;
import com.cdwater.cdticket.cinema.domain.SeatConfigRepository;
import com.cdwater.cdticket.cinema.infrastructure.entity.Hall;
import com.cdwater.cdticket.cinema.infrastructure.entity.SeatConfig;
import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SeatConfigServiceTest {

    private HallRepository hallRepository;
    private SeatConfigRepository configRepository;
    private AdminAuthorizer authorizer;
    private SeatConfigService service;

    @BeforeEach
    void setUp() {
        hallRepository = mock(HallRepository.class);
        configRepository = mock(SeatConfigRepository.class);
        authorizer = mock(AdminAuthorizer.class);
        service = new SeatConfigService(hallRepository, configRepository, authorizer);
    }

    @Test
    void getGridDefaultsMissingCellsToSellable() {
        Hall hall = new Hall(1L, 5L, "1号厅", 2, 2, 1, 0, null, null);
        when(hallRepository.findById(1L)).thenReturn(hall);
        when(configRepository.listByHallId(1L)).thenReturn(List.of(
                new SeatConfig(1L, 1L, 1, 1, "1排1座", 1, 0, null, null)));

        SeatGridVO grid = service.getGrid(1L);

        assertEquals(2, grid.getRows());
        assertEquals(2, grid.getCols());
        assertEquals(4, grid.getSeats().size());
        assertEquals(1, grid.getSeats().get(0).getStatus());
        assertEquals(0, grid.getSeats().get(1).getStatus()); // 未配置默认可售
    }

    @Test
    void replaceRejectsOutOfBounds() {
        Hall hall = new Hall(1L, 5L, "1号厅", 2, 2, 1, 0, null, null);
        when(hallRepository.findById(1L)).thenReturn(hall);
        assertThrows(BizException.class, () -> service.replace(1L,
                List.of(new SeatCellCommand(3, 1, 0)))); // 第3排超出2排
    }

    @Test
    void replaceDeletesThenInserts() {
        Hall hall = new Hall(1L, 5L, "1号厅", 2, 2, 1, 0, null, null);
        when(hallRepository.findById(1L)).thenReturn(hall);
        service.replace(1L, List.of(
                new SeatCellCommand(1, 1, 0), new SeatCellCommand(1, 2, 1)));
        verify(configRepository).deleteByHallId(1L);
        verify(configRepository).batchInsert(argThat(list -> list.size() == 2
                && list.get(0).getSeatNo().equals("1排1座")
                && list.get(1).getStatus() == 1));
    }
}
