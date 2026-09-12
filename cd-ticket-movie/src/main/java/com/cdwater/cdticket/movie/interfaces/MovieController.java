package com.cdwater.cdticket.movie.interfaces;

import com.cdwater.cdticket.common.admin.AdminAuthorizer;
import com.cdwater.cdticket.common.api.PageResult;
import com.cdwater.cdticket.common.api.Result;
import com.cdwater.cdticket.movie.application.MovieService;
import com.cdwater.cdticket.movie.application.dto.MovieSaveCommand;
import com.cdwater.cdticket.movie.application.dto.MovieVO;
import com.cdwater.cdticket.movie.interfaces.dto.MovieSaveRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
    private final AdminAuthorizer adminAuthorizer;

    @GetMapping
    public Result<PageResult<MovieVO>> page(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) String title,
                                            @RequestParam(required = false) Integer status) {
        adminAuthorizer.requireSuperAdmin();
        return Result.success(movieService.page(page, size, title, status));
    }

    @PostMapping
    public Result<Void> create(@RequestBody @Valid MovieSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        movieService.create(toCommand(req));
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid MovieSaveRequest req) {
        adminAuthorizer.requireSuperAdmin();
        movieService.update(id, toCommand(req));
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        adminAuthorizer.requireSuperAdmin();
        movieService.delete(id);
        return Result.success();
    }

    private MovieSaveCommand toCommand(MovieSaveRequest req) {
        return new MovieSaveCommand(req.getTitle(), req.getPoster(), req.getDescription(),
                req.getDuration(), req.getReleaseDate(), req.getStatus());
    }
}
