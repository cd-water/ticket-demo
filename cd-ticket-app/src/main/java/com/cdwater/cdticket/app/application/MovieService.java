package com.cdwater.cdticket.app.application;

import com.cdwater.cdticket.app.application.dto.BoxOfficeVO;
import com.cdwater.cdticket.app.application.dto.MovieDetailVO;
import com.cdwater.cdticket.app.application.dto.MovieVO;
import com.cdwater.cdticket.app.common.PageResult;
import com.cdwater.cdticket.app.common.ResultCode;
import com.cdwater.cdticket.app.common.exception.BizException;
import com.cdwater.cdticket.app.domain.model.Movie;
import com.cdwater.cdticket.app.domain.repository.MovieRepository;
import com.cdwater.cdticket.app.infrastructure.service.CacheService;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private static final int TOP_LIMIT = 8;
    private static final long LIST_CACHE_TTL = 60;
    private static final long DETAIL_CACHE_TTL = 300;
    private static final String BLOOM_MOVIE = "bloom:movie";
    private static final String BOX_KEY = "box:office:today";

    private final MovieRepository movieRepository;
    private final CacheService cacheService;
    private final StringRedisTemplate redis;

    private static MovieVO toVO(Movie m) {
        MovieVO vo = new MovieVO();
        vo.setId(m.getId());
        vo.setTitle(m.getTitle());
        vo.setPoster(m.getPoster());
        return vo;
    }

    /** 热映：逻辑过期缓存 + 互斥锁异步重建 */
    public List<MovieVO> hot() {
        return cacheService.getOrLoad("movies:hot", LIST_CACHE_TTL,
                () -> movieRepository.findHot(TOP_LIMIT).stream().map(MovieService::toVO).toList(),
                new TypeReference<>() {});
    }

    /** 待映 */
    public List<MovieVO> coming() {
        return cacheService.getOrLoad("movies:coming", LIST_CACHE_TTL,
                () -> movieRepository.findComing(TOP_LIMIT).stream().map(MovieService::toVO).toList(),
                new TypeReference<>() {});
    }

    /** 今日票房榜：读 Redis ZSet（Kafka 异步累计），空则 DB 聚合回填 */
    public List<BoxOfficeVO> boxOffice() {
        Set<org.springframework.data.redis.core.ZSetOperations.TypedTuple<String>> top =
                redis.opsForZSet().reverseRangeWithScores(BOX_KEY, 0, 9);
        if (top == null || top.isEmpty()) {
            List<BoxOfficeVO> fromDb = movieRepository.boxOffice();
            fromDb.forEach(b -> redis.opsForZSet().incrementScore(BOX_KEY,
                    String.valueOf(b.getId()), b.getBoxOffice().doubleValue()));
            expireAtDayEnd();
            return fromDb;
        }
        List<Long> ids = top.stream().map(t -> Long.valueOf(t.getValue())).toList();
        Map<Long, String> titles = movieRepository.findByIds(ids).stream()
                .collect(Collectors.toMap(Movie::getId, Movie::getTitle));
        return top.stream().map(t -> {
            BoxOfficeVO vo = new BoxOfficeVO();
            vo.setId(Long.valueOf(t.getValue()));
            vo.setTitle(titles.getOrDefault(vo.getId(), "未知电影"));
            vo.setBoxOffice(BigDecimal.valueOf(t.getScore()));
            return vo;
        }).toList();
    }

    /** 电影列表分页：showStatus=hot 按上映日期倒序 / coming 按上映日期升序 */
    public PageResult<MovieVO> page(String showStatus, int page, int size) {
        return cacheService.getOrLoad("movies:" + showStatus + ":" + page + ":" + size, LIST_CACHE_TTL, () -> {
            PageResult<Movie> p = movieRepository.page(showStatus, page, size);
            return new PageResult<>(p.getTotal(), p.getRecords().stream().map(MovieService::toVO).toList(),
                    p.getPage(), p.getSize());
        }, new TypeReference<>() {});
    }

    /**
     * 电影详情：布隆过滤 + 空值缓存防穿透；不存在或已下架返回 C004。
     * 布隆无位时兜底查 DB（命中则回填布隆位），保证不误拒真实存在的电影。
     */
    public MovieDetailVO detail(Long id) {
        if (!cacheService.bloomContains(BLOOM_MOVIE, id)) {
            Movie m = movieRepository.findById(id);
            if (m == null || m.getStatus() == null || m.getStatus() != 1) {
                throw new BizException(ResultCode.NOT_FOUND);
            }
            cacheService.bloomAdd(BLOOM_MOVIE, id);
            return toDetailVO(m);
        }
        MovieDetailVO vo = cacheService.getOrLoad("movie:detail:" + id, DETAIL_CACHE_TTL, () -> {
            Movie m = movieRepository.findById(id);
            if (m == null || m.getStatus() == null || m.getStatus() != 1) {
                return null; // 缓存空值，防穿透
            }
            cacheService.bloomAdd(BLOOM_MOVIE, id);
            return toDetailVO(m);
        }, new TypeReference<>() {});
        if (vo == null) {
            throw new BizException(ResultCode.NOT_FOUND);
        }
        return vo;
    }

    private static MovieDetailVO toDetailVO(Movie m) {
        MovieDetailVO vo = new MovieDetailVO();
        vo.setId(m.getId());
        vo.setTitle(m.getTitle());
        vo.setPoster(m.getPoster());
        vo.setDescription(m.getDescription());
        vo.setDuration(m.getDuration());
        vo.setReleaseDate(m.getReleaseDate());
        vo.setShowStatus(m.getReleaseDate().isAfter(LocalDate.now()) ? "coming" : "hot");
        return vo;
    }

    private void expireAtDayEnd() {
        long expireAt = LocalDate.now().plusDays(1).atStartOfDay()
                .plusMinutes(5).atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        redis.expireAt(BOX_KEY, java.time.Instant.ofEpochSecond(expireAt));
    }
}
