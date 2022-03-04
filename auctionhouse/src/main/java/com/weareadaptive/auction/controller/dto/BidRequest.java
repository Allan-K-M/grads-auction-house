package com.weareadaptive.auction.controller.dto;

import javax.validation.constraints.Min;

public record BidRequest(
                         @Min(1)
                         int quantity,

                         @Min(1)
                         double price) {
}
