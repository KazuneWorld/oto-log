package com.example.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.webapp.entity.MoodTag;
import com.example.webapp.entity.Post;
import com.example.webapp.entity.Song;
import com.example.webapp.mapper.PostMapper;
import com.example.webapp.service.PostService;

@ExtendWith(MockitoExtension.class)
class PostControllerTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private PostService postService;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;

    private PostController postController;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        postController = new PostController(postService);
        ReflectionTestUtils.setField(postController, "postMapper", postMapper);
    }

    @Test
    void 新規投稿画面に気分タグを渡す() {
        List<MoodTag> moodTags = List.of(new MoodTag());
        org.mockito.Mockito.when(postMapper.findAllTagId()).thenReturn(moodTags);

        String view = postController.newPost(model);

        assertEquals("post/newpost", view);
        verify(model).addAttribute("moodTags", moodTags);
    }

    @Test
    void 投稿処理は認証ユーザーでサービスを呼びタイムラインへ戻る() {
        Post post = new Post();
        Song song = new Song();
        org.mockito.Mockito.when(authentication.getName()).thenReturn("user-1");

        String view = postController.postTimeline(authentication, post, song);

        assertEquals("redirect:/timeline", view);
        verify(postService).insertPost("user-1", post, song);
    }
}
