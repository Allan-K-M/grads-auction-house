package com.weareadaptive.auction.service;


import static java.lang.Math.min;
import static java.lang.String.format;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;

import com.weareadaptive.auction.exception.EntityNotFoundException;
import com.weareadaptive.auction.exception.UnauthorizedActivityException;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.Bid;
import com.weareadaptive.auction.model.BusinessException;
import com.weareadaptive.auction.model.ClosingSummary;
import com.weareadaptive.auction.model.WinningBid;
import com.weareadaptive.auction.repository.AuctionRepository;
import com.weareadaptive.auction.repository.BidRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import org.springframework.stereotype.Service;


@Service
public class AuctionLotService {
  private static ClosingSummary closingSummary;
  private static Supplier<Instant> timeProvider;
  private final AuctionRepository auctionRepository;
  private final BidRepository bidRepository;
  private final UserService userService;

  public AuctionLotService(AuctionRepository auctionRepository,
                           BidRepository bidRepository,
                           UserService userService) {
    this.auctionRepository = auctionRepository;
    this.bidRepository = bidRepository;
    this.userService = userService;
    timeProvider = Instant::now;
  }


  public AuctionLot create(String owner, String symbol, int quantity, double minPrice) {
     userService.getByUsername(owner).orElseThrow(
        () -> new BusinessException("Invalid Username"));
    var auctionLot = new AuctionLot(owner, symbol, quantity, minPrice);
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

    Bid newBid = new Bid(id, bidder.getUsername(), quantity, price);
    bidRepository.save(newBid);
    return newBid;
  }

  public List<Bid> getAllAuctionBids(String username, int id) {
    AuctionLot auctionLot = auctionRepository.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Auction not found"));
    if (auctionLot.getOwner().equals(username)) {
      return bidRepository.getAllAuctionBids(id);
    }
    throw new UnauthorizedActivityException("User can not view Bids");
  }

  public ClosingSummary closeAuction(int id, String username) {
    AuctionLot auctionLot =
        getAuctionById(id).orElseThrow(() -> new EntityNotFoundException("Auction not found"));
    if (auctionLot.getStatus() == AuctionLot.Status.CLOSED) {
      throw new BusinessException("Cannot close because already closed.");
    }
    if (!auctionLot.getOwner().equals(username)) {
      throw new UnauthorizedActivityException("User can not close this auction");
    }


    var orderedBids = getAllAuctionBids(username, id)
        .stream()
        .sorted(reverseOrder(comparing(Bid::getPrice))
            .thenComparing(reverseOrder(comparingInt(Bid::getQuantity))))
        .toList();
    var availableQuantity = auctionLot.getQuantity();
    var revenue = BigDecimal.ZERO;
    var winningBids = new ArrayList<WinningBid>();

    for (Bid bid : orderedBids) {
      if (availableQuantity > 0) {
        var bidQuantity = min(availableQuantity, bid.getQuantity());

        winningBids.add(new WinningBid(bidQuantity, bid));
        bid.win(bidQuantity);
        availableQuantity -= bidQuantity;
        revenue = revenue.add(valueOf(bidQuantity).multiply(valueOf(bid.getPrice())));
      } else {
        bid.lost();
      }
    }

    auctionRepository.close(id, AuctionLot.Status.CLOSED);
    closingSummary =
        new ClosingSummary(unmodifiableList(winningBids),
            auctionLot.getQuantity() - availableQuantity,
            revenue, timeProvider.get());
    return closingSummary;

  }

  public ClosingSummary getClosingSummary(String username, int id) {
    AuctionLot auctionLot = auctionRepository.getById(id);
    if (auctionLot.getStatus() == AuctionLot.Status.OPENED) {
      throw new BusinessException("Auction not closed yet");
    }
    if (auctionLot.getOwner().equals(username)) {
      return closingSummary;
    }
    throw new UnauthorizedActivityException("User can not view Closing Summary");
  }
}
