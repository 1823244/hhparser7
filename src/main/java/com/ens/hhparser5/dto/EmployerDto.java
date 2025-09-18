package com.ens.hhparser5.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class EmployerDto {
    private long id;
    private String hhid;
    private String name;
    private String url;

    public EmployerDto(long id) {
        this.id = id;
    }
    public EmployerDto(long id, String hhid, String name) {
        this.id = id;
        this.hhid = hhid;
        this.name = name;
    }
}
