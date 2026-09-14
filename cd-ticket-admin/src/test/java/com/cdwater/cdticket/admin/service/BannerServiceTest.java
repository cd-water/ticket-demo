package com.cdwater.cdticket.admin.service;

import com.cdwater.cdticket.admin.common.exception.BizException;
import com.cdwater.cdticket.admin.dto.banner.BannerSaveRequest;
import com.cdwater.cdticket.admin.dto.banner.BannerVO;
import com.cdwater.cdticket.admin.entity.Banner;
import com.cdwater.cdticket.admin.mapper.BannerMapper;
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
class BannerServiceTest {

    @Mock
    private BannerMapper bannerMapper;
    @InjectMocks
    private BannerService bannerService;

    private BannerSaveRequest buildRequest(Long id) {
        BannerSaveRequest req = new BannerSaveRequest();
        req.setId(id);
        req.setImage("img.png");
        req.setLinkUrl("https://example.com");
        req.setSort(1);
        req.setStatus(1);
        return req;
    }

    @Test
    void list_mapsToVO() {
        Banner b = new Banner();
        b.setId(1L);
        b.setImage("img.png");
        b.setLinkUrl("https://example.com");
        b.setSort(3);
        b.setStatus(1);
        when(bannerMapper.selectList(any())).thenReturn(List.of(b));

        List<BannerVO> list = bannerService.list();

        assertThat(list).hasSize(1);
        BannerVO v = list.get(0);
        assertThat(v.getId()).isEqualTo(1L);
        assertThat(v.getImage()).isEqualTo("img.png");
        assertThat(v.getLinkUrl()).isEqualTo("https://example.com");
        assertThat(v.getSort()).isEqualTo(3);
        assertThat(v.getStatus()).isEqualTo(1);
    }

    @Test
    void save_create_inserts() {
        bannerService.save(buildRequest(null));
        verify(bannerMapper).insert(any(Banner.class));
        verify(bannerMapper, never()).updateById(any(Banner.class));
    }

    @Test
    void save_update_updatesExisting() {
        when(bannerMapper.selectById(5L)).thenReturn(new Banner());
        bannerService.save(buildRequest(5L));
        verify(bannerMapper).updateById(any(Banner.class));
        verify(bannerMapper, never()).insert(any(Banner.class));
    }

    @Test
    void save_update_notFound_throws404() {
        when(bannerMapper.selectById(5L)).thenReturn(null);

        assertThatThrownBy(() -> bannerService.save(buildRequest(5L)))
                .isInstanceOf(BizException.class)
                .extracting("code")
                .isEqualTo(404);
        verify(bannerMapper, never()).updateById(any(Banner.class));
    }
}
