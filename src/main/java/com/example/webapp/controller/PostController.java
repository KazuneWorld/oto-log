package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.webapp.entity.MoodTag;
import com.example.webapp.mapper.PostMapper;
import org.springframework.ui.Model;

import org.springframework.security.core.annotation.AuthenticationPrincipal;


//PostController
@Controller 
public class PostController {
    @Autowired
    private PostMapper postMapper;
    
    //投稿の画面
    @GetMapping("/newpost")
    public String newPost(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        String userId = userDetails.getUsername();
        List<MoodTag> moodTags = postMapper.allTagId();
        model.addAttribute("userId", userId);
        model.addAttribute("moodTags", moodTags);
        return "post/newpost";
    }
}
