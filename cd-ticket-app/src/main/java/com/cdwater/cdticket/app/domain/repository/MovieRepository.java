package com.cdwater.cdticket.app.domain.repository;

import com.cdwater.cdticket.app.application.dto.BoxOfficeVO;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.domain.model.Movie;

import java.util.Collection;
import java.util.List;

public interface MovieRepository {

    /** 热映：上架且已上映，按上映日期倒序，最多 limit 部 */
    List<Movie> findHot(int limit);

    /** 待映：上架且未上映，按上映日期升序，最多 limit 部 */
    List<Movie> findComing(int limit);

    /** 分页（showStatus: hot/coming） */
    PageResult<Movie> page(String showStatus, int page, int size);

    Movie findById(Long id);

    List<Movie> findByIds(Collection<Long> ids);

    /** 今日票房榜 top 10 */
    List<BoxOfficeVO> boxOffice();
}
