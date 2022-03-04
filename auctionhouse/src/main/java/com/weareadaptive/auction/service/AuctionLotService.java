package com.weareadaptive.auction.service;


import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.AuctionState;
import com.weareadaptive.auction.model.Bid;
import com.weareadaptive.auction.model.BusinessException;
import com.weareadaptive.auction.model.UserState;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.stream.Stream;

@Service
public class AuctionLotService {
  public static final String AUCTION_LOT_ENTITY = "AuctionLot";
  private final AuctionState auctionState;
  private final UserState userState;

  public AuctionLotService(AuctionState auctionState, UserState userState) {
    this.auctionState = auctionState;
    this.userState = userState;
  }

  public AuctionLot create(String owner, String symbol, int quantity, double minPrice) {
    var user = userState.getByUsername(owner).get();
    var id = auctionState.nextId();
    var auctionLot = new AuctionLot(id, user, symbol, quantity, minPrice);
    auctionState.add(auctionLot);
    return auctionLot;
  }

  public Stream<AuctionLot> getAllAuctions(String username) {
    return auctionState.stream().filter((auctionLot) -> auctionLot.getOwner().getUsername().equals(username));
  }

  public Optional<AuctionLot> getAuctionById(int id) {
    return Optional.ofNullable(auctionState.get(id));
  }

  public Bid bid(int auctionId, String userName, int quantity, double price) {
    var auction = getAuctionById(auctionId).orElseThrow(() -> new BusinessException("Invalid Auction Id"));
    var bidder = userState.getByUsername(userName).orElseThrow(() -> new BusinessException("Invalid User Id"));
    auction.bid(bidder, quantity, price);
    return new Bid(bidder, quantity, price);
  }
}
