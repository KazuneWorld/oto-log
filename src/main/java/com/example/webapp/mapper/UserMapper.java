package com.example.webapp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.example.webapp.entity.User;

@Mapper
public interface UserMapper {
    // ログイン時、
    User findByUserId(@Param("userEmail") String userEmail);
    
    
}