package com.example.webapp.entity;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    private int friend_relation_id;
    private String requester_id;
    private String receiver_id;
    private int friend_status;
    private boolean notify_on_post;

}