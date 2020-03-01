package com.arlley.redis.controller;

import com.arlley.redis.jdbc.UserJdbc;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
public class UserController {

    @Resource
    private UserJdbc userJdbc;
    @RequestMapping("/hello")
    public String hello(int id){

        return userJdbc.getUser(id).getName();
    }
}
