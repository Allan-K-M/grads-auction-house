package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.controller.dto.UserResponse;
import com.weareadaptive.auction.model.User;

public class UserMapper {

    public static UserResponse map(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(), user.getOrganisation());

    }
 }
