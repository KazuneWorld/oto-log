package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.example.webapp.entity.MoodTag;
import com.example.webapp.mapper.PostMapper;
import org.springframework.ui.Model;


//PostController
@Controller 
public class PostController {
    @Autowired
    private PostMapper postMapper;
    
    //投稿の画面
    @GetMapping("/newpost")
    public String newPost(Model model) {
        System.out.println(postMapper.findByTagId());
        List<MoodTag> moodTags = postMapper.findByTagId();
        model.addAttribute("moodTags", moodTags);
        System.out.println(moodTags);
        return "post/newpost";
    }
}
