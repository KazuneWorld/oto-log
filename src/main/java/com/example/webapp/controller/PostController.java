package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.webapp.entity.MoodTag;
import com.example.webapp.mapper.PostMapper;
import com.example.webapp.service.PostService;

import org.springframework.ui.Model;


//PostController
@Controller 
public class PostController {

    @Autowired
    private PostMapper postMapper;

    @Autowired
    private PostService postService;
    
    //投稿の画面
    @GetMapping("/newpost")
    public String newPost(Model model) {
        List<MoodTag> moodTags = postMapper.findAllTagId();
        model.addAttribute("moodTags", moodTags);
        return "post/newpost";
    }

    //投稿後のタイムライン画面
    @PostMapping("/timeline")
    public String postTimeline(Authentication authentication, @RequestParam("moodTagId") int moodTagId) {
        String userId = authentication.getName();
        String postId = postService.generateRandomString(12);
        postMapper.insertPostId(postId, userId, moodTagId);
        return "redirect:/timeline";
    }
}
