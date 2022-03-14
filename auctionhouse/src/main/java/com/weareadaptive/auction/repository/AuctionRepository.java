package com.weareadaptive.auction.repository;

import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.User;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<AuctionLot,Integer> {

  @Transactional
  @Query("select a from Auction a where a.owner=?1")
  List<AuctionLot> getAllUserAuctions(String username);
}

