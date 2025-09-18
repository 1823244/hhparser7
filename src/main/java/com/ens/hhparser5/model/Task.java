package com.ens.hhparser5.model;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Task {
    private long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String user;
    private boolean isFinished;
    private long projectId;
    private Project project;
    private long userId;

}
