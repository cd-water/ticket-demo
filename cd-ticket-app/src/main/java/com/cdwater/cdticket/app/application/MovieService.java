package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.BoxOfficeVO;
import com.cdwater.cdticket.app.application.dto.MovieDetailVO;
import com.cdwater.cdticket.app.application.dto.MovieVO;
import com.cdwater.cdticket.app.common.PageResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MovieService {

    /** 热映：上架且 release_date <= 今天，按上映日期倒序，最多 8 部 */
    public List<MovieVO> hot() {
        return List.of();
    }

    /** 待映：上架且 release_date > 今天，按上映日期升序，最多 8 部 */
    public List<MovieVO> coming() {
        return List.of();
    }

    /** 今日票房榜：Redis ZSet movie:boxoffice:yyyyMMdd 取今日 top 10（数组顺序即排名） */
    public List<BoxOfficeVO> boxOffice() {
        return List.of();
    }

    /** 电影列表分页：status=hot 按上映日期倒序 / coming 按上映日期升序 */
    public PageResult<MovieVO> page(String status, int page, int size) {
        return new PageResult<>(0, List.of(), page, size);
    }

    /** 电影详情；不存在或已下架抛 C201 */
    public MovieDetailVO detail(Long id) {
        return null;
    }
}
