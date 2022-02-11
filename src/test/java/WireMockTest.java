
import com.github.tomakehurst.wiremock.WireMockServer;
import io.restassured.response.Response;
import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static io.restassured.RestAssured.given;

enum LOCATIONS {
    LON, MAN, CAM, LCS
}

public class WireMockTest {

    WireMockServer wireMockServer;
    bean restBean = new bean();

    public Properties prop;
    String path = System.getProperty("user.dir");
    List<String> expectedList = null;
    ArrayList actualList = null;
    InputStream postFilePath = null;

    public WireMockTest() {
        try {
            prop = new Properties();
            String propertiesFilePath = path + "/data.properties";
            FileInputStream file = new FileInputStream(propertiesFilePath);
            prop.load(file);
            restBean.setId(prop.getProperty("id"));
            restBean.setCost(prop.getProperty("cost"));
            restBean.setLocation(prop.getProperty("locationName"));
            restBean.setFileName(prop.getProperty("fileName"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @BeforeEach
    public void setup() {
        wireMockServer = new WireMockServer(8090);
        wireMockServer.start();
    }

    @AfterEach
    public void teardown() {
        wireMockServer.stop();
    }

    /**
     * stubbing for /location/get/all
     */
    public void setupStub() {
        wireMockServer.stubFor(get(urlEqualTo("/location/get/all"))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBodyFile("json/locations.json")));
    }

    /**
     * stubbing for /location/get/{LOCATION_CODE}
     *
     * @param file
     * @param locationName
     */
    public void setupStubForLocation(String file, String locationName) {
        wireMockServer.stubFor(get(urlEqualTo("/location/get/" + locationName + ""))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBodyFile("json/" + file + ".json")));

    }

    /**
     * stubbing for post request
     */
    public void stubForPOst() {
        wireMockServer.stubFor(post(urlEqualTo("/location/get/all"))
                .willReturn(aResponse().withHeader("Content-Type", "text/plain")
                        .withStatus(200)
                        .withBodyFile("json/locations.json")));

    }

    /**
     * Get all items and validate that LON, MAN, CAM and LCS were returned in the
     * response
     */
    @Test
    public void validateGetAllResponse() {
        setupStub();
        String key = "items";
        expectedList = new ArrayList<>();
        for (LOCATIONS listExpected : LOCATIONS.values()) {
            expectedList.add(listExpected.toString());
        }
        Response response = given().
                when().
                get("http://localhost:8090/location/get/all").
                then().extract().response();
        System.out.println(response.getBody().asString());
        int statusCode = response.getStatusCode();
        Assert.assertEquals(statusCode, 200);

        List<HashMap<String, Object>> list = response.jsonPath().getList(key);
        actualList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {

            HashMap<String, Object> locationDetails = list.get(i);
            String locationId = (String) locationDetails.get("location");
            System.out.println(locationId);
            actualList.add(locationId);

        }

        Assert.assertEquals(expectedList, actualList);
    }

    /**
     * test to validate the non-existent locations
     */
    @Test
    public void validateNonExistance() {
        given().
                when().
                get("http://localhost:8090/location/endpoint").
                then().
                assertThat().statusCode(404);
    }

    /**
     * Get each item (LON, MAN, CAM and LCS) individually and validate the response
     */
    @Test
    public void validateLocations() {
        String fileName = restBean.getFileName();
        actualList = new ArrayList();
        expectedList = new ArrayList();
        expectedList.add(restBean.getCost().substring(1));
        expectedList.add(restBean.getLocation());
        expectedList.add(restBean.getId());
        setupStubForLocation(fileName, restBean.getLocation());
        Response response = given().when().
                pathParam("LOCATION_CODE", restBean.getLocation()).get("http://localhost:8090/location/get/{LOCATION_CODE}").
                then().extract().response();
        System.out.println(response.getBody().asString());
        List<HashMap<String, Object>> list = response.jsonPath().getList("items");
        for (HashMap<String, Object> mylist : list) {
            for (HashMap.Entry<String, Object> entry : mylist.entrySet()) {
                actualList.add(entry.getValue());
            }
        }
        Assert.assertEquals(expectedList, actualList);
    }

    /**
     * Write a test that would validate new item addition using POST and the response
     * returns all the items.
     */
    @Test
    public void postData() throws IOException {
        stubForPOst();
        String file = path +  "/src/test/resources/post_Location.json";
        String jsonBody = generateStringFromResource(file);
        Response response = given().contentType("application/json").body(jsonBody).
                when().post("http://localhost:8090/location/get/all").then().extract().response();
        System.out.println(response.getBody().asString());
    }

    private String generateStringFromResource(String path) throws IOException {

        return new String(Files.readAllBytes(Paths.get(path)));

    }
}
