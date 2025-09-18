package com.ens.hhparser5.model;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Vacancy {
    public long id;
    public String hhid;
    public String name;
    public long employer;
    public int salary_from;
    public int salary_to;
    public int gross;
    public String url;
    public String alternate_url;
    public int archived;
    public int region;//area in terms of hh.ru

    public Vacancy(long id) {
        this.id = id;
    }

    public int getArchived() {
        return this.archived;
    }

}
