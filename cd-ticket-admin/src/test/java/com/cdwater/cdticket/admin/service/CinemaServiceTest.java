package com.cdwater.cdticket.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.common.PageResult;
import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.cinema.CinemaSaveRequest;
import com.cdwater.cdticket.admin.dto.cinema.CinemaVO;
import com.cdwater.cdticket.admin.entity.Cinema;
import com.cdwater.cdticket.admin.mapper.CinemaMapper;
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
class CinemaServiceTest {

    @Mock
    private CinemaMapper cinemaMapper;
    @InjectMocks
    private CinemaService cinemaService;

    private CinemaSaveRequest buildRequest(Long id) {
        CinemaSaveRequest req = new CinemaSaveRequest();
        req.setId(id);
        req.setName("万达影城");
        req.setAddress("朝阳区");
        req.setStatus(1);
        return req;
    }

    @Test
    void page_returnsConvertedPage() {
        Cinema c = new Cinema();
        c.setId(1L);
        c.setName("万达影城");
        c.setAddress("朝阳区");
        c.setStatus(1);
        Page<Cinema> cinemaPage = new Page<>(1, 10);
        cinemaPage.setTotal(1);
        cinemaPage.setRecords(List.of(c));
        when(cinemaMapper.selectPage(any(), any())).thenReturn(cinemaPage);

        PageResult<CinemaVO> pr = cinemaService.page(1, 10, "万达", 1);

        assertThat(pr.getTotal()).isEqualTo(1);
        assertThat(pr.getPage()).isEqualTo(1);
        assertThat(pr.getSize()).isEqualTo(10);
        assertThat(pr.getRecords()).hasSize(1);
        assertThat(pr.getRecords().get(0).getName()).isEqualTo("万达影城");
        assertThat(pr.getRecords().get(0).getAddress()).isEqualTo("朝阳区");
    }

    @Test
    void save_create_inserts() {
        cinemaService.save(buildRequest(null));
        verify(cinemaMapper).insert(any(Cinema.class));
        verify(cinemaMapper, never()).updateById(any(Cinema.class));
    }

    @Test
    void save_update_updatesExisting() {
        when(cinemaMapper.selectById(5L)).thenReturn(new Cinema());
        cinemaService.save(buildRequest(5L));
        verify(cinemaMapper).updateById(any(Cinema.class));
        verify(cinemaMapper, never()).insert(any(Cinema.class));
    }

    @Test
    void save_update_notFound_throws404() {
        when(cinemaMapper.selectById(5L)).thenReturn(null);

        assertThatThrownBy(() -> cinemaService.save(buildRequest(5L)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
        verify(cinemaMapper, never()).updateById(any(Cinema.class));
    }

    @Test
    void getCinema_mapsToVO() {
        Cinema c = new Cinema();
        c.setId(9L);
        c.setName("万达影城");
        c.setAddress("朝阳区");
        c.setStatus(1);
        when(cinemaMapper.selectById(9L)).thenReturn(c);

        CinemaVO v = cinemaService.getCinema(9L);

        assertThat(v.getId()).isEqualTo(9L);
        assertThat(v.getName()).isEqualTo("万达影城");
        assertThat(v.getAddress()).isEqualTo("朝阳区");
        assertThat(v.getStatus()).isEqualTo(1);
    }

    @Test
    void getCinema_notFound_throws404() {
        when(cinemaMapper.selectById(9L)).thenReturn(null);

        assertThatThrownBy(() -> cinemaService.getCinema(9L))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
    }
}
