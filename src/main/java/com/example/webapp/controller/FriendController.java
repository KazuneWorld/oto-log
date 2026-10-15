package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


@Controller 
public class FriendController {
    
    //フレンド承認画面
    @GetMapping("/friend/requests")
    public String friendRequests() {
        return "friend/friend-requests";
    }

    //フレンド追加画面
    @GetMapping("/friend/add")
    public String friendAdd() {
        return "friend/friend-add";
    }
    
}
