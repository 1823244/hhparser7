package com.ens.hhparser5.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class InworkDto {
    private long id;
    private String status;
    private long vacancyId;
    private String vacancyHhid;
    private Date endDate;
}
