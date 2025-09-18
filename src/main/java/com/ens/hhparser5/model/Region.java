package com.ens.hhparser5.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    private int id;
    private int parentId;
    private String name;

}
