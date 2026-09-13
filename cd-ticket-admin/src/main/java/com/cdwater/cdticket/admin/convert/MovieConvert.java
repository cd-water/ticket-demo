package com.cdwater.cdticket.admin.convert;

import com.cdwater.cdticket.admin.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.dto.movie.MovieSaveRequest;
import com.cdwater.cdticket.admin.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MovieConvert {

    MovieConvert INSTANCE = Mappers.getMapper(MovieConvert.class);

    MovieVO toVO(Movie movie);

    Movie toEntity(MovieSaveRequest command);

    MovieOption toOption(Movie movie);
}
