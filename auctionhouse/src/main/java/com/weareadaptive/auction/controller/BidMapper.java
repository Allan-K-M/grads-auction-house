package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.controller.dto.BidResponse;
import com.weareadaptive.auction.model.Bid;


public class BidMapper {
  public static BidResponse mapBid(Bid bid, int auctionId) {
    return new BidResponse(bid.getUser(),
      auctionId, bid.getQuantity(), bid.getPrice());
  }
}
