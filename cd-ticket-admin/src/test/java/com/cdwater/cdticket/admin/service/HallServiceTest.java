package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.hall.HallSaveRequest;
import com.cdwater.cdticket.admin.dto.hall.HallVO;
import com.cdwater.cdticket.admin.entity.Hall;
import com.cdwater.cdticket.admin.mapper.HallMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class HallServiceTest {

    @Mock
    private HallMapper hallMapper;
    @InjectMocks
    private HallService hallService;

    private HallSaveRequest buildRequest(Long id) {
        HallSaveRequest req = new HallSaveRequest();
        req.setId(id);
        req.setCinemaId(3L);
        req.setName("1号厅");
        req.setSeatRows(8);
        req.setSeatCols(10);
        req.setStatus(1);
        return req;
    }

    @Test
    void listByCinema_mapsToVO() {
        Hall h = new Hall();
        h.setId(1L);
        h.setCinemaId(3L);
        h.setName("1号厅");
        h.setSeatRows(8);
        h.setSeatCols(10);
        h.setStatus(1);
        when(hallMapper.selectList(any())).thenReturn(List.of(h));

        List<HallVO> list = hallService.listByCinema(3L);

        assertThat(list).hasSize(1);
        HallVO v = list.get(0);
        assertThat(v.getId()).isEqualTo(1L);
        assertThat(v.getCinemaId()).isEqualTo(3L);
        assertThat(v.getName()).isEqualTo("1号厅");
        assertThat(v.getSeatRows()).isEqualTo(8);
        assertThat(v.getSeatCols()).isEqualTo(10);
        assertThat(v.getStatus()).isEqualTo(1);
    }

    @Test
    void save_create_inserts() {
        hallService.save(buildRequest(null));
        verify(hallMapper).insert(any(Hall.class));
        verify(hallMapper, never()).updateById(any(Hall.class));
    }

    @Test
    void save_update_updatesExisting() {
        when(hallMapper.selectById(5L)).thenReturn(new Hall());
        hallService.save(buildRequest(5L));
        verify(hallMapper).updateById(any(Hall.class));
        verify(hallMapper, never()).insert(any(Hall.class));
    }

    @Test
    void save_update_notFound_throws404() {
        when(hallMapper.selectById(5L)).thenReturn(null);

        assertThatThrownBy(() -> hallService.save(buildRequest(5L)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
    }
}
