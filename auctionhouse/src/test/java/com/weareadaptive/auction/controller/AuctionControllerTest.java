package com.weareadaptive.auction.controller;

import com.github.javafaker.Faker;
import com.weareadaptive.auction.TestData;
import com.weareadaptive.auction.controller.dto.AuctionResponse;
import com.weareadaptive.auction.controller.dto.CreateAuctionRequest;
import com.weareadaptive.auction.model.AuctionLot;
import com.weareadaptive.auction.model.AuctionState;
import com.weareadaptive.auction.service.AuctionLotService;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;

import static com.weareadaptive.auction.TestData.ADMIN_AUTH_TOKEN;
import static io.restassured.RestAssured.given;
import static java.lang.String.format;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToObject;
import static org.hamcrest.Matchers.greaterThan;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpStatus.NOT_FOUND;

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
      .body("id", greaterThan(0))
      .body("minPrice", equalTo(10.0F))
      .body("symbol", equalTo("FB"))
      .body("quantity", equalTo(100));
    //.body("status", equalTo());
    //@formatter:on
  }


  @DisplayName("create should throw an Authoro")
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
      .body( find1+"owner", equalTo(testData.user1().getUsername()))
      .body(find1+"symbol", equalTo("ORANGE"))
      .body(find1+"quantity", equalTo(34))
      .body(find1+"minPrice", equalTo(45.78F));
  }

  @DisplayName("Should return Auction with given Id")
  @Test
  public void getAuctionByIdShouldReturnAuction(){
     AuctionLot auctionLot=auctionLotService.create(testData.user1().getUsername(),"ORANGE",45,33.65);

    given()
      .baseUri(uri)
      .header(AUTHORIZATION,testData.user1Token())
      .pathParam("id", auctionLot.getId())
      .when()
      .get("auctions/{id}")
      .then()
      .statusCode(HttpStatus.OK.value())
      .body("owner",equalTo(testData.user1().getUsername()))
      .body("symbol", equalTo("ORANGE"))
      .body("quantity", equalTo(45))
      .body("minPrice", equalTo(33.65F));
  }
@DisplayName("get Auction by Id should throw if auction does not Exit")
@Test
  public void  getAuctionByIdShouldThrowIfAuctionDoesNotExist(){
    given()
      .baseUri(uri)
      .header(AUTHORIZATION,testData.user1Token())
      .pathParam("id", INVALID_AUCTION_ID)
      .when()
      .get("auctions/{id}")
      .then()
      .statusCode(NOT_FOUND.value());
  }

  public void bidShouldReturnBidIfBidIsValid(){

  }


}
