package com.ens.hhparser5.mappers;

import com.ens.hhparser5.dto.VacancyDto;
import com.ens.hhparser5.model.Vacancy;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface VacancyToDtoMapper {

    VacancyToDtoMapper INSTANCE = Mappers.getMapper(VacancyToDtoMapper.class);
    VacancyDto toDto(Vacancy vacancy);


}
