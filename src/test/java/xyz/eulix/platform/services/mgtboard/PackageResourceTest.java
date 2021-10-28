package xyz.eulix.platform.services.mgtboard;

import static io.restassured.RestAssured.given;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.util.Objects;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import xyz.eulix.platform.services.mgtboard.dto.PackageCheckRes;
import xyz.eulix.platform.services.mgtboard.dto.PackageReq;
import xyz.eulix.platform.services.mgtboard.dto.PackageRes;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PackageResourceTest {
  PackageReq app_1_0 = PackageReq.of("app-1", "ios", "1.0", 1000L,
      "http://app1.0", "ios app 1.0 版本", "12345678901234567890123456789012", true, "0",
      "0", "1.0");
  PackageReq app_2_0 = PackageReq.of("app-1", "ios", "2.0", 1000L,
      "http://app2.0", "ios app 1.0 版本", "12345678901234567890123456789012", true, "0",
      "0", "2.0");
  PackageReq box_1_0 = PackageReq.of("box-1", "box", "1.0", 1000L,
      "http://box1.0", "box 1.0 版本", "12345678901234567890123456789012", true, "1.0",
      "1.0", "0");
  PackageReq box_2_0 = PackageReq.of("box-1", "box", "2.0", 1000L,
      "http://box2.0", "box 1.0 版本", "12345678901234567890123456789012", true, "2.0",
      "2.0", "0");

  @Test
  @Order(2)
  void appPkgCheckAppTest() {

    given()
        .header("Request-Id", "uuid")
        .body(app_1_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");
    given()
        .header("Request-Id", "uuid")
        .body(box_1_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");

    final Response resp = given()
        .header("Request-Id", "uuid")
        .queryParam("action","app_check")
        .queryParam("app_name","app-1")
        .queryParam("box_name","box-1")
        .queryParam("pkg_type","ios")
        .queryParam("app_type","ios")
        .queryParam("cur_box_version","1.0")
        .queryParam("cur_app_version","1.0")
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/api/package/check");
    PackageCheckRes res = resp.body().as(PackageCheckRes.class);

    assert res.getNewVersionExist();
  }

  @Test
  @Order(11)
  void appPkgCheckAppUpdateBoxNotCompatible() {


    given()
        .header("Request-Id", "uuid")
        .body(app_1_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");


    given()
        .header("Request-Id", "uuid")
        .body(app_2_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");

    given()
        .header("Request-Id", "uuid")
        .body(box_1_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");

    given()
        .header("Request-Id", "uuid")
        .body(box_2_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");


    final Response resp = given()
        .header("Request-Id", "uuid")
        .queryParam("action","app_check")
        .queryParam("app_name","app-1")
        .queryParam("box_name","box-1")
        .queryParam("pkg_type","ios")
        .queryParam("app_type","ios")
        .queryParam("cur_box_version","1.0")
        .queryParam("cur_app_version","1.0")
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/api/package/check");
    PackageCheckRes res = resp.body().as(PackageCheckRes.class);

    assert res.getNewVersionExist();
  }

  @Test
  @Order(12)
  void appPkgCheckTestBox() {

    given()
        .header("Request-Id", "uuid")
        .body(box_1_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");


    given()
        .header("Request-Id", "uuid")
        .body(box_2_0)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");

    final Response resp = given()
        .header("Request-Id", "uuid")
        .queryParam("action","box_check")
        .queryParam("box_name","box-1")
        .queryParam("pkg_type","box")
        .queryParam("app_name", "app-1")
        .queryParam("app_type","ios")
        .queryParam("cur_box_version","1.0")
        .queryParam("cur_app_version","1.0")
        .contentType(ContentType.JSON)
        .when()
        .get("/v1/api/package/check");
    PackageCheckRes res = resp.body().as(PackageCheckRes.class);

    assert res.getNewVersionExist();
  }

  @Test
  @Order(1)
  void appPkgSaveTest() {

    PackageRes packageRes = add(app_1_0);

    assert Objects.equals(packageRes.getPkgName(), app_1_0.getPkgName());
  }


  @Test
  @Order(3)
  void appPkgUpdateTest() {

    String url = "http://test";
    app_1_0.setDownloadUrl(url);
    final Response resp1 = given()
        .header("Request-Id", "uuid")
        .body(app_1_0)
        .contentType(ContentType.JSON)
        .when()
        .put("/v1/api/package");

    PackageRes packageRes = resp1.body().as(PackageRes.class);
    assert Objects.equals(packageRes.getDownloadUrl(), url);

  }

  @Test
  @Order(4)
  void appPkgDelTest() {


    given()
        .header("Request-Id", "uuid")
        .queryParam("pkg_name","app-1")
        .queryParam("pkg_type","ios")
        .queryParam("pkg_version","1.0")
        .contentType(ContentType.JSON)
        .when()
        .delete("/v1/api/package")
        .then()
        .statusCode(OK.getStatusCode())
        .body(containsString("true"));
  }

  public PackageRes add(PackageReq req){
    final Response resp = given()
        .header("Request-Id", "uuid")
        .body(req)
        .contentType(ContentType.JSON)
        .when()
        .post("/v1/api/package");

     return resp.body().as(PackageRes.class);
  }

}
