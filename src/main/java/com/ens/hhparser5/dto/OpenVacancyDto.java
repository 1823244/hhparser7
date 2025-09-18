package com.ens.hhparser5.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OpenVacancyDto {
    private long id;
    private String name;
    private String hhid;
    private int salary_netto;
    private String employer;
    private String url;
    private String employer_hhid;
    private String employer_link;
    private int count;//это счетчик строк для вывода на html страницу, шаблон openvacancies.html
    private Date startDate;//дата открытия вакансии
    private String regionName;//название региона
}
