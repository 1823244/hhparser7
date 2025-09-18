package com.ens.hhparser5.model;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class Inwork {
    private long id;
    private String status;
    private long vacancyId;
    private String vacancyHhid;
    private Date endDate;

}
