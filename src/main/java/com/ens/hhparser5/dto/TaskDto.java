package com.ens.hhparser5.dto;

import com.ens.hhparser5.model.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private long id;
    private String name;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String user;
    private boolean isFinished;
    private long projectId;
    private Project project;

}
