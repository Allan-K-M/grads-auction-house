package com.weareadaptive.auction.service;


import com.weareadaptive.auction.exception.UnauthorizedActivityException;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.AuctionState;
import com.weareadaptive.auction.model.Bid;
import com.weareadaptive.auction.model.BusinessException;
import com.weareadaptive.auction.model.ClosingSummary;
import com.weareadaptive.auction.model.UserState;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;

//formatter:off
@Service
public class AuctionLotService {
  private final AuctionState auctionState;
  private final UserState userState;

  public AuctionLotService(AuctionState auctionState, UserState userState) {
    this.auctionState = auctionState;
    this.userState = userState;
  }
  //formatter:on

  public AuctionLot create(String owner, String symbol, int quantity, double minPrice) {
    var user = userState.getByUsername(owner).orElseThrow(
        () -> new BusinessException("Invalid User Id"));
    var id = auctionState.nextId();
    var auctionLot = new AuctionLot(id, user, symbol, quantity, minPrice);
    auctionState.add(auctionLot);
    return auctionLot;
  }

  public Stream<AuctionLot> getAllAuctions(String username) {
    return auctionState.stream()
      .filter((auctionLot) -> auctionLot.getOwner().getUsername().equals(username));
  }

  public Optional<AuctionLot> getAuctionById(int id) {
    return Optional.ofNullable(auctionState.get(id));
  }

  public Bid bid(int auctionId, String userName, int quantity, double price) {
    var auction = getAuctionById(auctionId)
        .orElseThrow(() -> new BusinessException("Invalid Auction Id"));
    var bidder = userState.getByUsername(userName)
        .orElseThrow(() -> new BusinessException("Invalid User Id"));
    if (auction.getStatus().equals(AuctionLot.Status.CLOSED)) {
      throw new BusinessException("Auction Already closed can not bid");
    }
    auction.bid(bidder, quantity, price);
    return new Bid(bidder, quantity, price);
  }

  public List<Bid> getAllAuctionBids(String username, int id) {
    AuctionLot auctionLot = auctionState.get(id);
    if (auctionLot.getOwner().getUsername().equals(username)) {
      return auctionLot.getBids();
    }
    throw new UnauthorizedActivityException("User can not view Bids");
  }

  public ClosingSummary closeAuction(int id, String username) {
    AuctionLot auctionLot = auctionState.get(id);
    if (auctionLot.getOwner().getUsername().equals(username)) {
      return auctionLot.close();
    }
    throw new UnauthorizedActivityException("User can not close auction");
  }
}
