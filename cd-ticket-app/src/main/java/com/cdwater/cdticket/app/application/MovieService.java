package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.BoxOfficeVO;
import com.cdwater.cdticket.app.application.dto.MovieDetailVO;
import com.cdwater.cdticket.app.application.dto.MovieVO;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.exception.BizException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class MovieService {

    // ponytail: 内存 mock,数据重启即丢;接入 t_movie 表后改为 repository 查询;hot/coming 的过滤条件上映日期判定走 DB
    private static final Map<Long, MovieDetailVO> MOCK_MOVIES = new LinkedHashMap<>();

    static {
        // hot 电影（已上映）：id 1-4
        register(1L, "流浪地球 3", "https://cdn.example.com/poster1.jpg",
                "太阳危机后的第 37 年,地球踏上了新的航程……", 173,
                LocalDate.of(2026, 5, 1), "hot");
        register(2L, "深海传说", "https://cdn.example.com/poster2.jpg",
                "海底两万里的冒险,一段跨越种族与时间的友谊。", 128,
                LocalDate.of(2026, 6, 18), "hot");
        register(3L, "长安三万里", "https://cdn.example.com/poster3.jpg",
                "大唐盛世下的诗人与剑客,追忆诗酒趁年华的少年意气。", 168,
                LocalDate.of(2026, 7, 10), "hot");
        register(4L, "熊出没·重启", "https://cdn.example.com/poster4.jpg",
                "光头强和熊兄弟踏上全新征程,守护森林的秘密。", 95,
                LocalDate.of(2026, 8, 1), "hot");
        // coming 待映：id 5-6
        register(5L, "千里江山图", "https://cdn.example.com/poster5.jpg",
                "北宋天才少年王希孟的传世名作背后的故事。", 142,
                LocalDate.now().plusDays(14), "coming");
        register(6L, "星际拓荒者", "https://cdn.example.com/poster6.jpg",
                "人类文明迈向半人马座 α 的第一支先遣队。", 156,
                LocalDate.now().plusDays(30), "coming");
    }

    private static void register(Long id, String title, String poster, String desc,
                                 int duration, LocalDate releaseDate, String showStatus) {
        MovieDetailVO m = new MovieDetailVO();
        m.setId(id);
        m.setTitle(title);
        m.setPoster(poster);
        m.setDescription(desc);
        m.setDuration(duration);
        m.setReleaseDate(releaseDate);
        m.setShowStatus(showStatus);
        MOCK_MOVIES.put(id, m);
    }

    private static MovieVO toVO(MovieDetailVO m) {
        MovieVO vo = new MovieVO();
        vo.setId(m.getId());
        vo.setTitle(m.getTitle());
        vo.setPoster(m.getPoster());
        return vo;
    }

    /** 热映：上架且 release_date <= 今天，按 release_date 倒序，最多 8 部 */
    public List<MovieVO> hot() {
        return MOCK_MOVIES.values().stream()
                .filter(m -> "hot".equals(m.getShowStatus()))
                .sorted(Comparator.comparing(MovieDetailVO::getReleaseDate).reversed())
                .limit(8)
                .map(MovieService::toVO)
                .toList();
    }

    /** 待映：上架且 release_date > 今天，按 release_date 升序，最多 8 部 */
    public List<MovieVO> coming() {
        return MOCK_MOVIES.values().stream()
                .filter(m -> "coming".equals(m.getShowStatus()))
                .sorted(Comparator.comparing(MovieDetailVO::getReleaseDate))
                .limit(8)
                .map(MovieService::toVO)
                .toList();
    }

    /** 今日票房榜 top 10（数组顺序即排名；mock 数据固定写死） */
    public List<BoxOfficeVO> boxOffice() {
        BigDecimal[] box = {
                new BigDecimal("1284000.00"),
                new BigDecimal("960000.00"),
                new BigDecimal("612000.00"),
                new BigDecimal("435000.00")
        };
        List<MovieDetailVO> hotMovies = hot().stream()
                .map(v -> MOCK_MOVIES.get(v.getId()))
                .toList();
        List<BoxOfficeVO> list = new ArrayList<>();
        for (int i = 0; i < Math.min(box.length, hotMovies.size()); i++) {
            BoxOfficeVO vo = new BoxOfficeVO();
            vo.setId(hotMovies.get(i).getId());
            vo.setTitle(hotMovies.get(i).getTitle());
            vo.setBoxOffice(box[i]);
            list.add(vo);
        }
        return list;
    }

    /** 电影列表分页：status=hot 按上映日期倒序 / coming 按上映日期升序 */
    public PageResult<MovieVO> page(String status, int page, int size) {
        // ponytail: mock 数据量小,直接返回全集;接入 DB 后做 LIMIT/OFFSET
        List<MovieVO> all = "hot".equals(status) ? hot() : coming();
        return new PageResult<>((long) all.size(), all, page, size);
    }

    /** 电影详情；不存在或已下架抛 NOT_FOUND */
    public MovieDetailVO detail(Long id) {
        MovieDetailVO m = MOCK_MOVIES.get(id);
        if (m == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return m;
    }
}
