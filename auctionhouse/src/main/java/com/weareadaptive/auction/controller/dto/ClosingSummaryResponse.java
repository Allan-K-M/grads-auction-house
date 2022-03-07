package com.weareadaptive.auction.controller.dto;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.stream.Stream;

public record ClosingSummaryResponse(Stream<BidResponse> winningBids,
                                     int totalSoldQuantity, BigDecimal totalRevenue,
                                     Instant closingTime) {
}
