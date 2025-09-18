package com.ens.hhparser5.ui;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Service
public class Navbar {
    private List<NavbarItem> items = new ArrayList<>();

    public Navbar(){
        items.add(new NavbarItem("Projects", "/projects", true));
        items.add(new NavbarItem("Employers", "/employers", true));
        items.add(new NavbarItem("Vacancies", "/vacancies", true));
        items.add(new NavbarItem("Black list", "/blacklist", true));
        items.add(new NavbarItem("InWork", "/inwork", true));
        items.add(new NavbarItem("Tasks", "/tasks", true));

    }

    // Выключить активность раздела в Навбар
    // Используется для текущего раздела
    public void offActivity(String name){
        for (NavbarItem item:items) {
            if (item.getName() == name){
                item.setActive(false);
                break;
            }
        }
    }
}
