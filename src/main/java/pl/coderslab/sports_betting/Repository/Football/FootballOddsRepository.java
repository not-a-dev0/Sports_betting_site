package pl.coderslab.sports_betting.Repository.Football;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.coderslab.sports_betting.Entity.Football.FootballOdds;

@Repository
public interface FootballOddsRepository extends JpaRepository<FootballOdds, Long> {
}
