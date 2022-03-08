package com.weareadaptive.auction.service;

import com.weareadaptive.auction.exception.EntityNotFoundException;
import com.weareadaptive.auction.model.User;
import com.weareadaptive.auction.model.UserState;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserState userState;

  public UserService(UserState userState) {
    this.userState = userState;
  }

  public User create(String username, String password, String firstName, String lastName,
                     String organisation) {
    var nextId = userState.nextId();
    User newUser = new User(nextId, username, password, firstName, lastName, organisation);
    userState.add(newUser);
    return newUser;
  }

  public Optional<User> get(int id) {
    return Optional.ofNullable(userState.get(id));
  }

  public List<User> getAllUsers() {
    return userState.stream().toList();
  }

  public User update(int id, String firstName, String lastName, String organisationName) {
    var user = get(id).orElseThrow(() -> new EntityNotFoundException(" "));
    user.setFirstName(firstName);
    user.setOrganisation(organisationName);
    user.setLastName(lastName);
    return user;
  }
}
