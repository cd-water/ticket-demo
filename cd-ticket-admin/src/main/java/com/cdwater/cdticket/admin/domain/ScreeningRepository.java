package com.cdwater.cdticket.admin.domain;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.admin.domain.entity.Screening;

public interface ScreeningRepository {
    IPage<Screening> pageByMovieIdAndCinemaId(Page<Screening> page, Long movieId, Long cinemaId);
    Screening findById(Long id);
    Screening save(Screening screening);
    void deleteById(Long id);
}
