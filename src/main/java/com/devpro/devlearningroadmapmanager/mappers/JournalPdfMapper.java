package com.devpro.devlearningroadmapmanager.mappers;

import com.devpro.devlearningroadmapmanager.dtos.JournalPdfDto;
import com.devpro.devlearningroadmapmanager.entities.JournalPDF;
import org.mapstruct.*;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = MappingConstants.ComponentModel.SPRING)
public interface JournalPdfMapper {

    JournalPDF toEntity(JournalPdfDto.JournalPdfSaveDto request);

    JournalPdfDto toDto(JournalPDF journalPdf);

    List<JournalPdfDto> toDtoList(List<JournalPDF> journals);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    JournalPDF partialUpdate(JournalPdfDto.JournalPdfSaveDto request, @MappingTarget JournalPDF journalPdf);
}