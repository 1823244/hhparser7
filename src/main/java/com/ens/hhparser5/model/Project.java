package com.ens.hhparser5.model;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Project {
    private long id;
    private String name;
    private long userId;

    public Project(long id, String name){
        this.id = id;
        this.name = name;
    }

    public Project(String name){
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", userId=" + userId +
                '}';
    }
}
