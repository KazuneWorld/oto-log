package com.example.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;


import com.example.webapp.entity.MoodTag;
import com.example.webapp.entity.Post;
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

    public PostController(PostService postService) {
        this.postService = postService;
    }
    
    //投稿の画面
    @GetMapping("/newpost")
    public String newPost(Model model) {
        // 気分タグの取得
        List<MoodTag> moodTags = postMapper.findAllTagId();
        model.addAttribute("moodTags", moodTags);
        return "post/newpost";
    }

    //投稿後のタイムライン画面
    @PostMapping("/timeline")
    public String postTimeline(Authentication authentication, @RequestParam("moodTagId") int moodTagId, @ModelAttribute Post post) {
        String userId = authentication.getName();
        postService.insertPost(userId, moodTagId);
        return "redirect:/timeline";
    }
}
