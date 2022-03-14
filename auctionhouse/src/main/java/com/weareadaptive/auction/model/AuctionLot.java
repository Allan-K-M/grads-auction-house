package com.weareadaptive.auction.model;

import static java.lang.Math.min;
import static java.lang.String.format;
import static java.math.BigDecimal.valueOf;
import static java.util.Collections.reverseOrder;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparing;
import static java.util.Comparator.comparingInt;
import static org.apache.logging.log4j.util.Strings.isBlank;

import com.weareadaptive.auction.repository.BidRepository;
import com.weareadaptive.auction.service.AuctionLotService;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "Auction")
public class AuctionLot {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  private String owner;
  private String symbol;
  private double minPrice;
  private int quantity;
  private Status status;
  private static final AuctionLotService auctionLotService = null;
  private static ClosingSummary closingSummary;
  private static Supplier<Instant> timeProvider;

  public AuctionLot( String owner, String symbol, int quantity, double minPrice) {
    if (owner == null) {
      throw new BusinessException("owner cannot be null");
    }
    if (isBlank(symbol)) {
      throw new BusinessException("symbol cannot be null or empty");
    }
    if (minPrice < 0) {
      throw new BusinessException("minPrice cannot be bellow 0");
    }
    if (quantity < 0) {
      throw new BusinessException("quantity must be above 0");
    }

    this.owner = owner;
    this.symbol = symbol.toUpperCase().trim();
    this.quantity = quantity;
    this.minPrice = minPrice;
    status = Status.OPENED;
    timeProvider = Instant::now;
  }

  public AuctionLot() {

  }

  public Status getStatus() {
    return status;
  }

  public String getOwner() {
    return owner;
  }

  public String getSymbol() {
    return symbol;
  }

  public ClosingSummary getClosingSummary() {
    if (Status.CLOSED != status) {
      throw new BusinessException("AuctionLot must be closed to have a closing summary");
    }
    return closingSummary;
  }




  public ClosingSummary close() {
    if (status == Status.CLOSED) {
      throw new BusinessException("Cannot close because already closed.");
    }

    status = Status.CLOSED;

    var orderedBids = auctionLotService.getAllAuctionBids(owner, id)
        .stream()
        .sorted(reverseOrder(comparing(Bid::getPrice))
            .thenComparing(reverseOrder(comparingInt(Bid::getQuantity))))
        .toList();
    var availableQuantity = this.quantity;
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

    closingSummary =
        new ClosingSummary(unmodifiableList(winningBids), this.quantity - availableQuantity,
            revenue, timeProvider.get());
    return closingSummary;
  }

  public int getId() {
    return id;
  }

  public double getMinPrice() {
    return minPrice;
  }

  public int getQuantity() {
    return quantity;
  }

  public void setTimeProvider(Supplier<Instant> timeProvider) {
    this.timeProvider = timeProvider;
  }

  public List<Bid> getLostBids() {
    return auctionLotService.getAllAuctionBids(owner, id)
        .stream()
        .filter(bid -> closingSummary.winningBids().stream().noneMatch(wb -> wb.originalBid() == bid))
        .toList();
  }

  public List<WinningBid> getWonBids(String owner) {
    return closingSummary.winningBids()
        .stream()
        .filter(b -> b.originalBid().getUser().equals(owner))
        .toList();
  }

  @Override
  public String toString() {
    return "AuctionLot{"
        + "owner=" + owner
        + ", title='" + symbol + '\''
        + ", status=" + status
        + '}';
  }

  public enum Status {
    OPENED,
    CLOSED
  }
}
