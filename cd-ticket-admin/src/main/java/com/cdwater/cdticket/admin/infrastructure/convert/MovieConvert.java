package com.cdwater.cdticket.admin.infrastructure.convert;

import com.cdwater.cdticket.admin.application.dto.MovieOption;
import com.cdwater.cdticket.admin.application.dto.MovieSaveCommand;
import com.cdwater.cdticket.admin.application.dto.MovieVO;
import com.cdwater.cdticket.admin.domain.entity.Movie;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface MovieConvert {

    MovieConvert INSTANCE = Mappers.getMapper(MovieConvert.class);

    MovieVO toVO(Movie movie);

    Movie toEntity(MovieSaveCommand command);

    MovieOption toOption(Movie movie);
}
