package wooteco.subway.acceptance;

import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import wooteco.subway.dto.LineRequest;
import wooteco.subway.dto.LineResponse;
import wooteco.subway.dto.StationRequest;
import wooteco.subway.dto.StationResponse;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    private static final LineRequest lineRequest1 = new LineRequest("2호선", "GREEN", 1L, 2L, 20);
    private static final LineRequest lineRequest2 = new LineRequest("3호선", "ORANGE", 2L, 3L, 20);

    @BeforeEach
    void setup() {
        createStationByMap(new StationRequest("신설동역"));
        createStationByMap(new StationRequest("용두역"));
        createStationByMap(new StationRequest("성수역"));
    }

    @DisplayName("지하철 노선을 생성 성공 시 상태코드 201을 반환하고 Location 헤더에 주소를 전달한다.")
    @Test
    void createLine() {
        // given
        ExtractableResponse<Response> response = createLineFixture(lineRequest1);
        // then

        List<String> expectedStationNames = List.of("신설동역","용두역");
        List<String> resultStationNames = response.jsonPath().getList("stations", StationResponse.class).stream()
            .map(StationResponse::getName)
            .collect(Collectors.toList());
        assertThat(resultStationNames).containsAll(expectedStationNames);
        assertThat(response.jsonPath().getString("name")).isEqualTo("2호선");
        assertThat(response.jsonPath().getString("color")).isEqualTo("GREEN");
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.header("Location")).isNotBlank();
    }

    @DisplayName("기존에 존재하는 지하철 노선 이름으로 지하철 노선을 생성 시 상태코드 400을 반환한다.")
    @Test
    void createLineWithDuplicateName() {
        // given
        ExtractableResponse<Response> createResponse1 = createLineFixture(lineRequest1);
        // when
        ExtractableResponse<Response> createResponse2 = createLineFixture(lineRequest1);
        // then
        assertThat(createResponse2.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }


    @DisplayName("지하철 노선 목록을 조회 시 상태코드 200을 반환하고 노선 목록을 반환한다.")
    @Test
    void getLines() {
        /// given
        ExtractableResponse<Response> createResponse1 = createLineFixture(lineRequest1);
        ExtractableResponse<Response> createResponse2 = createLineFixture(lineRequest2);
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .get("/lines")
            .then().log().all()
            .extract();
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<Long> expectedLineIds = Stream.of(createResponse1, createResponse2)
            .map(it -> Long.parseLong(it.header("Location").split("/")[2]))
            .collect(Collectors.toList());
        List<Long> resultLineIds = response.jsonPath().getList(".", LineResponse.class).stream()
            .map(LineResponse::getId)
            .collect(Collectors.toList());
        assertThat(resultLineIds).containsAll(expectedLineIds);
    }

    @DisplayName("노선 id를 통해 지하철 노선을 조회하며 성공 시 노선과 상태코드 200을 반환한다.")
    @Test
    void getLineById() {
        /// given
        ExtractableResponse<Response> createdResponse = createLineFixture(lineRequest1);
        String uri = createdResponse.header("Location");
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .get(uri)
            .then().log().all()
            .extract();
        // then
        assertThat(response.jsonPath().getString("name")).isEqualTo("2호선");
        assertThat(response.jsonPath().getString("color")).isEqualTo("GREEN");
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("노선을 수정 성공 시 상태 코드 200을 반환한다.")
    @Test
    void updateLine() {
        /// given
        ExtractableResponse<Response> createdResponse = createLineFixture(lineRequest1);
        String uri = createdResponse.header("Location");

        LineRequest updateRequest = new LineRequest("3호선", "ORANGE");
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(updateRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put(uri)
            .then().log().all()
            .extract();
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    @DisplayName("존재하지 않는 ID의 노선을 수정 상태 코드 400을 반환한다..")
    @Test
    void updateLine_noExistLine_Exception() {
        /// given
        ExtractableResponse<Response> createdResponse1 = createLineFixture(lineRequest1);
        ExtractableResponse<Response> createdResponse2 = createLineFixture(lineRequest2);
        LineRequest updateRequest = new LineRequest("4호선", "ORANGE");
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(updateRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put("/lines/10000")
            .then().log().all()
            .extract();
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }
//
    @DisplayName("중복된 이름으로 노선 수정 시 상태 코드 400을 반환한다.")
    @Test
    void updateLine_duplicateName_Exception() {
        /// given
        ExtractableResponse<Response> createdResponse1 = createLineFixture(lineRequest1);
        ExtractableResponse<Response> createdResponse2 = createLineFixture(lineRequest2);

        String uri = createdResponse2.header("Location");

        LineRequest updateRequest = new LineRequest("2호선", "BLUE");
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(updateRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put(uri)
            .then().log().all()
            .extract();
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("중복된 색깔로 노선 수정 시 상태 코드 400을 반환한다.")
    @Test
    void updateLine_duplicateColor_Exception() {
        /// given
        ExtractableResponse<Response> createdResponse1 = createLineFixture(lineRequest1);
        ExtractableResponse<Response> createdResponse2 = createLineFixture(lineRequest2);

        String uri = createdResponse2.header("Location");

        LineRequest updateRequest = new LineRequest("4호선", "GREEN");
        // when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .body(updateRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .put(uri)
            .then().log().all()
            .extract();
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("노선 제거 성공 시 상태 코드 204를 반환한다.")
    @Test
    void deleteLine() {
        // given
        ExtractableResponse<Response> createResponse = createLineFixture(lineRequest1);
        // when
        String uri = createResponse.header("Location");
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .delete(uri)
            .then().log().all()
            .extract();
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("존재하지 않는 ID의 노선을 제거 시 상태 코드 200을 반환한다.")
    @Test
    void deleteLine_noExistLine_Exception() {
        /// given & when
        ExtractableResponse<Response> response = RestAssured.given().log().all()
            .when()
            .delete("/lines/1000")
            .then().log().all()
            .extract();
        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }


    private ExtractableResponse<Response> createStationByMap(StationRequest stationRequest) {
        return RestAssured.given().log().all()
            .body(stationRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/stations")
            .then().log().all()
            .extract();
    }

    private ExtractableResponse<Response> createLineFixture(LineRequest lineRequest) {
        // when
        return RestAssured.given().log().all()
            .body(lineRequest)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .when()
            .post("/lines")
            .then().log().all()
            .extract();
    }
}
