package com.ens.hhparser5.dto;

import com.ens.hhparser5.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    public long id;
    public String name;
    public long userId;
    public String searchText;
}
