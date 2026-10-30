package com.example.webapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import com.example.webapp.entity.User;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    @InjectMocks
    private AuthService authService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void ユーザーをログイン状態にしてセッションへ保存する() {
        User user = new User();
        user.setUserEmail("user@example.com");
        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername("user-id")
                .password("hashed-password")
                .roles("USER")
                .build();
        when(userDetailsService.loadUserByUsername("user@example.com")).thenReturn(userDetails);
        when(request.getSession()).thenReturn(session);

        authService.login(user, request);

        SecurityContext context = SecurityContextHolder.getContext();
        assertSame(userDetails, context.getAuthentication().getPrincipal());
        assertEquals("user-id", context.getAuthentication().getName());
        verify(session).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);
    }
}
