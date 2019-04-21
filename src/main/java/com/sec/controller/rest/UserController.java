package com.sec.controller.rest;

import com.sec.common.Codes;
import com.sec.common.Resp;
import com.sec.pojo.User;
import com.sec.service.portal.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping("/send_code")
    public Resp register(@Valid User user) {
        return userService.sendCode(user);
    }

    @RequestMapping("/register")
    public Resp register(@Valid User user, String code) {
        return userService.register(user, code);
    }

    @RequestMapping("/login")
    public Resp login(User user) {
        return userService.login(user);
    }

    @RequestMapping("/info")
    public Resp info() {
        User loginUser = userService.getUserByToken();
        if(loginUser == null) {
            return Resp.error(Codes.NO_LOGIN);
        }
        return Resp.success(loginUser);
    }

    @RequestMapping("/logout")
    public Resp logout() {
        return userService.logout();
    }
}
