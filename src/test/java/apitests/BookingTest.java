package apitests;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.*;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;

import java.util.List;

import static io.restassured.RestAssured.baseURI;

public class BookingTest {

    private int storedBookingId;
    private String bookingResponseBody;
    private String authToken;
    @BeforeClass
    public void beforeclass(){
        baseURI= "https://restful-booker.herokuapp.com/";
    }

    @Test
    public void createToken() {
        Response response = RestAssured.given()
                .contentType("application/json")
                .accept("application/json")
                .body("{\n" +
                        "  \"username\": \"admin\",\n" +
                        "  \"password\": \"password123\"\n" +
                        "}")
                .when()
                .post("https://restful-booker.herokuapp.com/auth");

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");

        authToken = response.jsonPath().getString("token");
        Assert.assertNotNull(authToken, "Token is null!");

        System.out.println("Generated Token: " + authToken);
    }

    public String getAuthToken() {
        return authToken;
    }
    @Test
    public void getBookingIds() {
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("booking");

        System.out.println("GET Booking IDs Status Code: " + response.statusCode());
        Assert.assertEquals(response.statusCode(), 200);

        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);

        List<Integer> bookingIds = response.jsonPath().getList("bookingid");
        Assert.assertEquals(response.statusCode(), 200);
        storedBookingId = bookingIds.get(0);
        System.out.println("Stored Booking ID: " + storedBookingId);

    }

    @Test(dependsOnMethods = "getBookingIds")
    public void getBookingById() {
        Response response = given()
                .accept(ContentType.JSON)
                .log().uri()
                .when()
                .get("booking/" + storedBookingId);

        System.out.println("GET Booking Status Code: " + response.statusCode());
        Assert.assertEquals(response.statusCode(), 200);
        bookingResponseBody = response.getBody().asString();

        System.out.println("Response Body: " + response.getBody().asString());
    }


    @Test(dependsOnMethods = "getBookingById")
    public void createBooking() {

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(bookingResponseBody)
                .when()
                .post("booking");

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");

        System.out.println("Response Body: " + response.getBody().asString());

        String bookingId = response.jsonPath().getString("bookingid");
        Assert.assertNotNull(bookingId, "Booking ID should not be null!");
        System.out.println("Created Booking ID: " + bookingId);
    }

    @Test(dependsOnMethods = "getBookingById")
    public void updateBooking() {

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                        "  \"firstname\": \"James\",\n" +
                        "  \"lastname\": \"Brown\",\n" +
                        "  \"totalprice\": 111,\n" +
                        "  \"depositpaid\": true,\n" +
                        "  \"bookingdates\": {\n" +
                        "    \"checkin\": \"2025-02-18\",\n" +
                        "    \"checkout\": \"2025-02-20\"\n" +
                        "  },\n" +
                        "  \"additionalneeds\": \"Breakfast\"\n" +
                        "}")
                .when()
                .put("booking/" + storedBookingId);


        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");

        System.out.println("Response Body (UpdateBooking): " + response.getBody().asString());
    }

    @Test(dependsOnMethods = "getBookingById")
    public void partialUpdateBooking() {

        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + getAuthToken())
                .body("{\n" +
                        "  \"firstname\": \"James\",\n" +
                        "  \"lastname\": \"Brown\"\n" +
                        "}")
                .when()
                .patch("booking/" + storedBookingId);

        Assert.assertEquals(response.statusCode(), 200, "Status code is not 200");

        System.out.println("Response Body (PartialUpdateBooking): " + response.getBody().asString());
    }

    @Test(dependsOnMethods = "createBooking")
    public void deleteBooking() {

        Response response = RestAssured.given()
                .accept(ContentType.JSON)
                .header("Authorization", "Bearer " + getAuthToken())
                .when()
                .delete("booking/" + storedBookingId);

        Assert.assertEquals(response.statusCode(), 201, "Status code is not 201");

        System.out.println("Response Body (DeleteBooking): " + response.getBody().asString());
    }

    @Test
    public void pingHealthCheck() {
        Response response = RestAssured.given()
                .accept("application/json")
                .when()
                .get(baseURI + "/ping");

        Assert.assertEquals(response.statusCode(), 201, "Status code is not 201");

        String healthStatus = response.jsonPath().getString("status");
        Assert.assertEquals(healthStatus, "Created", "Healthcheck status is not OK");

        System.out.println("Ping HealthCheck Response: " + response.getBody().asString());
    }
}
