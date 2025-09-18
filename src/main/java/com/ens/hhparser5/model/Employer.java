package com.ens.hhparser5.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Employer {
    public long id;
    public String hhid;
    public String name;
    public String url;

    public Employer(long id) {
        this.id = id;
    }
    public Employer(long id, String hhid, String name) {
        this.id = id;
        this.hhid = hhid;
        this.name = name;
    }

}
