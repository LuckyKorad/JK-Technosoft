package tests;

import config.BaseTest;
import io.qameta.allure.Description;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class BookApiTest extends BaseTest {
    private static int bookId;
    private static String token;

    @BeforeClass
    public void setupUserAndLogin() {
        String email = "apitestuser@test.com";
        String password = "password123";

        //  Try signup
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":0," +
                  "\"email\":\"" + email + "\"," +
                  "\"password\":\"" + password + "\"}")
        .when()
            .post("/signup")
        .then()
            .statusCode(anyOf(is(200), is(400))); // 200=new user, 400=already exists

        //  Login
        token =
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"))
            .extract()
            .path("access_token");
    }

    // ---------------- POSITIVE TESTS ----------------

    @Test(priority = 1)
    @Description("Positive: Create a new book")
    public void createBook() {
        bookId =
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"id\":0," +
                  "\"name\":\"Test Book\"," +
                  "\"author\":\"John Doe\"," +
                  "\"published_year\":2024," +
                  "\"book_summary\":\"A test summary\"}")
        .when()
            .post("/books/")
        .then()
            .statusCode(anyOf(is(200), is(201)))  // some APIs return 200 instead of 201
            .header("Content-Type", containsString("application/json"))
            .body("name", equalTo("Test Book"))
            .extract().path("id");
    }

    @Test(priority = 2)
    @Description("Positive: Get created book")
    public void getBook() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/books/" + bookId)
        .then()
            .statusCode(200)
            .header("Content-Type", containsString("application/json"))
            .body("id", equalTo(bookId))
            .body("name", equalTo("Test Book"));
    }

    @Test(priority = 3)
    @Description("Positive: Update book details")
    public void updateBook() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"id\":" + bookId + "," +
                  "\"name\":\"Updated Book\"," +
                  "\"author\":\"Jane Doe\"," +
                  "\"published_year\":2025," +
                  "\"book_summary\":\"Updated summary\"}")
        .when()
            .put("/books/" + bookId)
        .then()
            .statusCode(200)
            .body("name", equalTo("Updated Book"));
    }

    @Test(priority = 4)
    @Description("Positive: Delete book and verify deletion")
    public void deleteBook() {
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .delete("/books/" + bookId)
        .then()
            .statusCode(200);

        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/books/" + bookId)
        .then()
            .statusCode(404);
    }

    // ---------------- NEGATIVE TESTS ----------------

    @Test(priority = 5)
    @Description("Negative: Create book without auth should fail")
    public void createBookWithoutAuth() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"id\":0,\"name\":\"Unauthorized Book\",\"author\":\"X\",\"published_year\":2024,\"book_summary\":\"X\"}")
        .when()
            .post("/books/")
        .then()
            .statusCode(anyOf(is(401), is(403))); // accept both
    }

    @Test(priority = 6)
    @Description("Negative: Get non-existing book should return 404")
    public void getNonExistingBook() {
        int fakeId = 999999; // unlikely to exist
        given()
            .header("Authorization", "Bearer " + token)
        .when()
            .get("/books/" + fakeId)
        .then()
            .statusCode(404);
    }

    @Test(priority = 7)
    @Description("Negative: Create book with missing fields should fail")
    public void createBookWithMissingFields() {
        given()
            .header("Authorization", "Bearer " + token)
            .contentType(ContentType.JSON)
            .body("{\"id\":0,\"name\":\"Incomplete Book\"}") // missing fields
        .when()
            .post("/books/")
        .then()
            .statusCode(anyOf(is(422), is(500))); // 422 ideal, 500 if API crashes
    }

    @Test(priority = 8)
    @Description("Negative: Login with wrong password should fail")
    public void loginWithWrongPassword() {
        given()
            .contentType(ContentType.JSON)
            .body("{\"email\":\"apitestuser@test.com\",\"password\":\"wrongpass\"}")
        .when()
            .post("/login")
        .then()
            .statusCode(anyOf(is(400), is(401))); // depending on backend
    }
}
