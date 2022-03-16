package com.weareadaptive.auction.repository;

import com.weareadaptive.auction.model.User;
import java.util.Optional;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

  @Query("select u from AuctionUser u where u.username=?1 and u.password=?2")
  Optional<User> validateUsernamePassword(String username, String password);

  @Query("select u from AuctionUser u where u.username=?1")
  Optional<User> getByUsername(String username);

  @Modifying
  @Transactional
  @Query("update AuctionUser  set blocked = true where id=?1")
  int block(int id);

  @Modifying
  @Transactional
  @Query("update AuctionUser set blocked=false  where id=?1")
  int unblock(int id);

  @Modifying
  @Transactional
  @Query("update AuctionUser set firstName=?2, lastName=?3, organisation=?4 where id=?1")
  int update(int id, String firstName, String lastName, String organisationName);
}