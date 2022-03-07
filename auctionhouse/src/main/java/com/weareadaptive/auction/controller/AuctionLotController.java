package com.weareadaptive.auction.controller;

import static com.weareadaptive.auction.controller.AuctionMapper.map;
import static com.weareadaptive.auction.controller.BidMapper.mapBid;

import com.weareadaptive.auction.controller.dto.AuctionResponse;
import com.weareadaptive.auction.controller.dto.BidRequest;
import com.weareadaptive.auction.controller.dto.BidResponse;
import com.weareadaptive.auction.controller.dto.ClosingSummaryResponse;
import com.weareadaptive.auction.controller.dto.CreateAuctionRequest;
import com.weareadaptive.auction.exception.EntityNotFoundException;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.Bid;
import com.weareadaptive.auction.service.AuctionLotService;
import java.security.Principal;
import java.util.stream.Stream;
import javax.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;





@RestController
@RequestMapping("/auctions")
@PreAuthorize("hasRole('ROLE_USER')")
public class AuctionLotController {
  private final AuctionLotService auctionLotService;

  public AuctionLotController(AuctionLotService auctionLotService) {
    this.auctionLotService = auctionLotService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  AuctionResponse create(@RequestBody @Valid CreateAuctionRequest createAuctionRequest,
                         Principal principal) {
    return map(auctionLotService.create(principal.getName(),
      createAuctionRequest.symbol(), createAuctionRequest.quantity(),
      createAuctionRequest.minPrice()));

  }

  @GetMapping("/owner")
  Stream<AuctionResponse> getAllUserAuctions(Principal principal) {
    return auctionLotService.getAllAuctions(principal.getName()).map(AuctionMapper::map);
  }


  @GetMapping("/{id}")
  AuctionResponse getAuctionById(@PathVariable int id) {
    AuctionLot auctionLot = auctionLotService.getAuctionById(id).orElseThrow(
        () -> new EntityNotFoundException("Invalid Auction Id"));
    return map(auctionLot);
  }

  @ResponseStatus(HttpStatus.CREATED)
  @PostMapping("/bids/{id}")
  BidResponse bid(@RequestBody @Valid BidRequest bidRequest,
                  Principal principal, @PathVariable int id) {

    Bid bid = auctionLotService.bid(id, principal.getName(),
        bidRequest.quantity(), bidRequest.price());

    return mapBid(bid, id);

  }

  @ResponseStatus(HttpStatus.FOUND)
  @GetMapping("/bids/{id}")
  Stream<BidResponse> getAllAuctionBids(Principal principal, @PathVariable int id) {
    return auctionLotService.getAllAuctionBids(
      principal.getName(), id).stream().map(bid -> mapBid(bid, id));

  }

  @PutMapping("/{id}")
  ClosingSummaryResponse closeAuction(@PathVariable int id, Principal principal) {


    return ClosingSummaryMapper.map(auctionLotService.closeAuction(id, principal.getName()), id);
  }

  @GetMapping("/{id}/ClosingSummary")
  ClosingSummaryResponse getClosingSummary(Principal principal, @PathVariable int id){
    return ClosingSummaryMapper.map(auctionLotService.getClosingSummary(principal.getName(),id),id);
  }


}
