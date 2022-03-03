package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.controller.dto.CreateUserRequest;
import com.weareadaptive.auction.controller.dto.UpdateUserRequest;
import com.weareadaptive.auction.controller.dto.UserResponse;
import com.weareadaptive.auction.exception.EntityNotFoundException;
import com.weareadaptive.auction.model.User;
import com.weareadaptive.auction.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.stream.Stream;

import static com.weareadaptive.auction.controller.UserMapper.map;

@RestController
@RequestMapping("/users")
@PreAuthorize("hasRole('ROLE_ADMIN')")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  UserResponse create(@RequestBody @Valid CreateUserRequest createUserRequest) {
    String username = createUserRequest.username();
    String password = createUserRequest.password();
    String firstName = createUserRequest.firstName();
    String lastName = createUserRequest.lastName();
    String organisation = createUserRequest.organisation();

    return map(userService.create(username, password, firstName, lastName, organisation));
  }

  @GetMapping("/{id}")
  UserResponse get(@PathVariable int id) {
    User user = userService.get(id)
      .orElseThrow(() -> new EntityNotFoundException("Invalid ID"));
    return map(user);
  }

  @GetMapping
  Stream<UserResponse> getAllUsers() {
    return userService.getAllUsers().stream().map(UserMapper::map);
  }

  @PutMapping("/{id}")
  UserResponse update(@PathVariable int id, @RequestBody @Valid UpdateUserRequest updateUserRequest) {
    return map(userService.update(id, updateUserRequest.firstName(), updateUserRequest.lastName(), updateUserRequest.organisation()));
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/block")
  void block(@PathVariable int id) {
    User user = userService.get(id).orElseThrow(() -> new EntityNotFoundException("Invalid Id"));
    user.block();
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PutMapping("/{id}/unblock")
  void unblock(@PathVariable int id) {
    User user = userService.get(id).orElseThrow(() -> new EntityNotFoundException("Invalid Id"));
    user.unblock();
  }

}
