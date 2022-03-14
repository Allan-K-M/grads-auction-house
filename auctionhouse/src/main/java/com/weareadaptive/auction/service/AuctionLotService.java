package com.weareadaptive.auction.service;


import static java.lang.String.format;

import com.weareadaptive.auction.exception.UnauthorizedActivityException;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.Bid;
import com.weareadaptive.auction.model.BusinessException;
import com.weareadaptive.auction.model.ClosingSummary;
import com.weareadaptive.auction.repository.AuctionRepository;
import com.weareadaptive.auction.repository.BidRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.stereotype.Service;


@Service
public class AuctionLotService {
  private final AuctionRepository auctionRepository;
  private final BidRepository bidRepository;
  private final UserService userService;

  public AuctionLotService(AuctionRepository auctionRepository,
                           BidRepository bidRepository,
                           UserService userService) {
    this.auctionRepository = auctionRepository;
    this.bidRepository = bidRepository;
    this.userService = userService;
  }


  public AuctionLot create(String owner, String symbol, int quantity, double minPrice) {
    var user = userService.getByUsername(owner).orElseThrow(
        () -> new BusinessException("Invalid Username"));
    var auctionLot = new AuctionLot( owner, symbol, quantity, minPrice);
    auctionRepository.save(auctionLot);
    return auctionLot;
  }

  public List<AuctionLot> getAllAuctions(String username) {
    return auctionRepository.getAllUserAuctions(username);
  }

  public Optional<AuctionLot> getAuctionById(int id) {
    return auctionRepository.findById(id);
  }



  public Bid bid(int id, String userName, int quantity, double price) {

    var auction = getAuctionById(id)
      .orElseThrow(() -> new BusinessException("Invalid Auction Id"));

    if (auction.getStatus() == AuctionLot.Status.CLOSED) {
      throw new BusinessException("Cannot close an already closed.");
    }

    if (userName.equals(auction.getOwner())) {
      throw new BusinessException("User cannot bid on his own auctions");
    }

    if (quantity < 0) {
      throw new BusinessException("quantity must be be above 0");
    }

    if (price < auction.getMinPrice()) {
      throw new BusinessException(format("price needs to be above %s", auction.getMinPrice()));
    }

    var bidder = userService.getByUsername(userName)
        .orElseThrow(() -> new BusinessException("Invalid User name"));

    Bid newBid=new Bid(id, bidder.getUsername(), quantity, price);
    bidRepository.save(newBid);
    return newBid;
  }

  public List<Bid> getAllAuctionBids(String username, int id) {
    AuctionLot auctionLot = auctionRepository.getById(id);
    if (auctionLot.getOwner().equals(username)) {
      return bidRepository.getAllAuctionBids(id);
    }
    throw new UnauthorizedActivityException("User can not view Bids");
  }

  public ClosingSummary closeAuction(int id, String username) {
    AuctionLot auctionLot = auctionRepository.getById(id);
    if (auctionLot.getOwner().equals(username)) {
      auctionLot.close();
      auctionRepository.save(auctionLot);
      return auctionLot.close();
    }
    throw new UnauthorizedActivityException("User can not close auction");
  }

  public ClosingSummary getClosingSummary(String username, int id) {
    AuctionLot auctionLot = auctionRepository.getById(id);
    if (auctionLot.getOwner().equals(username)) {
      return auctionLot.getClosingSummary();
    }
    throw new UnauthorizedActivityException("User can not view Closing Summary");
  }
}
