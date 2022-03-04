package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.controller.dto.AuctionResponse;
import com.weareadaptive.auction.controller.dto.CreateAuctionRequest;
import com.weareadaptive.auction.exception.EntityNotFoundException;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.service.AuctionLotService;
import com.weareadaptive.auction.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;

import static com.weareadaptive.auction.controller.AuctionMapper.map;

@RestController
@RequestMapping("/auctions")
@PreAuthorize("hasRole('ROLE_USER')")
public class AuctionLotController {
  private final AuctionLotService auctionLotService;
  private final UserService userService;

  public AuctionLotController(AuctionLotService auctionLotService, UserService userService) {
    this.auctionLotService = auctionLotService;
    this.userService = userService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  AuctionResponse create(@RequestBody @Valid CreateAuctionRequest createAuctionRequest, Principal principal) {
    return map(auctionLotService.create(principal.getName(), createAuctionRequest.symbol(), createAuctionRequest.quantity(), createAuctionRequest.minPrice()));

  }

  @GetMapping("/owner")
  List<AuctionResponse> getAllUserAuctions(Principal principal) {
    return auctionLotService.getAllAuctions(principal.getName()).map(AuctionMapper::map).toList();
  }

  @ResponseStatus(HttpStatus.NO_CONTENT)
  @GetMapping("/{id}")
  AuctionResponse getAuctionById(@RequestBody @Valid int id) {
    AuctionLot auctionLot = auctionLotService.getAuctionById(id).orElseThrow(() -> new EntityNotFoundException("Invalid Auction Id"));
    return map(auctionLot);
  }



}
