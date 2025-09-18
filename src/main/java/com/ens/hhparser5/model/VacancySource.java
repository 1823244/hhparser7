package com.ens.hhparser5.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Objects;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class VacancySource {
    public long id;
    public String hhid;
    public String json;

    public VacancySource(long id) {
        this.id = id;
    }
    public VacancySource(long id, String hhid) {
        this.id = id; this.hhid = hhid;
    }

    public String getHhid() {
        return hhid;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VacancySource that = (VacancySource) o;
        return hhid.equals(that.hhid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hhid);
    }
}
