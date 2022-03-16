package com.weareadaptive.auction.repository;

import com.weareadaptive.auction.model.AuctionLot;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuctionRepository extends JpaRepository<AuctionLot,Integer> {

  @Transactional
  @Query("select a from Auction a where a.owner=?1")
  List<AuctionLot> getAllUserAuctions(String username);


  @Modifying
  @Transactional
  @Query("update Auction set status=?2 where id=?1")
  void close(int id, AuctionLot.Status closed);
}

