package com.cdwater.cdticket.app.domain.repository;

import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.domain.model.Cinema;
import com.cdwater.cdticket.app.domain.model.Movie;
import com.cdwater.cdticket.app.domain.model.Screening;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface CinemaRepository {

    /** 影院分页（停业/营业都返回，按 id 升序） */
    PageResult<Cinema> page(int page, int size);

    Cinema findById(Long id);

    /** 影院排片（按开场时间升序），movieId 可空 */
    List<Screening> findScreenings(Long cinemaId, Long movieId);

    /** 影院正在排片的电影（去重，按排片时间先后） */
    List<Movie> findMoviesByCinema(Long cinemaId);

    /** 影厅名称（id -> name） */
    Map<Long, String> findHallNames(Collection<Long> hallIds);
}
