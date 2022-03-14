package com.weareadaptive.auction.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;


public record ClosingSummary(
    List<WinningBid> winningBids,
    int totalSoldQuantity,
    BigDecimal totalRevenue,
    Instant closingTime) {
}
