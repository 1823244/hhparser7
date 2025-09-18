// основная часть сделана по этой статье
//https://www.bezkoder.com/websecurityconfigureradapter-deprecated-spring-boot/
// "From Spring Boot 2.7, WebSecurityConfigurerAdapter is deprecated. "

// оф. документация - здесь:
//https://docs.spring.io/spring-security/reference/5.8/servlet/configuration/java.html
// (взял оттуда метод userDetailsService() и примеры для filterChain()

// todo Реализовать через старый подход - WebSecurityConfigurerAdapter
// (это 2020й год: https://www.youtube.com/watch?v=HvovW6Uh1yU&t=6874s)
// А здесь посмотреть страницу регистрации:
// https://habr.com/ru/post/482552/
// А здесь посмотреть реализацию jdbc auth:
// https://www.youtube.com/watch?v=HvovW6Uh1yU&t=6874s
// После jdbc выполнить переход на JPA auth
// но это наверное потребует перевода всего проекта на JPA...


package com.ens.hhparser5.configuration;

import com.ens.hhparser5.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;

@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private UserService userService;

    //@Autowired
    //private PasswordEncoder passwordEncoder;

    //@Autowired
    //private DataSource dataSource;

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .authorizeRequests()
                .antMatchers("/","/login","/logout","/registration","/register-success").permitAll()
                .antMatchers("/api/public/**").permitAll()
                .antMatchers("/projects/**").authenticated()
                .antMatchers("/searchtexts/**").authenticated()
                .antMatchers("/blacklist/**").authenticated()
                .antMatchers("/employers/**").authenticated()
                .antMatchers("/inwork/**").authenticated()
                .antMatchers("/tasks/**").authenticated()
                .antMatchers("/users/**").authenticated()
                .antMatchers("/vacancies/**").authenticated()

                .and()
                .csrf()
                    .ignoringAntMatchers("/api/public/**")
                .and()
                .formLogin()
                    .loginPage("/login")
                    .defaultSuccessUrl("/projects")
                    .permitAll()
                .and()
                .logout()
                    //.invalidateHttpSession(true)
                    .logoutSuccessUrl("/")
                    //.clearAuthentication(true)
                    .permitAll();

        //http.exceptionHandling().authenticationEntryPoint(new Http403ForbiddenEntryPoint());
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(daoAuthenticationProvider());
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider(){
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        authenticationProvider.setUserDetailsService(userService);
        return authenticationProvider;
    }
}

