package com.ens.hhparser5.dto;

import lombok.*;


@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VacancyDto {
    private long id;
    private String hhid;
    private String name;
    private long employer;
    private int salary_from;
    private int salary_to;
    private int gross;
    private String url;
    private String alternate_url;
    private int archived;
}
