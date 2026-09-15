package com.cdwater.cdticket.app.infrastructure.config;

import com.cdwater.cdticket.app.application.CinemaService;
import com.cdwater.cdticket.app.application.MovieService;
import com.cdwater.cdticket.app.infrastructure.service.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 缓存预热：启动后预加载热门电影/影院列表并填充布隆位，避免首个用户请求打满冷启动 DB。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheWarmup implements ApplicationRunner {

    private final MovieService movieService;
    private final CinemaService cinemaService;
    private final CacheService cacheService;

    @Override
    public void run(ApplicationArguments args) {
        try {
            movieService.hot().forEach(m -> cacheService.bloomAdd("bloom:movie", m.getId()));
            movieService.coming().forEach(m -> cacheService.bloomAdd("bloom:movie", m.getId()));
            movieService.page("hot", 1, 10);
            movieService.page("coming", 1, 10);
            cinemaService.page(1, 10);
            log.info("cache warmup done");
        } catch (Exception e) {
            log.warn("cache warmup failed, will be warmed on demand", e);
        }
    }
}
