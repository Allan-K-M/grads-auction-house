package com.weareadaptive.auction.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity(name = "Bids")
public class Bid {



  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int bidId;

  private int id;

  private String  owner;
  private int quantity;
  private double price;
  private State state;
  private int winQuantity;

  public int getId() {
    return id;
  }

  public void setId(int auctionId) {
    this.id = auctionId;
  }

  public Bid(int auctionId,String owner, int quantity, double price) {
    if (owner == null) {
      throw new BusinessException("user cannot be null");
    }

    if (price <= 0) {
      throw new BusinessException("price must be above 0");
    }

    if (quantity <= 0) {
      throw new BusinessException("quantity must be above 0");
    }

    this.id=auctionId;
    this.price = price;
    this.owner = owner;
    this.quantity = quantity;
    state = State.PENDING;
  }

  public Bid() {

  }

  public int getQuantity() {
    return quantity;
  }

  public String getUser() {
    return owner;
  }

  public double getPrice() {
    return price;
  }

  public int getWinQuantity() {
    return winQuantity;
  }

  public State getState() {
    return state;
  }

  public int getBidId() {
    return bidId;
  }

  public void setBidId(int bidId) {
    this.bidId = bidId;
  }

  public void lost() {
    if (state != State.PENDING) {
      throw new BusinessException("Must be a pending bid");
    }

    state = State.LOST;
  }

  public void win(int winQuantity) {
    if (state != State.PENDING) {
      throw new BusinessException("Must be a pending bid"+state.toString());
    }

    if (quantity < winQuantity) {
      throw new BusinessException("winQuantity must be lower or equal to to the bid quantity");
    }

    state = State.WIN;
    this.winQuantity = winQuantity;
  }

  @Override
  public String toString() {
    return "Bid{"
      + "owner=" + owner
      + ", price=" + price
      + ", quantity=" + quantity
      + '}';
  }

  public enum State {
    PENDING,
    LOST,
    WIN
  }
}
