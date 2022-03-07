package com.weareadaptive.auction.controller;

import com.weareadaptive.auction.TestData;
import com.weareadaptive.auction.controller.dto.BidRequest;
import com.weareadaptive.auction.controller.dto.CreateAuctionRequest;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.service.AuctionLotService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuctionControllerTest {
  public static final int INVALID_AUCTION_ID = 999999;
  @Autowired
  private TestData testData;
  @Autowired
  private AuctionLotService auctionLotService;
  @LocalServerPort
  private int port;
  private String uri;

  @BeforeEach
  public void initialiseRestAssuredMockMvcStandalone() {
    uri = "http://localhost:" + port;
  }

  @DisplayName("Create should return created Auction if valid")
  @Test
  public void createShouldReturnAuction() {
    var quantity = 100;
    var minCost = 10.0;

    var createAuctionRequest = new CreateAuctionRequest("FB", minCost, quantity);
    //@formatter:off
    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user1Token())
      .contentType(ContentType.JSON)
      .body(createAuctionRequest)
      .when()
      .post("/auctions")
      .then()
      .statusCode(HttpStatus.CREATED.value())
      .body("id", greaterThan(0))
      .body("minPrice", equalTo(10.0F))
      .body("symbol", equalTo("FB"))
      .body("quantity", equalTo(100));
    //@formatter:on
  }


  @DisplayName("create should throw an Authorization Exception if User is Blocked ")
  @Test
  public void createShouldThrowIfUserCreatingAuctionIsBlocked() {
    testData.user1().block();
    var quantity = 100;
    var minCost = 100.0;

    var createAuctionRequest = new CreateAuctionRequest("APPL", minCost, quantity);

    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user1Token())
      .contentType(ContentType.JSON)
      .body(createAuctionRequest)
      .when()
      .post("/auctions")
      .then()
      .statusCode(HttpStatus.UNAUTHORIZED.value());


  }

  @DisplayName("getAuctions should return a list of all user Auctions")
  @Test
  public void getAuctionsShouldReturnAllUserAuctions() {

    var find1 = format("find { it.id == %s }.", testData.auctionLot1().getId());

    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user1Token())
      .when()
      .get("auctions/owner")
      .then()
      .log()
      .all()
      .body(find1 + "owner", equalTo(testData.user1().getUsername()))
      .body(find1 + "symbol", equalTo("ORANGE"))
      .body(find1 + "quantity", equalTo(34))
      .body(find1 + "minPrice", equalTo(45.78F));
  }

  @DisplayName("Should return Auction with given Id")
  @Test
  public void getAuctionByIdShouldReturnAuction() {
    AuctionLot auctionLot = auctionLotService.create(testData.user1().getUsername(), "ORANGE", 45, 33.65);

    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user1Token())
      .pathParam("id", auctionLot.getId())
      .when()
      .get("auctions/{id}")
      .then()
      .statusCode(HttpStatus.OK.value())
      .body("owner", equalTo(testData.user1().getUsername()))
      .body("symbol", equalTo("ORANGE"))
      .body("quantity", equalTo(45))
      .body("minPrice", equalTo(33.65F));
  }

  @DisplayName("get Auction by Id should throw if auction does not Exit")
  @Test
  public void getAuctionByIdShouldThrowIfAuctionDoesNotExist() {
    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user1Token())
      .pathParam("id", INVALID_AUCTION_ID)
      .when()
      .get("auctions/{id}")
      .then()
      .statusCode(NOT_FOUND.value());
  }

  @DisplayName("Should return bid response if bid created is valid")
  @Test
  public void bidShouldReturnBidIfBidIsValid() {

    BidRequest bidRequest = new BidRequest(3, 45.99);

    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user2Token())
      .pathParam("id", testData.auctionLot1().getId())
      .contentType(ContentType.JSON)
      .body(bidRequest)
      .when()
      .post("auctions/bids/{id}")
      .then()
      .statusCode(HttpStatus.CREATED.value())
      .body("ownerUsername", equalTo(testData.user2().getUsername()))
      .body("auctionId", equalTo(testData.auctionLot1().getId()))
      .body("quantity", equalTo(bidRequest.quantity()))
      .body("price", equalTo(45.99F));


  }

  @DisplayName("bid should throw a business exception if auction id is invalid")
  @Test
  public void bidShouldThrowIfAuctionIdIsInvalid() {

    BidRequest bidRequest = new BidRequest(3, 45.99);
    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user2Token())
      .pathParam("id", INVALID_AUCTION_ID)
      .contentType(ContentType.JSON)
      .body(bidRequest)
      .when()
      .post("auctions/bids/{id}")
      .then()
      .statusCode(HttpStatus.BAD_REQUEST.value());


  }

  @DisplayName("Should throw if User tries to bid on their own Auction")
  @Test
  public void shouldThrowIfUserCanNotBid() {
    BidRequest bidRequest = new BidRequest(3, 45.99);

    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user1Token())
      .contentType(ContentType.JSON)
      .body(bidRequest)
      .pathParam("id", testData.auctionLot1().getId())
      .when()
      .post("auctions/bids/{id}")
      .then()
      .statusCode(HttpStatus.BAD_REQUEST.value());
  }

  @DisplayName("Should return all bids for the auction owned by the User")
  @Test
  public void getAllAuctionBidsShouldReturnAllBidsForUserAuction() {
    auctionLotService.bid(testData.auctionLot1().getId(), testData.user2().getUsername(), 4, 134.56);

    var find1 = format("find { it.auctionId == %s }.", testData.auctionLot1().getId());

    //assert the whole object
    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user1Token())
      .pathParam("id",testData.auctionLot1().getId())
      .when()
      .get("auctions/bids/{id}")
      .then()
      .statusCode(HttpStatus.FOUND.value())
      .log()
      .all()
      .body(find1+"quantity", equalTo(4))
      .body(find1 + "price", equalTo(134.56F))
      .body(find1 + "auctionId", equalTo(testData.auctionLot1().getId()));

  }

  @DisplayName("GetAllAuctionBids should throw an Unauthorized Exception if user can not view all bids")
  @Test
  public void getAllAuctionBidsShouldThrowIfUserIsNotOwner(){
    auctionLotService.bid(testData.auctionLot1().getId(), testData.user2().getUsername(), 4, 134.56);


    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user2Token())
      .pathParam("id",testData.auctionLot1().getId())
      .when()
      .get("auctions/bids/{id}")
      .then()
      .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @DisplayName("Should return closing summary if user is owner")
  @Test
  public void closeAuctionShouldReturnSummary(){
    var bid1=auctionLotService.bid(testData.auctionLot2().getId(),testData.user2().getUsername(),10,125.00);
    var bid2=auctionLotService.bid(testData.auctionLot2().getId(),testData.user3().getUsername(),10,127.00);


    var find1 = format("winningBids.find { it.ownerUsername == '%s' }.", testData.user2().getUsername());
    var find2 = format("winningBids.find { it.ownerUsername == '%s' }.", testData.user3().getUsername());

    given()
      .baseUri(uri)
      .header(AUTHORIZATION,testData.user1Token())
      .pathParam("id",testData.auctionLot2().getId())
      .when()
      .put("auctions/{id}")
      .then()
      .statusCode(HttpStatus.OK.value())
      .log()
      .all()
      .body(find1+"ownerUsername",equalTo(bid1.getUser().getUsername()))
      .body(find1+"quantity",equalTo(bid1.getQuantity()))
      .body(find1+"price",equalTo(125.00F))
      .body(find2+"ownerUsername",equalTo(bid2.getUser().getUsername()))
      .body(find2+"quantity",equalTo(bid2.getQuantity()))
      .body(find2+"price",equalTo(127.00F))
      .body("totalSoldQuantity",equalTo(20))
      .body("totalRevenue",equalTo(2520F));

  }
  @DisplayName("Should throw if not owner trying to close")
  @Test
  public void closeAuctionShouldThrowIfNotOwner(){

    given()
      .baseUri(uri)
      .header(AUTHORIZATION,testData.user2Token())
      .pathParam("id",testData.auctionLot2().getId())
      .when()
      .put("auctions/{id}")
      .then()
      .statusCode(HttpStatus.UNAUTHORIZED.value());

  }
  @DisplayName("Should throw if Auction is already closed")
  @Test
  public void closeAuctionShouldThrowIfAuctionIsClosed(){
    testData.auctionLot1().close();

    given()
      .baseUri(uri)
      .header(AUTHORIZATION,testData.user1Token())
      .pathParam("id",testData.auctionLot1().getId())
      .when()
      .put("auctions/{id}")
      .then()
      .statusCode(HttpStatus.BAD_REQUEST.value());

  }


}
