package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.movie.MovieOption;
import com.cdwater.cdticket.admin.application.dto.movie.MovieSaveCommand;
import com.cdwater.cdticket.admin.application.dto.movie.MovieVO;
import com.cdwater.cdticket.admin.infrastructure.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MovieConvert {

    MovieConvert INSTANCE = Mappers.getMapper(MovieConvert.class);

    MovieVO toVO(Movie movie);

    Movie toEntity(MovieSaveCommand command);

    MovieOption toOption(Movie movie);
}
