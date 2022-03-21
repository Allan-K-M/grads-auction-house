package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class git UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }
}
