package com.weareadaptive.auction.model;


import static org.apache.logging.log4j.util.Strings.isBlank;

import java.time.Instant;
import java.util.function.Supplier;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "Auction")
public class AuctionLot {


  private static Supplier<Instant> timeProvider;
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;
  private String owner;
  private String symbol;
  private double minPrice;
  private int quantity;
  private Status status;

  public AuctionLot(String owner, String symbol, int quantity, double minPrice) {
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


  public int getId() {
    return id;
  }

  public double getMinPrice() {
    return minPrice;
  }

  public int getQuantity() {
    return quantity;
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
