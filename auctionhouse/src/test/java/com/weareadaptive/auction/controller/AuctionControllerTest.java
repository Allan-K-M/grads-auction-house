package com.weareadaptive.auction.controller;

import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.weareadaptive.auction.IntegrationTest;
import com.weareadaptive.auction.TestData;
import com.weareadaptive.auction.controller.dto.BidRequest;
import com.weareadaptive.auction.controller.dto.CreateAuctionRequest;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.Bid;
import com.weareadaptive.auction.service.AuctionLotService;
import com.weareadaptive.auction.service.UserService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuctionControllerTest extends IntegrationTest {
  public static final int INVALID_AUCTION_ID = 999999;
  @Autowired
  private TestData testData;

  @Autowired
  private AuctionLotService auctionLotService;

  @Autowired
  private UserService userService;


  @BeforeEach
  public void initialiseRestAssuredMockMvcStandalone() {
    uri = "http://localhost:" + port;

  }


  @DisplayName("create should return created Auction if valid")
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
      .body("owner", equalTo(testData.user1().getUsername()))
      .body("quantity", equalTo(100));
    var auction = auctionLotService.getAuctionById(1);
    //@formatter:on
  }


  @DisplayName("create should throw an Authorization Exception if User is Blocked ")
  @Test
  public void createShouldThrowIfUserCreatingAuctionIsBlocked() {
    userService.block(testData.user1().getId());
    var quantity = 100;
    var minCost = 100.0;
    //@formatter:off
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
    //@formatter:on


  }

  @DisplayName("getAuctions should return a list of all user Auctions")
  @Test
  public void getAuctionsShouldReturnAllUserAuctions() {


    AuctionLot auctionLot1=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);
    var find1 = format("find { it.id == %s }.", auctionLot1.getId());

    //@formatter:off
    given()
        .baseUri(uri)
        .header(AUTHORIZATION, testData.user1Token())
    .when()
        .get("auctions/owner")
    .then()
        .log()
        .all()
        .body(find1 + "owner", equalTo(testData.user1().getUsername()))
        .body(find1 + "symbol", equalTo("FB"))
        .body(find1 + "quantity", equalTo(100))
        .body(find1 + "minPrice", equalTo(20.00F));
    //@formatter:on

  }

  @DisplayName("getAuctionById should return Auction with given Id")
  @Test
  public void getAuctionByIdShouldReturnAuction() {
    AuctionLot auctionLot =
        auctionLotService.create(testData.user1().getUsername(), "ORANGE", 45, 33.65);
    //@formatter:off
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
    //@formatter:on
  }

  @DisplayName("getAuctionById should throw if auction does not Exit")
  @Test
  public void getAuctionByIdShouldThrowIfAuctionDoesNotExist() {


    //@formatter:off
    given()
        .baseUri(uri)
        .header(AUTHORIZATION, testData.user1Token())
        .pathParam("id", 99999)
    .when()
        .get("auctions/{id}")
    .then()
        .statusCode(NOT_FOUND.value());

    //@formatter:on
  }

  @DisplayName("bid should return bid response if bid created is valid")
  @Test
  public void bidShouldReturnBidIfBidIsValid() {

    BidRequest bidRequest = new BidRequest(3, 45.99);
    AuctionLot auctionLot1=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);
    //Bid bid=auctionLotService.bid(auctionLot1.getAuction_id(),testData.user2().getUsername(),5,100.00);

    //@formatter:off

    given().
        baseUri(uri)
        .header(AUTHORIZATION, testData.user2Token())
        .pathParam("id", auctionLot1.getId())
        .contentType(ContentType.JSON)
        .body(bidRequest)
        .when()
        .post("auctions/bids/{id}").then()
        .statusCode(HttpStatus.CREATED.value())
        .body("ownerUsername", equalTo(testData.user2().getUsername()))
        .body("auctionId", equalTo(auctionLot1.getId()))
        .body("quantity", equalTo(bidRequest.quantity())).body("price", equalTo(45.99F));


    //@formatter:on

  }

  @DisplayName("bid should throw a business exception if auction id is invalid")
  @Test
  public void bidShouldThrowIfAuctionIdIsInvalid() {

    BidRequest bidRequest = new BidRequest(3, 45.99);
    given().baseUri(uri).header(AUTHORIZATION, testData.user2Token())
        .pathParam("id", INVALID_AUCTION_ID).contentType(ContentType.JSON).body(bidRequest).when()
        .post("auctions/bids/{id}").then().statusCode(HttpStatus.BAD_REQUEST.value());


  }

  @DisplayName("bid should throw if User tries to bid on their own Auction")
  @Test
  public void bidShouldThrowIfUserCanNotBid() {
    BidRequest bidRequest = new BidRequest(3, 45.99);
    AuctionLot auctionLot1=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);
    //@formatter:off
    given().
        baseUri(uri)
        .header(AUTHORIZATION, testData.user1Token())
        .contentType(ContentType.JSON)
        .body(bidRequest)
        .pathParam("id", auctionLot1.getId())
        .when()
        .post("auctions/bids/{id}")
        .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
    //@formatter:on
  }

  @DisplayName("getAllAuctionBids should return all bids for the auction owned by the User")
  @Test
  public void getAllAuctionBidsShouldReturnAllBidsForUserAuction() {
    AuctionLot auctionLot1=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

    Bid bid1=auctionLotService.bid(auctionLot1.getId(), testData.user2().getUsername(), 4,
        134.56);
//    Bid bid2=auctionLotService.bid(auctionLot1.getId(), testData.user3().getUsername(), 5,
//        144.56);


    var find1 = format("find { it.auctionId == %s }.", bid1.getId());

    //@formatter:off
    given()
        .baseUri(uri)
        .header(AUTHORIZATION, testData.user1Token())
        .pathParam("id", auctionLot1.getId())
        .when()
        .get("auctions/bids/{id}")
        .then()
        .statusCode(HttpStatus.FOUND.value())
        .log()
        .all()
        .body(find1 + "auctionId", equalTo(bid1.getId()))
        .body(find1 + "quantity", equalTo(4))
        .body(find1 + "price", equalTo(134.56F));
    //@formatter:on
  }

  @DisplayName("getAllAuctionBids should throw an Unauthorized Exception if user can not view all bids")
  @Test
  public void getAllAuctionBidsShouldThrowIfUserIsNotOwner() {
    AuctionLot auctionLot1=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

    auctionLotService.bid(auctionLot1.getId(), testData.user2().getUsername(), 4,
        134.56);


    given().baseUri(uri).header(AUTHORIZATION, testData.user2Token())
        .pathParam("id",auctionLot1.getId()).when().get("auctions/bids/{id}").then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());
  }

  @DisplayName("closeAuction should return closing summary if user is owner")
  @Test
  public void closeAuctionShouldReturnSummary() {
    AuctionLot auctionLot2=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

    var bid1 =
        auctionLotService.bid(auctionLot2.getId(), testData.user2().getUsername(), 10,
            125.00);
    var bid2 =
        auctionLotService.bid(auctionLot2.getId(), testData.user3().getUsername(), 10,
            127.00);

    var find1 =
        format("winningBids.find { it.ownerUsername == '%s' }.", testData.user2().getUsername());
    var find2 =
        format("winningBids.find { it.ownerUsername == '%s' }.", testData.user3().getUsername());

    given().baseUri(uri).header(AUTHORIZATION, testData.user1Token())
        .pathParam("id", auctionLot2.getId()).when().put("auctions/{id}").then()
        .statusCode(HttpStatus.OK.value()).log().all()
        .body(find1 + "ownerUsername", equalTo(bid1.getUser()))
        .body(find1 + "quantity", equalTo(bid1.getQuantity()))
        .body(find1 + "price", equalTo(125.00F))
        .body(find2 + "ownerUsername", equalTo(bid2.getUser()))
        .body(find2 + "quantity", equalTo(bid2.getQuantity()))
        .body(find2 + "price", equalTo(127.00F)).body("totalSoldQuantity", equalTo(20))
        .body("totalRevenue", equalTo(2520F));
  }

  @DisplayName("closeAuction should throw if not owner trying to close")
  @Test
  public void closeAuctionShouldThrowIfNotOwner() {
    AuctionLot auctionLot2=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

    given().baseUri(uri).header(AUTHORIZATION, testData.user2Token())
        .pathParam("id", auctionLot2.getId()).when().put("auctions/{id}").then()
        .statusCode(HttpStatus.UNAUTHORIZED.value());

  }

  @DisplayName("closeAuction should throw if Auction is already closed")
  @Test
  public void closeAuctionShouldThrowIfAuctionIsClosed() {
    AuctionLot auctionLot1=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

    auctionLot1.close();
    String ownerToken= testData.auctionUserToken(auctionLot1.getOwner());

    //@formatter:off
    given()
        .baseUri(uri)
        .header(AUTHORIZATION, ownerToken)
        .pathParam("id", auctionLot1.getId())
        .when()
        .put("auctions/{id}").then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
    //@formatter:on

  }

  @DisplayName("getClosingSummary should return closing summary if is closed and is owner")
  @Test
  public void getClosingSummaryShouldReturnClosingSummary() {
    AuctionLot auctionLot2=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

    var bid1 =
        auctionLotService.bid(auctionLot2.getId(), testData.user2().getUsername(), 10,
            125.00);
    var bid2 =
        auctionLotService.bid(auctionLot2.getId(), testData.user3().getUsername(), 10,
            127.00);


    var find1 =
        format("winningBids.find { it.ownerUsername == '%s' }.", testData.user2().getUsername());
    var find2 =
        format("winningBids.find { it.ownerUsername == '%s' }.", testData.user3().getUsername());

    auctionLot2.close();

    //@formatter:off
    given().baseUri(uri).header(AUTHORIZATION, testData.user1Token())
        .pathParam("id", auctionLot2.getId()).when().get("auctions/{id}/ClosingSummary")
        .then().statusCode(HttpStatus.OK.value())
        .body(find1 + "ownerUsername", equalTo(bid1.getUser()))
        .body(find1 + "quantity", equalTo(bid1.getQuantity()))
        .body(find1 + "price", equalTo(125.00F))
        .body(find2 + "ownerUsername", equalTo(bid2.getUser()))
        .body(find2 + "quantity", equalTo(bid2.getQuantity()))
        .body(find2 + "price", equalTo(127.00F)).body("totalSoldQuantity", equalTo(20))
        .body("totalRevenue", equalTo(2520F));
    //@formatter:on
  }

  @DisplayName("getClosingSummary should throw if not owner of the auction")
  @Test
  public void getClosingSummaryShouldReturnThrowIfNotOwner() {
    AuctionLot auctionLot2=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

   auctionLot2.close();
    //@formatter:off
    given()
      .baseUri(uri)
      .header(AUTHORIZATION, testData.user2Token())
      .pathParam("id", auctionLot2.getId())
    .when()
      .get("auctions/{id}/ClosingSummary")
    .then()
      .statusCode(HttpStatus.UNAUTHORIZED.value());
    //@formatter:on
  }

  @DisplayName("getClosingSummary should throw if auction is not closed")
  @Test
  public void getClosingSummaryShouldThrowIfNotClosed() {
    AuctionLot auctionLot1=auctionLotService.create(testData.user1().getUsername(),"FB",100,20.00);

    String ownerToken= testData.auctionUserToken(auctionLot1.getOwner());

    //@formatter:off
    given()
        .baseUri(uri)
        .header(AUTHORIZATION, ownerToken)
        .pathParam("id", auctionLot1.getId())
    .when()
        .get("auctions/{id}/ClosingSummary")
    .then()
        .statusCode(HttpStatus.BAD_REQUEST.value());
    //@formatter:on
  }
}


