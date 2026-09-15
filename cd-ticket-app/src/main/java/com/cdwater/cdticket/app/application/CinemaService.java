package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.CinemaDetailVO;
import com.cdwater.cdticket.app.application.dto.CinemaVO;
import com.cdwater.cdticket.app.application.dto.MovieVO;
import com.cdwater.cdticket.app.application.dto.ScreeningVO;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.exception.BizException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class CinemaService {

    // ponytail: 内存 mock,数据重启即丢;接入 t_cinema/t_screening/t_movie 后改为 repository 查询
    private static final Map<Long, CinemaDetailVO> MOCK_CINEMAS = new LinkedHashMap<>();
    private static final Map<Long, List<ScreeningVO>> MOCK_SCREENINGS = new LinkedHashMap<>();

    static {
        cinema(1L, "CD-TICKET·天府广场店", "成都市锦江区人民东路10号", 1);
        cinema(2L, "CD-TICKET·春熙路店", "成都市锦江区春熙路88号", 1);
        cinema(3L, "CD-TICKET·宽窄巷子店", "成都市青羊区长顺上街127号", 0);

        screenings(1L, List.of(
                screening(1001L, 1L, 6L, "3号激光厅", "2026-09-15 14:20:00", "48.00"),
                screening(1002L, 1L, 1L, "1号巨幕厅", "2026-09-15 19:30:00", "58.00"),
                screening(1003L, 2L, 6L, "3号激光厅", "2026-09-15 20:00:00", "48.00"),
                screening(1004L, 5L, 2L, "2号杜比厅", "2026-09-16 10:00:00", "52.00")
        ));
        screenings(2L, List.of(
                screening(2001L, 1L, 1L, "1号巨幕厅", "2026-09-15 13:50:00", "58.00"),
                screening(2002L, 2L, 6L, "3号激光厅", "2026-09-15 16:40:00", "48.00"),
                screening(2003L, 4L, 2L, "2号杜比厅", "2026-09-15 19:10:00", "52.00")
        ));
        screenings(3L, List.of(
                screening(3001L, 3L, 6L, "3号激光厅", "2026-09-15 15:00:00", "48.00")
        ));
    }

    private static void cinema(Long id, String name, String address, Integer status) {
        CinemaDetailVO vo = new CinemaDetailVO();
        vo.setId(id);
        vo.setName(name);
        vo.setAddress(address);
        vo.setStatus(status);
        vo.setMovies(new ArrayList<>());
        MOCK_CINEMAS.put(id, vo);
    }

    private static void screenings(Long cinemaId, List<ScreeningVO> list) {
        MOCK_SCREENINGS.put(cinemaId, new ArrayList<>(list));
        CinemaDetailVO c = MOCK_CINEMAS.get(cinemaId);
        if (c == null) return;
        for (ScreeningVO s : list) {
            if (c.getMovies().stream().noneMatch(m -> m.getId().equals(s.getMovieId()))) {
                MovieVO m = new MovieVO();
                m.setId(s.getMovieId());
                m.setTitle(movieTitle(s.getMovieId()));
                m.setPoster("https://cdn.example.com/poster" + s.getMovieId() + ".jpg");
                c.getMovies().add(m);
            }
        }
    }

    private static ScreeningVO screening(Long id, Long movieId, Long hallId,
                                         String hallName, String startTimeStr, String price) {
        ScreeningVO s = new ScreeningVO();
        s.setId(id);
        s.setMovieId(movieId);
        s.setHallId(hallId);
        s.setHallName(hallName);
        s.setStartTime(LocalDateTime.parse(startTimeStr.replace(" ", "T")));
        s.setEndTime(s.getStartTime().plusHours(3));
        s.setPrice(new BigDecimal(price));
        return s;
    }

    // ponytail: 电影标题/海报与 MovieService 的 mock 重复,应来自 movie domain,当前未实现共享层
    private static String movieTitle(Long id) {
        return switch (id.intValue()) {
            case 1 -> "流浪地球 3";
            case 2 -> "深海传说";
            case 3 -> "长安三万里";
            case 4 -> "熊出没·重启";
            case 5 -> "千里江山图";
            case 6 -> "星际拓荒者";
            default -> "未知电影";
        };
    }

    private static CinemaVO toVO(CinemaDetailVO c) {
        CinemaVO vo = new CinemaVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setAddress(c.getAddress());
        vo.setStatus(c.getStatus());
        return vo;
    }

    /** 影院分页（停业/营业都返回，按 id 升序） */
    public PageResult<CinemaVO> page(int page, int size) {
        // ponytail: mock 数据量小,直接返回全集;接入 DB 后做 LIMIT/OFFSET
        List<CinemaVO> all = MOCK_CINEMAS.values().stream()
                .sorted(Comparator.comparing(CinemaDetailVO::getId))
                .map(CinemaService::toVO)
                .toList();
        return new PageResult<>((long) all.size(), all, page, size);
    }

    /** 影院详情；不存在抛 NOT_FOUND */
    public CinemaDetailVO detail(Long id) {
        CinemaDetailVO c = MOCK_CINEMAS.get(id);
        if (c == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return c;
    }

    /** 影院排片列表（可按 movieId 过滤） */
    public List<ScreeningVO> screenings(Long cinemaId, Long movieId) {
        if (!MOCK_CINEMAS.containsKey(cinemaId)) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        List<ScreeningVO> all = MOCK_SCREENINGS.getOrDefault(cinemaId, List.of());
        if (movieId == null) return all;
        return all.stream().filter(s -> s.getMovieId().equals(movieId)).toList();
    }
}
