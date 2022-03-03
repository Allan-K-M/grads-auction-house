package com.weareadaptive.auction.controller;

import static com.weareadaptive.auction.TestData.ADMIN_AUTH_TOKEN;
import static com.weareadaptive.auction.TestData.USER_AUTH_TOKEN;
import com.github.javafaker.Faker;
import com.weareadaptive.auction.TestData;
import com.weareadaptive.auction.controller.dto.CreateAuctionRequest;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.User;
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
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AuctionControllerTest {
  public static final int INVALID_AUCTION_ID = 999999;
  private final Faker faker = new Faker();
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
      .body("id",greaterThan(0))
      .body("minPrice", equalTo(10.0F))
      .body("symbol", equalTo("FB"))
      .body("quantity", equalTo(100));
        //.body("status", equalTo());
    //@formatter:on
  }

  @DisplayName("should throw Business Exception if Auction already exists")
  @Test
  public void shouldThrowIfCreatedAuctionExists(){
 //create auction eveerytime with real values to be coherent
   String symbol=testData.auctionLot1().getSymbol();
   User owner=testData.auctionLot1().getOwner();
   double minPrice=testData.auctionLot1().getMinPrice();
   int quantity=testData.auctionLot1().getQuantity();
    AuctionLot.Status status=testData.auctionLot1().getStatus();

   var createAuctionRequest=new CreateAuctionRequest(symbol,minPrice,quantity);
    //@formatter:off
      given()
        .baseUri(uri)
        .header(AUTHORIZATION, testData.getToken(owner))
        .contentType(ContentType.JSON)
        .body(createAuctionRequest)
      .when()
        .post("/auctions")
      .then()
      .statusCode(HttpStatus.BAD_REQUEST.value())
      .body("message", containsString("already exist"));

  }

  @DisplayName("Should throw an Authoro")
  @Test
  public void shouldThrowIfUserCreatingAuctionIsBlocked(){
    testData.user2().block();
    var name = faker.name();
    var quantity = faker.number().numberBetween(2,100);
    var minCost=faker.number().randomDouble(2,2,100);

    var createAuctionRequest = new CreateAuctionRequest(faker.animal().toString(), minCost, quantity);

      given()
        .baseUri(uri)
        .header(AUTHORIZATION, testData.getToken(testData.user2()))
        .contentType(ContentType.JSON)
        .body(createAuctionRequest)
      .when()
        .post("/auction")
      .then()
      .statusCode(HttpStatus.BAD_REQUEST.value())
      .body("message", containsString("already exist"));



  }

}
