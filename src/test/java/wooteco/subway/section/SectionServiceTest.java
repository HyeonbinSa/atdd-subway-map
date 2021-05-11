package wooteco.subway.section;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import wooteco.subway.exception.ImpossibleDeleteException;
import wooteco.subway.exception.ImpossibleDistanceException;
import wooteco.subway.exception.NoSuchStationInLineException;
import wooteco.subway.line.Line;
import wooteco.subway.line.LineService;
import wooteco.subway.station.Station;
import wooteco.subway.station.StationResponse;
import wooteco.subway.station.StationService;

@SpringBootTest
@Transactional
@Sql("classpath:test-schema.sql")
class SectionServiceTest {
    private static final String stationName1 = "강남역";
    private static final String stationName2 = "서초역";
    private static final String stationName3 = "잠실역";
    private static final String stationName4 = "매봉역";

    @Autowired
    private SectionService sectionService;

    @Autowired
    private StationService stationService;

    @Autowired
    private LineService lineService;

    @BeforeEach
    void setUp() {
        Station station1 = new Station(stationName1);
        Station station2 = new Station(stationName2);
        Station station3 = new Station(stationName3);
        Station station4 = new Station(stationName4);

        stationService.createStation(stationName1);
        stationService.createStation(stationName2);
        stationService.createStation(stationName3);
        stationService.createStation(stationName4);

        lineService.createLine(new Line(
            new Line("2호선", "green"), Arrays.asList(station1, station2, station3, station4)));
    }

    @DisplayName("구간을 생성한다.")
    @Test
    void createSection() {
        Section section = new Section(1L, 1L, 2L, 100);
        Section section1 = new Section(1L, 2L, 3L, 100);

        assertEquals(1L, sectionService.createSection(section));
        assertEquals(2L, sectionService.createSection(section1));
    }

    @DisplayName("구간 추가 시 거리가 맞지 않으면 에러가 발생한다.")
    @Test
    void addSectionException() {
        Section section = new Section(1L, 1L, 3L, 100);
        Section section1 = new Section(1L, 2L, 3L, 1000);

        assertEquals(1L, sectionService.createSection(section));
        assertThatThrownBy(() -> sectionService.addSection(section1)).isInstanceOf(ImpossibleDistanceException.class);
    }

    @DisplayName("상행종점, 하행종점을 추가한다.")
    @Test
    void addEndSection() {
        Section section = new Section(1L, 1L, 2L, 100);
        Section endSection = new Section(1L, 2L, 3L, 1000);
        Section startSection = new Section(1L, 4L, 1L, 1000);

        assertEquals(1L, sectionService.createSection(section));
        assertEquals(2L, sectionService.addSection(endSection));
        assertEquals(3L, sectionService.addSection(startSection));
        assertEquals(stationName1, sectionService.makeOrderedStations(1L).get(0).getName());
        assertEquals(stationName2, sectionService.makeOrderedStations(1L).get(1).getName());
        assertEquals(stationName3, sectionService.makeOrderedStations(1L).get(2).getName());
        assertEquals(stationName4, sectionService.makeOrderedStations(1L).get(3).getName());
    }

    @DisplayName("구간을 삭제한다.")
    @Test
    void deleteSection() {
        addEndSection();
        assertEquals(1, sectionService.deleteSectionByStationId(1L, 2L));
    }

    @DisplayName("종점 구간을 삭제한다.")
    @Test
    void deleteEndSection() {
        addEndSection();
        assertEquals(1, sectionService.deleteSectionByStationId(1L, 4L));
        assertEquals(1, sectionService.deleteSectionByStationId(1L, 1L));
    }

    @DisplayName("존재하지 않는 역을 삭제한다.")
    @Test
    void deleteSectionException() {
        addEndSection();
        stationService.createStation("메롱역");

        assertThatThrownBy(() -> sectionService.deleteSectionByStationId(1L, 5L))
            .isInstanceOf(NoSuchStationInLineException.class);
    }

    @DisplayName("하나 남은 구간을 삭제한다.")
    @Test
    void ImpossibleDeleteSectionException() {
        addEndSection();

        assertEquals(1, sectionService.deleteSectionByStationId(1L, 4L));
        assertEquals(1, sectionService.deleteSectionByStationId(1L, 1L));

        assertThatThrownBy(() -> sectionService.deleteSectionByStationId(1L, 2L))
            .isInstanceOf(ImpossibleDeleteException.class);
    }
}