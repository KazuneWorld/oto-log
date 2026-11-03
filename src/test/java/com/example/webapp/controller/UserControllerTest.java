package com.example.webapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;

import com.example.webapp.entity.User;
import com.example.webapp.service.AuthService;
import com.example.webapp.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private Model model;

    @InjectMocks
    private UserController userController;

    @Test
    void パスワード確認が一致しなければ登録せずフォームを返す() {
        User user = new User();
        user.setUserPass("password");

        String view = userController.signup(user, "different", request, model);

        assertEquals("user/signup", view);
        verify(model).addAttribute("errorMessage", "パスワードが一致しません。");
        verify(userService, never()).insert(user);
        verify(authService, never()).login(user, request);
    }

    @Test
    void メールアドレスが重複していれば登録せずフォームを返す() {
        User user = new User();
        user.setUserPass("password");
        user.setUserEmail("used@example.com");
        when(userService.isEmailTaken("used@example.com")).thenReturn(true);

        String view = userController.signup(user, "password", request, model);

        assertEquals("user/signup", view);
        verify(model).addAttribute("errorMessage", "このメールアドレスは既に使用されています。");
        verify(userService, never()).insert(user);
        verify(authService, never()).login(user, request);
    }

    @Test
    void 入力が正しければ登録して自動ログインする() {
        User user = new User();
        user.setUserPass("password");
        user.setUserEmail("new@example.com");
        user.setUserLoginId("new-user");
        when(userService.isEmailTaken("new@example.com")).thenReturn(false);
        when(userService.isLoginIdTaken("new-user")).thenReturn(false);

        String view = userController.signup(user, "password", request, model);

        assertEquals("redirect:/timeline", view);
        verify(userService).insert(user);
        verify(authService).login(user, request);
    }
}
