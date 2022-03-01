package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.controller.dto.CreateUserRequest;
import com.weareadaptive.auction.controller.dto.UpdateUserRequest;
import com.weareadaptive.auction.controller.dto.UserResponse;
import com.weareadaptive.auction.model.User;
import com.weareadaptive.auction.service.UserService;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.method.P;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;
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

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@RequestBody @Valid CreateUserRequest user) {
        User newUser = userService.create(user.username(), user.password(),
                user.firstName(), user.lastName(), user.organisation());
        return map(newUser);
    }

    @GetMapping("{id}")
    public UserResponse getUser(@PathVariable("id") @Valid int id) {
        return map(userService.getUser(id));
    }

    @PutMapping("{id}")
    public UserResponse edit(@PathVariable("id") @Valid int id, @RequestBody @Valid UpdateUserRequest updateUserRequest) {
        return map(
                userService.editUser(id, updateUserRequest.firstName(),
                        updateUserRequest.lastName(),
                        updateUserRequest.organisation()));
    }

    @GetMapping()
    public Stream<UserResponse> getUsers() {
        return userService.getUsers().values()
                .stream()
                .map(UserMapper::map);
    }

    @PutMapping("{id}/block")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(@PathVariable("id") @Valid int id) {
        userService.block(id);
    }
    @PutMapping("{id}/unblock")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(@PathVariable("id") @Valid int id) {
        userService.unblock(id);
    }

}
