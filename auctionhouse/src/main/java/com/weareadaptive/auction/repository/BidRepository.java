package com.weareadaptive.auction.repository;

import com.weareadaptive.auction.model.Bid;
import java.util.List;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BidRepository extends JpaRepository<Bid,Integer> {


  @Transactional
  @Query("select b from Bids b where b.id=?1")
  List<Bid> getAllAuctionBids( int id);

}
