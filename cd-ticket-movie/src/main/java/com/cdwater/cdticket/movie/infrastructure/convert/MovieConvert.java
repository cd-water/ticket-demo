package com.cdwater.cdticket.movie.infrastructure.convert;

import com.cdwater.cdticket.movie.application.dto.MovieOption;
import com.cdwater.cdticket.movie.application.dto.MovieSaveCommand;
import com.cdwater.cdticket.movie.application.dto.MovieVO;
import com.cdwater.cdticket.movie.infrastructure.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MovieConvert {

    MovieConvert INSTANCE = Mappers.getMapper(MovieConvert.class);

    MovieVO toVO(Movie movie);

    Movie toEntity(MovieSaveCommand command);

    MovieOption toOption(Movie movie);
}
