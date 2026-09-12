package com.cdwater.cdticket.screening.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cdwater.cdticket.screening.domain.ScreeningRepository;
import com.cdwater.cdticket.screening.infrastructure.entity.Screening;
import com.cdwater.cdticket.screening.infrastructure.mapper.ScreeningMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ScreeningRepositoryImpl implements ScreeningRepository {

    private final ScreeningMapper screeningMapper;

    @Override
    public IPage<Screening> pageByMovieIdAndCinemaId(Page<Screening> page, Long movieId, Long cinemaId) {
        return screeningMapper.selectPage(page, new LambdaQueryWrapper<Screening>()
                .eq(movieId != null, Screening::getMovieId, movieId)
                .eq(Screening::getCinemaId, cinemaId)
                .orderByDesc(Screening::getStartTime));
    }

    @Override
    public Screening findById(Long id) {
        return screeningMapper.selectById(id);
    }

    @Override
    public Screening save(Screening screening) {
        if (screening.getId() == null) {
            screeningMapper.insert(screening);
        } else {
            screeningMapper.updateById(screening);
        }
        return screening;
    }

    @Override
    public void deleteById(Long id) {
        screeningMapper.deleteById(id);
    }
}
