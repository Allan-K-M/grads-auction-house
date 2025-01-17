package com.weareadaptive.auction.controller.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public record CreateAuctionRequest(


      @NotBlank
      @Pattern(regexp = "^[A-Za-z0-9.]+$")
      @Size(max = 100)
      String symbol,


      @Min(0)
     double minPrice,


      @Min(0)
      int quantity) {
}
