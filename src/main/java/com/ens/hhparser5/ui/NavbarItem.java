package com.ens.hhparser5.ui;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class NavbarItem {
    public String name;
    public String link;
    public boolean isActive;
}
