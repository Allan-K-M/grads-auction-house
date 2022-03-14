package com.weareadaptive.auction.service;

import com.weareadaptive.auction.exception.EntityNotFoundException;
import com.weareadaptive.auction.model.BusinessException;
import com.weareadaptive.auction.model.User;
import com.weareadaptive.auction.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class UserService {
  private final UserRepository userRepository;


  public UserService(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public User create(String username, String password, String firstName, String lastName,
                     String organisation) {
    var newUser = new User();
    newUser.setUsername(username);
    newUser.setPassword(password);
    newUser.setFirstName(firstName);
    newUser.setLastName(lastName);
    newUser.setAdmin(false);
    newUser.setOrganisation(organisation);

    if (userRepository.getByUsername(username).isPresent()) {
      throw new BusinessException("User already exist");
    }
    userRepository.save(newUser);
    return newUser;
  }

  public Optional<User> get(int id) {
    return userRepository.findById(id);
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();
  }

  public User update(int id, String firstName, String lastName, String organisationName) {
    int update=userRepository.update(id,firstName,lastName,organisationName);
    if(update==1){
      return get(id).get();
    }
    throw new EntityNotFoundException(" User does not exist");
  }

  public Optional<User> validateUsernamePassword(String username, String password) {
    return userRepository.validateUsernamePassword(username, password);
  }

  public Optional<User> getByUsername(String username) {
    return userRepository.getByUsername(username);
  }

  public void block(int id) {
    int blocked = userRepository.block(id);
    if (blocked == 0) {
      throw new EntityNotFoundException("User not found");
    }
//   User user= get(id).orElseThrow(()->new EntityNotFoundException("User does not exist"));
//    user.setBlocked(true);
//    userRepository.save(user);
  }

  public void unblock(int id) {
    int blocked = userRepository.unblock(id);
    if (blocked == 0) {
      throw new EntityNotFoundException("User not found");
    }
//    User user= get(id).orElseThrow(()->new EntityNotFoundException("User does not exist"));
//    user.setBlocked(false);

  }
}
