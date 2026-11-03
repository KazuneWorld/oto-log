package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.webapp.entity.User;
import com.example.webapp.mapper.UserMapper;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void メールアドレスから認証情報を作成する() {
        User user = new User();
        user.setUserId("user-id");
        user.setUserPassHash("hashed-password");
        when(userMapper.findByLoginValue("user@example.com")).thenReturn(user);

        UserDetails result = userDetailsService.loadUserByUsername("user@example.com");

        assertEquals("user-id", result.getUsername());
        assertEquals("hashed-password", result.getPassword());
        assertEquals("ROLE_USER", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void 存在しないユーザーなら例外を投げる() {
        when(userMapper.findByLoginValue("missing@example.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@example.com"));
    }
}
