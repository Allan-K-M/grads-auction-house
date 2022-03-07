package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.controller.dto.BidResponse;
import com.weareadaptive.auction.controller.dto.ClosingSummaryResponse;
import com.weareadaptive.auction.model.ClosingSummary;
import java.util.stream.Stream;

public class ClosingSummaryMapper {
  public static ClosingSummaryResponse map(ClosingSummary closingSummary, int auctionId) {
    Stream<BidResponse> bidResponses = closingSummary.winningBids().stream()
        .map(winningBid -> BidMapper.mapBid(winningBid.originalBid(), auctionId));
    return new ClosingSummaryResponse(bidResponses, closingSummary.totalSoldQuantity(),
      closingSummary.totalRevenue(), closingSummary.closingTime());
  }
}
