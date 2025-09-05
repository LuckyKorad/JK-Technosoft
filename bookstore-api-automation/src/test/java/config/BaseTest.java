package config;

import org.testng.annotations.BeforeClass;

import io.restassured.RestAssured;

public class BaseTest {
	 @BeforeClass
	    public void setup() {
	        // Point this to your FastAPI server (running locally)
	        RestAssured.baseURI = "http://localhost:8000";  
	    }
}
