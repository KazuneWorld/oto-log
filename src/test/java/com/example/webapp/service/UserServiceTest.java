package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void 使用済みメールアドレスならtrueを返す() {
        when(userMapper.existsByEmail("used@example.com")).thenReturn(true);

        boolean result = userService.isEmailTaken("used@example.com");

        assertTrue(result);
        verify(userMapper).existsByEmail("used@example.com");
    }

    @Test
    void 未使用のログインIDならfalseを返す() {
        when(userMapper.existsByLoginId("new-user")).thenReturn(false);

        boolean result = userService.isLoginIdTaken("new-user");

        assertFalse(result);
        verify(userMapper).existsByLoginId("new-user");
    }

    @Test
    void ユーザー登録時にIDとフレンドコードを生成してパスワードをハッシュ化する() {
        User user = new User();
        user.setUserPass("plain-password");
        when(userMapper.existsByUserId(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(userMapper.existsByFriendCode(org.mockito.ArgumentMatchers.anyString())).thenReturn(false);
        when(passwordEncoder.encode("plain-password")).thenReturn("hashed-password");

        userService.insert(user);

        assertEquals(12, user.getUserId().length());
        assertEquals(8, user.getFriendCode().length());
        assertEquals("hashed-password", user.getUserPassHash());
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userMapper).insert(userCaptor.capture());
        Assertions.assertSame(user, userCaptor.getValue());
    }
}
