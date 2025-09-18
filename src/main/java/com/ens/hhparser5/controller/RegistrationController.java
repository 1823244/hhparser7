package com.ens.hhparser5.controller;

import com.ens.hhparser5.model.Role;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.Map;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/registration")
    public ModelAndView registration(ModelMap model) {
        return new ModelAndView("registration", model);
    }

    @GetMapping("/register-success")
    public ModelAndView registerSuccess(ModelMap model) {
        return new ModelAndView("registration-successful", model);
    }

    @PostMapping("/registration")
    public String registerUser(User user, Map<String,Object> model){

        User user_found = userService.findByUsername(user.getUsername());
        if (user_found != null){
            model.put("message", String.format("Username '%s' already exists!",user.getUsername()));
            return "redirect:/registration";
        }

        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        //user.setRoles(Collections.singleton(Role.USER));

/*`
        Set<String> roles = Arrays.stream(Role.values())
                .map(Role::name)
                .collect(Collectors.toSet());

        if (user.getRoles() != null) {
            user.getRoles().clear();
        }

        for (String key : model.keySet()) {
            if (roles.contains(key)) {
                user.getRoles().add(Role.valueOf(key));
            }
        }
*/

        userService.save(user);

        return "redirect:/register-success";
    }
}
