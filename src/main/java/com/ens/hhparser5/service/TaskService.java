package com.ens.hhparser5.service;

import com.ens.hhparser5.model.Project;
import com.ens.hhparser5.model.Task;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.repository.TaskRepoImpl;
import com.ens.hhparser5.utility.ClockHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskService {

    @Autowired
    private TaskRepoImpl taskRepoImpl;
    @Autowired
    private UserService userService;

    public Task createTask(Project project,
                              String name,
                              LocalDateTime startTime,
                              User user){

        Task task = new Task();
        if (project != null){
            task.setProjectId(project.getId());
        }

        task.setName(name);
        task.setStartTime(startTime);
        task.setProject(project);
        task.setUserId(user.getId());

        Task newTask = taskRepoImpl.save(task);

        return newTask;
    }

    public void finishTask(Task task){
        task.setEndTime(LocalDateTime.now(ClockHolder.getClock()));
        taskRepoImpl.save(task);
    }

    public Task obtainLastTask(User user){
        Task task = taskRepoImpl.findLast(user);
        return task;
    }

    public List<Task> findAll(User user){
        return taskRepoImpl.findAll(user);
    }

}
