package com.ens.hhparser5.service;

import com.ens.hhparser5.model.Role;
import com.ens.hhparser5.model.User;
import com.ens.hhparser5.repository.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.security.Principal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    public User findByUsername(String username) {
        return userRepo.findByUsername(username);
    }

    public void save(User user){
        userRepo.save(user);
    }

    public Optional<User> findById(long userId) {
        return userRepo.findById(userId);
    }

    public List<User> findAll(){
        return userRepo.findAll();
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null){
            throw new UsernameNotFoundException(String.format(
                    "User '%s' not found",username
            ));
        }
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(), user.getPassword(),
                mapRolesToAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority>
        mapRolesToAuthorities(Collection<Role> roles){
        return roles.stream().map(r->new SimpleGrantedAuthority(r.getName())).collect(Collectors.toList());
    }

    public User getPrincipalUser(Principal principal){
        return userRepo.findByUsername(principal.getName());
    }
}
