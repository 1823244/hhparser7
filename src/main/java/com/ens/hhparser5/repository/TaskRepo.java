package com.ens.hhparser5.repository;

import com.ens.hhparser5.model.Task;
import com.ens.hhparser5.model.User;

import java.util.List;

public interface TaskRepo {

    Task save(Task task);

    Task create(Task task);

    Task update(Task task, Task taskFound);

    Task findById(long id);

    Task findLast(User user);

    List<Task> findAll(User user);
}
