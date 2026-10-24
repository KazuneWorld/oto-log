package com.example.webapp.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.webapp.entity.Notification;
import com.example.webapp.service.NotificationService;



@Controller 
public class FriendController {

    private final NotificationService notificationService;
    
    public FriendController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //フレンド承認画面
    @GetMapping("/friend/requests")
    public String friendRequests() {
        return "friend/friend-requests";
    }

    //フレンド追加画面
    @GetMapping("/friend/add")
    public String friendAdd() {
        return "friend/friend-add";
    }

    //通知一覧画面
    @GetMapping("/notifications")
    public String notifications(Model model, Authentication authentication) {
        String userId = authentication.getName();
        // 一覧を取得して、一覧を開いた時点までの通知をすべて既読にする
        // (返ってきた通知の read は「開く前の状態」なので、今回開いた時点で未読だったものを強調表示できる)
        List<Notification> notifications = notificationService.getListAndMarkAllAsRead(userId);
        model.addAttribute("notifications", notifications);
 
        // フッターのバッジの件数は、この処理より前に AllPageModelAdvice が「開く前の件数」で入れている。
        // 一覧を開いた時点までの通知をすべて既読にしたので、このページのバッジは 0 に上書きする
        model.addAttribute("unreadNotificationCount", 0);
        return "friend/notifications";
    }
    
}
