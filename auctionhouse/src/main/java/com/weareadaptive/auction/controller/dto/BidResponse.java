package com.weareadaptive.auction.controller.dto;

public record BidResponse(String ownerUsername,
                          int auctionId,
                          int quantity,
                          Double price) {
}
