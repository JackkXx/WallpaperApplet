package com.service;

import com.entity.User;

public interface UserSerivce {
    User findByUserName(String userName);
}
