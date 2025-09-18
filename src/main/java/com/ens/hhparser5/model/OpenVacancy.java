package com.ens.hhparser5.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
public class OpenVacancy {
    public long id;
    public String name;
    public String hhid;
    public int salary_netto;
    public String employer;
    public String url;
    public String employer_hhid;
    public String employer_link;
    public int count;//это счетчик строк для вывода на html страницу, шаблон openvacancies.html
    public Date startDate;//дата открытия вакансии
    public String regionName;
}
