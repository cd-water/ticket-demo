package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.CinemaDetailVO;
import com.cdwater.cdticket.app.application.dto.CinemaVO;
import com.cdwater.cdticket.app.application.dto.ScreeningVO;
import com.cdwater.cdticket.app.common.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CinemaService {

    /** 影院分页（停业/营业都返回，按 id 升序） */
    public PageResult<CinemaVO> page(int page, int size) {
        return new PageResult<>(0, List.of(), page, size);
    }

    /** 影院详情 + 正在排片的电影；不存在抛 C301 */
    public CinemaDetailVO detail(Long id) {
        return null;
    }

    /** 影院排片列表（可按电影筛选） */
    public List<ScreeningVO> screenings(Long cinemaId, Long movieId) {
        return List.of();
    }
}
