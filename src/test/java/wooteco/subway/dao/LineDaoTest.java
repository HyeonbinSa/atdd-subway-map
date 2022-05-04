package wooteco.subway.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import wooteco.subway.domain.Line;

public class LineDaoTest {

    @BeforeEach
    void beforEach() {
        LineDao.deleteAll();
    }

    @DisplayName("새 지하철 노선을 저장한다.")
    @Test
    void save() {
        Line testLine = new Line("test", "GREEN");
        Line result = LineDao.save(testLine);
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getColor()).isEqualTo("GREEN");
    }

    @DisplayName("지하철 노선 이름을 이용해 지하철 노선을 조회한다.")
    @Test
    void findByName() {
        Line test = new Line("test", "GREEN");
        LineDao.save(test);
        Line result = LineDao.findByName("test").orElse(null);
        Optional<Line> result2 = LineDao.findByName("test2");

        assertThat(result.getName()).isEqualTo("test");
        assertThat(result.getColor()).isEqualTo("GREEN");
        assertThat(result2).isEmpty();
    }

    @DisplayName("저장된 모든 지하철 노선을 조회한다.")
    @Test
    void findAll() {
        Line test1 = new Line("test1", "GREEN");
        Line test2 = new Line("test2", "YELLOW");
        LineDao.save(test1);
        LineDao.save(test2);

        List<Line> lines = LineDao.findAll();

        assertThat(lines.size()).isEqualTo(2);
        assertThat(lines.get(0).getId()).isEqualTo(1);
        assertThat(lines.get(0).getName()).isEqualTo("test1");
        assertThat(lines.get(0).getColor()).isEqualTo("GREEN");
        assertThat(lines.get(1).getId()).isEqualTo(2);
        assertThat(lines.get(1).getName()).isEqualTo("test2");
        assertThat(lines.get(1).getColor()).isEqualTo("YELLOW");
    }
}