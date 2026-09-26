package com.example.webapp.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String userId;         
    private String userLoginId;    
    private String friendCode;    
    private String userEmail;      
    private String userPass;       
    private String userPassHash;   
    private String userNickname;   
    private String userIcon; 
    
}