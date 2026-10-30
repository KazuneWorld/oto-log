package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.webapp.entity.Post;
import com.example.webapp.entity.Song;
import com.example.webapp.mapper.PostMapper;
import com.example.webapp.mapper.SongMapper;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private SongMapper songMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private PostService postService;

    @Test
    void 曲と投稿を登録してフレンドへ通知する() {
        Post post = new Post();
        Song song = new Song();
        song.setTrackId("track-1");
        Song savedSong = new Song();
        savedSong.setSongId(42);
        savedSong.setTrackId("track-1");
        when(postMapper.existsByPostId(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(songMapper.findByTrackId("track-1")).thenReturn(savedSong);

        postService.insertPost("user-1", post, song);

        assertEquals("user-1", post.getUserId());
        assertEquals(12, post.getPostId().length());
        assertEquals(42, post.getSongId());
        verify(songMapper).insertSong(song);
        verify(postMapper).insertPost(post);
        verify(notificationService).notifyFriendPosted(post.getPostId());
    }

    @Test
    void 曲を登録できなければ投稿と通知を行わず例外を投げる() {
        Post post = new Post();
        Song song = new Song();
        song.setTrackId("missing-track");
        when(postMapper.existsByPostId(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(songMapper.findByTrackId("missing-track")).thenReturn(null);

        assertThrows(IllegalStateException.class,
                () -> postService.insertPost("user-1", post, song));

        verify(postMapper, never()).insertPost(post);
        verify(notificationService, never()).notifyFriendPosted(org.mockito.ArgumentMatchers.anyString());
    }
}
