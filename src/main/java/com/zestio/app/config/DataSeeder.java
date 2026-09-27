package com.zestio.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.zestio.app.competition.Competition;
import com.zestio.app.competition.CompetitionRepository;
import com.zestio.app.competition.enums.CompetitionLegFormat;
import com.zestio.app.competition.enums.CompetitionScope;
import com.zestio.app.competition.enums.CompetitionStatus;
import com.zestio.app.competition.enums.CompetitionType;
import com.zestio.app.match.Match;
import com.zestio.app.match.MatchRepository;
import com.zestio.app.match.enums.MatchPeriod;
import com.zestio.app.match.enums.MatchStatus;
import com.zestio.app.match.enums.MatchType;
import com.zestio.app.matchEvents.MatchEvent;
import com.zestio.app.matchEvents.MatchEventRepository;
import com.zestio.app.matchEvents.enums.EventType;
import com.zestio.app.matchLineup.LineupPlayer;
import com.zestio.app.matchLineup.MatchLineup;
import com.zestio.app.matchLineup.MatchLineupRepository;
import com.zestio.app.matchLineup.enums.Formation;
import com.zestio.app.matchLineup.enums.LineupStatus;
import com.zestio.app.matchLineup.enums.MissingReason;
import com.zestio.app.profile.Profile;
import com.zestio.app.profile.ProfileRepository;
import com.zestio.app.profile.enums.Position;
import com.zestio.app.profile.enums.Role;
import com.zestio.app.team.Team;
import com.zestio.app.team.TeamRepository;
import com.zestio.app.transfer.Transfer;
import com.zestio.app.transfer.TransferRepository;
import com.zestio.app.transfer.enums.TransferType;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final MatchEventRepository matchEventRepository;
    private final TransferRepository transferRepository;

    private final Random rng = new Random(42);
    private int phoneCounter = 700;

    private static final String[] FIRST_NAMES = {
            "James", "Daniel", "Michael", "Thomas", "Ryan", "Callum", "Harry", "Jack", "Sam", "Luke",
            "Oliver", "George", "Charlie", "Josh", "Ben", "Adam", "Connor", "Aaron", "Liam", "Ethan",
            "Mason", "Tyler", "Kyle", "Jamie", "Alex", "Marcus", "Andre", "Bruno", "Diego", "Rafael",
            "Carlos", "Mateo", "Luca", "Marco", "Nico", "Sven", "Lars", "Erik", "Viktor", "Pedro"
    };

    private static final String[] LAST_NAMES = {
            "Johnson", "Smith", "Williams", "Brown", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris",
            "Martin", "Clark", "Lewis", "Walker", "Young", "Allen", "King", "Wright", "Scott", "Green",
            "Baker", "Adams", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell",
            "Parker", "Evans", "Edwards", "Collins", "Stewart", "Morris", "Rogers", "Reed", "Cook", "Morgan"
    };

    private static final Position[] SQUAD_TEMPLATE = {
            Position.GK, Position.RB, Position.CB, Position.CB, Position.LB,
            Position.CDM, Position.CM, Position.CM, Position.RW, Position.ST, Position.LW,
            Position.GK, Position.CB, Position.LB, Position.CDM, Position.CAM, Position.CF, Position.ST,
            Position.RB, Position.CM
    };

    private static final Formation[] FORMATIONS = {
            Formation.F_4_3_3, Formation.F_4_2_3_1, Formation.F_4_4_2, Formation.F_3_5_2, Formation.F_4_1_4_1
    };

    private static final EventType[] GOAL_TYPES = {
            EventType.GOAL, EventType.GOAL, EventType.GOAL, EventType.GOAL,
            EventType.FREE_KICK_GOAL, EventType.LONG_RANGE_GOAL, EventType.PENALTY_GOAL
    };

    private static final java.util.Set<EventType> ASSISTABLE_GOAL_TYPES = java.util.Set.of(
            EventType.GOAL, EventType.FREE_KICK_GOAL
    );

    private static final int ASSIST_CHANCE_PERCENT = 70;

    private static final List<MatchPeriod> SEEDED_MATCH_PERIODS =
            List.of(MatchPeriod.FIRST_HALF, MatchPeriod.SECOND_HALF);

    // Track the current date for determining match status
    private final Instant now = Instant.now();

    @Override
    @Transactional
    public void run(String... args) {
        if (competitionRepository.count() > 0) {
            if (transferRepository.count() == 0) {
                seedTransfers(buildExistingSquads());
            }
            return;
        }

        Map<Long, List<Profile>> squadsByTeamId = new HashMap<>();

        // All competitions
        seedPremierLeague(squadsByTeamId);
        seedLaLiga(squadsByTeamId);
        seedLigue1(squadsByTeamId);
        seedSerieA(squadsByTeamId);
        seedBundesliga(squadsByTeamId);
        seedChampionsLeague(squadsByTeamId);

        // Player transfer history is seeded after all leagues and squads exist.
        seedTransfers(squadsByTeamId);
    }

    // -------------------------------------------------------------------------
    // Champions League
    // -------------------------------------------------------------------------

    private void seedChampionsLeague(Map<Long, List<Profile>> squadsByTeamId) {
        // 1. Create Competition
        Competition competition = new Competition();
        competition.setName("UEFA Champions League 2025/26");
        competition.setCompetitionCode("UCL");
        competition.setLogoUrl("https://example.com/logos/ucl.png");
        competition.setScope(CompetitionScope.INTERNATIONAL);
        competition.setStatus(CompetitionStatus.SCHEDULED);
        competition.setLegFormat(CompetitionLegFormat.DOUBLE);
        competition.setTotalTeams(32);
        competition.setCompetitionType(CompetitionType.LEAGUE);
        competition.setTotalRounds(8);
        competition.setStartDate(LocalDateTime.parse("2025-09-16T20:00:00"));
        competition.setEndDate(LocalDateTime.parse("2026-05-30T20:00:00"));
        competition.setCreatedAt(Instant.now());
        competition.setUpdatedAt(Instant.now());
        competition = competitionRepository.save(competition);

        // 2. Get top teams from each league (already seeded)
        List<Team> allTeams = teamRepository.findAll();
        
        // Select top 4 from each of the 5 leagues = 20 teams
        // Plus 12 more to make 32
        List<Team> uclTeams = new ArrayList<>();
        
        // For simplicity, take first 4 from each competition's teams
        // Since teams are stored sequentially, we need to be smarter
        // Let's select teams based on their ID order (first 4 from each competition)
        Map<String, List<Team>> teamsByCompetition = new HashMap<>();
        for (Team team : allTeams) {
            if (team.getCompetitions() != null && !team.getCompetitions().isEmpty()) {
                String compCode = team.getCompetitions().iterator().next().getCompetitionCode();
                teamsByCompetition.computeIfAbsent(compCode, k -> new ArrayList<>()).add(team);
            }
        }
        
        // Take top 4 from each league
        List<String> leagueCodes = Arrays.asList("EPL", "LFP", "L1", "LNPA", "BL");
        for (String code : leagueCodes) {
            List<Team> leagueTeams = teamsByCompetition.getOrDefault(code, new ArrayList<>());
            if (leagueTeams.size() >= 4) {
                uclTeams.addAll(leagueTeams.subList(0, 4));
            }
        }
        // Add 12 more teams from various leagues to reach 32
        for (Team team : allTeams) {
            if (uclTeams.size() >= 32) break;
            if (!uclTeams.contains(team)) {
                uclTeams.add(team);
            }
        }
        // Ensure we have exactly 32 teams
        while (uclTeams.size() < 32) {
            uclTeams.add(allTeams.get(rng.nextInt(allTeams.size())));
        }

        // 3. Build fixtures for Champions League (league phase)
        // Group stage: 8 groups of 4, round-robin
        List<MatchFixture> fixtures = new ArrayList<>();
        
        // Shuffle and assign to groups
        Collections.shuffle(uclTeams, rng);
        List<List<Team>> groups = new ArrayList<>();
        for (int i = 0; i < uclTeams.size(); i += 4) {
            groups.add(uclTeams.subList(i, Math.min(i + 4, uclTeams.size())));
        }
        
        // Generate group stage fixtures (each team plays each other twice - home and away)
        for (List<Team> group : groups) {
            if (group.size() == 4) {
                for (int i = 0; i < group.size(); i++) {
                    for (int j = i + 1; j < group.size(); j++) {
                        fixtures.add(new MatchFixture(group.get(i), group.get(j)));
                        fixtures.add(new MatchFixture(group.get(j), group.get(i)));
                    }
                }
            }
        }

        // 4. Generate up to 30 fixtures with dates between last month and this month,
        // guaranteeing every team gets at least one fixture before topping up randomly.
        List<MatchFixture> selectedFixtures = selectFixturesCoveringAllTeams(uclTeams, fixtures, 30);

        Instant matchDateCursor = getDateRangeStart();
        int fixtureCount = 0;

        for (MatchFixture fixture : selectedFixtures) {
            // Determine if match is in the past or future
            Instant matchDate = matchDateCursor;
            MatchStatus status = matchDate.isBefore(now) ? MatchStatus.FINISHED : MatchStatus.SCHEDULED;
            
            if (status == MatchStatus.FINISHED) {
                seedCompletedMatch(competition, fixture.home, fixture.away, matchDate, squadsByTeamId);
            } else {
                seedScheduledMatch(competition, fixture.home, fixture.away, matchDate);
            }
            
            matchDateCursor = matchDateCursor.plus(3, ChronoUnit.DAYS);
            fixtureCount++;
        }

        // 5. One upcoming/scheduled fixture
        if (fixtureCount < fixtures.size()) {
            MatchFixture nextFixture = fixtures.get(fixtureCount);
            seedScheduledMatch(competition, nextFixture.home, nextFixture.away, getFutureDate());
        } else {
            // If we used all fixtures, create a new one
            if (!uclTeams.isEmpty()) {
                seedScheduledMatch(competition, uclTeams.get(0), uclTeams.get(1), getFutureDate());
            }
        }
    }

    // -------------------------------------------------------------------------
    // Premier League (Updated Dates)
    // -------------------------------------------------------------------------

    private void seedPremierLeague(Map<Long, List<Profile>> squadsByTeamId) {
        Competition competition = new Competition();
        competition.setName("Premier League 2025/26");
        competition.setCompetitionCode("EPL");
        competition.setLogoUrl("https://example.com/logos/epl.png");
        competition.setScope(CompetitionScope.NATIONAL);
        competition.setStatus(CompetitionStatus.SCHEDULED);
        competition.setLegFormat(CompetitionLegFormat.DOUBLE);
        competition.setTotalTeams(20);
        competition.setCompetitionType(CompetitionType.LEAGUE);
        competition.setTotalRounds(38);
        competition.setStartDate(LocalDateTime.parse("2025-08-15T15:00:00"));
        competition.setEndDate(LocalDateTime.parse("2026-05-24T17:00:00"));
        competition.setCreatedAt(Instant.now());
        competition.setUpdatedAt(Instant.now());
        competition = competitionRepository.save(competition);

        List<Team> teams = Arrays.asList(
                createTeam("Arsenal", "ARS", "https://example.com/logos/ARS.png", "Emirates Stadium", competition),
                createTeam("Chelsea", "CHE", "https://example.com/logos/CHE.png", "Stamford Bridge", competition),
                createTeam("Manchester City", "MCI", "https://example.com/logos/MCI.png", "Etihad Stadium", competition),
                createTeam("Liverpool", "LIV", "https://example.com/logos/LIV.png", "Anfield", competition),
                createTeam("Tottenham Hotspur", "TOT", "https://example.com/logos/TOT.png", "Tottenham Hotspur Stadium", competition),
                createTeam("Manchester United", "MUN", "https://example.com/logos/MUN.png", "Old Trafford", competition),
                createTeam("Newcastle United", "NEW", "https://example.com/logos/NEW.png", "St. James' Park", competition),
                createTeam("Aston Villa", "AVL", "https://example.com/logos/AVL.png", "Villa Park", competition),
                createTeam("West Ham United", "WHU", "https://example.com/logos/WHU.png", "London Stadium", competition),
                createTeam("Brighton & Hove Albion", "BHA", "https://example.com/logos/BHA.png", "Amex Stadium", competition),
                createTeam("Brentford", "BRE", "https://example.com/logos/BRE.png", "Gtech Community Stadium", competition),
                createTeam("Fulham", "FUL", "https://example.com/logos/FUL.png", "Craven Cottage", competition),
                createTeam("Crystal Palace", "CRY", "https://example.com/logos/CRY.png", "Selhurst Park", competition),
                createTeam("Wolverhampton Wanderers", "WOL", "https://example.com/logos/WOL.png", "Molineux Stadium", competition),
                createTeam("Everton", "EVE", "https://example.com/logos/EVE.png", "Goodison Park", competition),
                createTeam("Nottingham Forest", "NFO", "https://example.com/logos/NFO.png", "City Ground", competition),
                createTeam("Bournemouth", "BOU", "https://example.com/logos/BOU.png", "Vitality Stadium", competition),
                createTeam("Leicester City", "LEI", "https://example.com/logos/LEI.png", "King Power Stadium", competition),
                createTeam("Ipswich Town", "IPS", "https://example.com/logos/IPS.png", "Portman Road", competition),
                createTeam("Southampton", "SOU", "https://example.com/logos/SOU.png", "St. Mary's Stadium", competition)
        );
        teams = teamRepository.saveAll(teams);

        Team arsenal = teams.get(0);
        Team chelsea = teams.get(1);
        squadsByTeamId.put(arsenal.getId(), createArsenalPlayersAndManager(arsenal));
        squadsByTeamId.put(chelsea.getId(), createChelseaPlayersAndManager(chelsea));

        for (int i = 2; i < teams.size(); i++) {
            Team team = teams.get(i);
            squadsByTeamId.put(team.getId(), createGeneratedSquadAndManager(team));
        }

        List<MatchFixture> fixtures = new ArrayList<>();
        fixtures.add(new MatchFixture(arsenal, chelsea));
        fixtures.addAll(buildRemainingFixtures(teams, 29));

        // Start dates from last month
        Instant matchDateCursor = getDateRangeStart();
        for (MatchFixture fixture : fixtures) {
            MatchStatus status = matchDateCursor.isBefore(now) ? MatchStatus.FINISHED : MatchStatus.SCHEDULED;
            
            if (status == MatchStatus.FINISHED) {
                seedCompletedMatch(competition, fixture.home, fixture.away, matchDateCursor, squadsByTeamId);
            } else {
                seedScheduledMatch(competition, fixture.home, fixture.away, matchDateCursor);
            }
            matchDateCursor = matchDateCursor.plus(3, ChronoUnit.DAYS);
        }

        // Upcoming fixture
        seedScheduledMatch(competition, arsenal, chelsea, getFutureDate());
    }

    // -------------------------------------------------------------------------
    // La Liga (Updated Dates)
    // -------------------------------------------------------------------------

    private void seedLaLiga(Map<Long, List<Profile>> squadsByTeamId) {
        Competition competition = new Competition();
        competition.setName("La Liga 2025/26");
        competition.setCompetitionCode("LFP");
        competition.setLogoUrl("https://example.com/logos/laliga.png");
        competition.setScope(CompetitionScope.NATIONAL);
        competition.setStatus(CompetitionStatus.SCHEDULED);
        competition.setLegFormat(CompetitionLegFormat.DOUBLE);
        competition.setTotalTeams(20);
        competition.setCompetitionType(CompetitionType.LEAGUE);
        competition.setTotalRounds(38);
        competition.setStartDate(LocalDateTime.parse("2025-08-16T19:00:00"));
        competition.setEndDate(LocalDateTime.parse("2026-05-24T19:00:00"));
        competition.setCreatedAt(Instant.now());
        competition.setUpdatedAt(Instant.now());
        competition = competitionRepository.save(competition);

        List<Team> teams = Arrays.asList(
                createTeam("Real Madrid", "RMA", "https://example.com/logos/RMA.png", "Santiago Bernabeu", competition),
                createTeam("FC Barcelona", "BAR", "https://example.com/logos/BAR.png", "Spotify Camp Nou", competition),
                createTeam("Atletico Madrid", "ATM", "https://example.com/logos/ATM.png", "Civitas Metropolitano", competition),
                createTeam("Athletic Bilbao", "ATH", "https://example.com/logos/ATH.png", "San Mames", competition),
                createTeam("Real Sociedad", "RSO", "https://example.com/logos/RSO.png", "Reale Arena", competition),
                createTeam("Real Betis", "BET", "https://example.com/logos/BET.png", "Estadio Benito Villamarin", competition),
                createTeam("Villarreal", "VIL", "https://example.com/logos/VIL.png", "Estadio de la Ceramica", competition),
                createTeam("Valencia", "VAL", "https://example.com/logos/VAL.png", "Mestalla", competition),
                createTeam("Sevilla", "SEV", "https://example.com/logos/SEV.png", "Ramon Sanchez Pizjuan", competition),
                createTeam("Girona", "GIR", "https://example.com/logos/GIR.png", "Estadi Montilivi", competition),
                createTeam("Celta Vigo", "CEL", "https://example.com/logos/CEL.png", "Balaidos", competition),
                createTeam("Osasuna", "OSA", "https://example.com/logos/OSA.png", "El Sadar", competition),
                createTeam("Rayo Vallecano", "RAY", "https://example.com/logos/RAY.png", "Campo de Futbol de Vallecas", competition),
                createTeam("Mallorca", "MLL", "https://example.com/logos/MLL.png", "Son Moix", competition),
                createTeam("Getafe", "GET", "https://example.com/logos/GET.png", "Coliseum Alfonso Perez", competition),
                createTeam("Alaves", "ALA", "https://example.com/logos/ALA.png", "Mendizorrotza", competition),
                createTeam("Espanyol", "ESP", "https://example.com/logos/ESP.png", "RCDE Stadium", competition),
                createTeam("Leganes", "LEG", "https://example.com/logos/LEG.png", "Butarque", competition),
                createTeam("Las Palmas", "LPA", "https://example.com/logos/LPA.png", "Estadio de Gran Canaria", competition),
                createTeam("Valladolid", "VLL", "https://example.com/logos/VLL.png", "Jose Zorrilla", competition)
        );
        teams = teamRepository.saveAll(teams);

        Team realMadrid = teams.get(0);
        Team barcelona = teams.get(1);
        squadsByTeamId.put(realMadrid.getId(), createRealMadridPlayersAndManager(realMadrid));
        squadsByTeamId.put(barcelona.getId(), createBarcelonaPlayersAndManager(barcelona));

        for (int i = 2; i < teams.size(); i++) {
            Team team = teams.get(i);
            squadsByTeamId.put(team.getId(), createGeneratedSquadAndManager(team));
        }

        List<MatchFixture> fixtures = new ArrayList<>();
        fixtures.add(new MatchFixture(realMadrid, barcelona));
        fixtures.addAll(buildRemainingFixtures(teams, 29));

        Instant matchDateCursor = getDateRangeStart();
        for (MatchFixture fixture : fixtures) {
            MatchStatus status = matchDateCursor.isBefore(now) ? MatchStatus.FINISHED : MatchStatus.SCHEDULED;
            
            if (status == MatchStatus.FINISHED) {
                seedCompletedMatch(competition, fixture.home, fixture.away, matchDateCursor, squadsByTeamId);
            } else {
                seedScheduledMatch(competition, fixture.home, fixture.away, matchDateCursor);
            }
            matchDateCursor = matchDateCursor.plus(3, ChronoUnit.DAYS);
        }

        seedScheduledMatch(competition, realMadrid, barcelona, getFutureDate());
    }

    // -------------------------------------------------------------------------
    // Ligue 1 (Already exists - updating dates)
    // -------------------------------------------------------------------------

    private void seedLigue1(Map<Long, List<Profile>> squadsByTeamId) {
        Competition competition = new Competition();
        competition.setName("Ligue 1 2025/26");
        competition.setCompetitionCode("L1");
        competition.setLogoUrl("https://example.com/logos/ligue1.png");
        competition.setScope(CompetitionScope.NATIONAL);
        competition.setStatus(CompetitionStatus.SCHEDULED);
        competition.setLegFormat(CompetitionLegFormat.DOUBLE);
        competition.setTotalTeams(20);
        competition.setCompetitionType(CompetitionType.LEAGUE);
        competition.setTotalRounds(38);
        competition.setStartDate(LocalDateTime.parse("2025-08-15T19:00:00"));
        competition.setEndDate(LocalDateTime.parse("2026-05-17T19:00:00"));
        competition.setCreatedAt(Instant.now());
        competition.setUpdatedAt(Instant.now());
        competition = competitionRepository.save(competition);

        List<Team> teams = Arrays.asList(
                createTeam("Paris Saint-Germain", "PSG", "https://example.com/logos/PSG.png", "Parc des Princes", competition),
                createTeam("Olympique de Marseille", "OM", "https://example.com/logos/OM.png", "Orange Velodrome", competition),
                createTeam("AS Monaco", "ASM", "https://example.com/logos/ASM.png", "Stade Louis II", competition),
                createTeam("Olympique Lyonnais", "OL", "https://example.com/logos/OL.png", "Groupama Stadium", competition),
                createTeam("LOSC Lille", "LIL", "https://example.com/logos/LIL.png", "Stade Pierre-Mauroy", competition),
                createTeam("OGC Nice", "NICE", "https://example.com/logos/NICE.png", "Allianz Riviera", competition),
                createTeam("RC Lens", "RCL", "https://example.com/logos/RCL.png", "Stade Bollaert-Delelis", competition),
                createTeam("Stade Rennais", "REN", "https://example.com/logos/REN.png", "Roazhon Park", competition),
                createTeam("RC Strasbourg", "STR", "https://example.com/logos/STR.png", "Stade de la Meinau", competition),
                createTeam("Stade Brestois", "BRE29", "https://example.com/logos/BRE29.png", "Stade Francis-Le Ble", competition),
                createTeam("Toulouse FC", "TFC", "https://example.com/logos/TFC.png", "Stadium de Toulouse", competition),
                createTeam("Montpellier HSC", "MHSC", "https://example.com/logos/MHSC.png", "Stade de la Mosson", competition),
                createTeam("FC Nantes", "FCN", "https://example.com/logos/FCN.png", "Stade de la Beaujoire", competition),
                createTeam("Angers SCO", "SCO", "https://example.com/logos/SCO.png", "Stade Raymond-Kopa", competition),
                createTeam("Le Havre AC", "HAC", "https://example.com/logos/HAC.png", "Stade Oceane", competition),
                createTeam("AJ Auxerre", "AJA", "https://example.com/logos/AJA.png", "Stade Abbe-Deschamps", competition),
                createTeam("Stade de Reims", "SDR", "https://example.com/logos/SDR.png", "Stade Auguste-Delaune", competition),
                createTeam("Clermont Foot", "CF63", "https://example.com/logos/CF63.png", "Stade Gabriel-Montpied", competition),
                createTeam("FC Metz", "FCM", "https://example.com/logos/FCM.png", "Stade Saint-Symphorien", competition),
                createTeam("Paris FC", "PFC", "https://example.com/logos/PFC.png", "Stade Charlety", competition)
        );
        teams = teamRepository.saveAll(teams);

        Team psg = teams.get(0);
        Team marseille = teams.get(1);
        squadsByTeamId.put(psg.getId(), createPsgPlayersAndManager(psg));
        squadsByTeamId.put(marseille.getId(), createMarseillePlayersAndManager(marseille));

        for (int i = 2; i < teams.size(); i++) {
            Team team = teams.get(i);
            squadsByTeamId.put(team.getId(), createGeneratedSquadAndManager(team));
        }

        List<MatchFixture> fixtures = new ArrayList<>();
        fixtures.add(new MatchFixture(psg, marseille));
        fixtures.addAll(buildRemainingFixtures(teams, 29));

        Instant matchDateCursor = getDateRangeStart();
        for (MatchFixture fixture : fixtures) {
            MatchStatus status = matchDateCursor.isBefore(now) ? MatchStatus.FINISHED : MatchStatus.SCHEDULED;
            
            if (status == MatchStatus.FINISHED) {
                seedCompletedMatch(competition, fixture.home, fixture.away, matchDateCursor, squadsByTeamId);
            } else {
                seedScheduledMatch(competition, fixture.home, fixture.away, matchDateCursor);
            }
            matchDateCursor = matchDateCursor.plus(3, ChronoUnit.DAYS);
        }

        seedScheduledMatch(competition, psg, marseille, getFutureDate());
    }

    // -------------------------------------------------------------------------
    // Serie A (Already exists - updating dates)
    // -------------------------------------------------------------------------

    private void seedSerieA(Map<Long, List<Profile>> squadsByTeamId) {
        Competition competition = new Competition();
        competition.setName("Serie A 2025/26");
        competition.setCompetitionCode("LNPA");
        competition.setLogoUrl("https://example.com/logos/seriea.png");
        competition.setScope(CompetitionScope.NATIONAL);
        competition.setStatus(CompetitionStatus.SCHEDULED);
        competition.setLegFormat(CompetitionLegFormat.DOUBLE);
        competition.setTotalTeams(20);
        competition.setCompetitionType(CompetitionType.LEAGUE);
        competition.setTotalRounds(38);
        competition.setStartDate(LocalDateTime.parse("2025-08-23T18:00:00"));
        competition.setEndDate(LocalDateTime.parse("2026-05-24T18:00:00"));
        competition.setCreatedAt(Instant.now());
        competition.setUpdatedAt(Instant.now());
        competition = competitionRepository.save(competition);

        List<Team> teams = Arrays.asList(
                createTeam("Juventus", "JUV", "https://example.com/logos/JUV.png", "Allianz Stadium", competition),
                createTeam("Inter Milan", "INT", "https://example.com/logos/INT.png", "San Siro", competition),
                createTeam("AC Milan", "MIL", "https://example.com/logos/MIL.png", "San Siro", competition),
                createTeam("SSC Napoli", "NAP", "https://example.com/logos/NAP.png", "Stadio Diego Armando Maradona", competition),
                createTeam("AS Roma", "ROM", "https://example.com/logos/ROM.png", "Stadio Olimpico", competition),
                createTeam("SS Lazio", "LAZ", "https://example.com/logos/LAZ.png", "Stadio Olimpico", competition),
                createTeam("Atalanta", "ATA", "https://example.com/logos/ATA.png", "Gewiss Stadium", competition),
                createTeam("Fiorentina", "FIO", "https://example.com/logos/FIO.png", "Stadio Artemio Franchi", competition),
                createTeam("Bologna FC", "BOL", "https://example.com/logos/BOL.png", "Stadio Renato Dall'Ara", competition),
                createTeam("Torino FC", "TOR", "https://example.com/logos/TOR.png", "Stadio Olimpico Grande Torino", competition),
                createTeam("Udinese", "UDI", "https://example.com/logos/UDI.png", "Bluenergy Stadium", competition),
                createTeam("Genoa CFC", "GEN", "https://example.com/logos/GEN.png", "Stadio Luigi Ferraris", competition),
                createTeam("Hellas Verona", "VER", "https://example.com/logos/VER.png", "Stadio Marcantonio Bentegodi", competition),
                createTeam("Cagliari Calcio", "CAG", "https://example.com/logos/CAG.png", "Unipol Domus", competition),
                createTeam("Empoli FC", "EMP", "https://example.com/logos/EMP.png", "Stadio Carlo Castellani", competition),
                createTeam("Parma Calcio", "PAR", "https://example.com/logos/PAR.png", "Stadio Ennio Tardini", competition),
                createTeam("Como 1907", "COM", "https://example.com/logos/COM.png", "Stadio Giuseppe Sinigaglia", competition),
                createTeam("Venezia FC", "VEN", "https://example.com/logos/VEN.png", "Stadio Pier Luigi Penzo", competition),
                createTeam("Lecce", "LEC", "https://example.com/logos/LEC.png", "Stadio Via del Mare", competition),
                createTeam("Monza", "MON", "https://example.com/logos/MON.png", "U-Power Stadium", competition)
        );
        teams = teamRepository.saveAll(teams);

        Team juventus = teams.get(0);
        Team interMilan = teams.get(1);
        squadsByTeamId.put(juventus.getId(), createJuventusPlayersAndManager(juventus));
        squadsByTeamId.put(interMilan.getId(), createInterMilanPlayersAndManager(interMilan));

        for (int i = 2; i < teams.size(); i++) {
            Team team = teams.get(i);
            squadsByTeamId.put(team.getId(), createGeneratedSquadAndManager(team));
        }

        List<MatchFixture> fixtures = new ArrayList<>();
        fixtures.add(new MatchFixture(juventus, interMilan));
        fixtures.addAll(buildRemainingFixtures(teams, 29));

        Instant matchDateCursor = getDateRangeStart();
        for (MatchFixture fixture : fixtures) {
            MatchStatus status = matchDateCursor.isBefore(now) ? MatchStatus.FINISHED : MatchStatus.SCHEDULED;
            
            if (status == MatchStatus.FINISHED) {
                seedCompletedMatch(competition, fixture.home, fixture.away, matchDateCursor, squadsByTeamId);
            } else {
                seedScheduledMatch(competition, fixture.home, fixture.away, matchDateCursor);
            }
            matchDateCursor = matchDateCursor.plus(3, ChronoUnit.DAYS);
        }

        seedScheduledMatch(competition, juventus, interMilan, getFutureDate());
    }

    // -------------------------------------------------------------------------
    // Bundesliga (Already exists - updating dates)
    // -------------------------------------------------------------------------

    private void seedBundesliga(Map<Long, List<Profile>> squadsByTeamId) {
        Competition competition = new Competition();
        competition.setName("Bundesliga 2025/26");
        competition.setCompetitionCode("BL");
        competition.setLogoUrl("https://example.com/logos/bundesliga.png");
        competition.setScope(CompetitionScope.NATIONAL);
        competition.setStatus(CompetitionStatus.SCHEDULED);
        competition.setLegFormat(CompetitionLegFormat.DOUBLE);
        competition.setTotalTeams(20);
        competition.setCompetitionType(CompetitionType.LEAGUE);
        competition.setTotalRounds(38);
        competition.setStartDate(LocalDateTime.parse("2025-08-22T18:30:00"));
        competition.setEndDate(LocalDateTime.parse("2026-05-16T17:30:00"));
        competition.setCreatedAt(Instant.now());
        competition.setUpdatedAt(Instant.now());
        competition = competitionRepository.save(competition);

        List<Team> teams = Arrays.asList(
                createTeam("Bayern Munich", "FCB", "https://example.com/logos/FCB.png", "Allianz Arena", competition),
                createTeam("Borussia Dortmund", "BVB", "https://example.com/logos/BVB.png", "Signal Iduna Park", competition),
                createTeam("RB Leipzig", "RBL", "https://example.com/logos/RBL.png", "Red Bull Arena", competition),
                createTeam("Bayer Leverkusen", "B04", "https://example.com/logos/B04.png", "BayArena", competition),
                createTeam("VfB Stuttgart", "VFB", "https://example.com/logos/VFB.png", "MHPArena", competition),
                createTeam("Eintracht Frankfurt", "SGE", "https://example.com/logos/SGE.png", "Deutsche Bank Park", competition),
                createTeam("SC Freiburg", "SCF", "https://example.com/logos/SCF.png", "Europa-Park Stadion", competition),
                createTeam("VfL Wolfsburg", "WOB", "https://example.com/logos/WOB.png", "Volkswagen Arena", competition),
                createTeam("Borussia Monchengladbach", "BMG", "https://example.com/logos/BMG.png", "Borussia-Park", competition),
                createTeam("Werder Bremen", "SVW", "https://example.com/logos/SVW.png", "Weserstadion", competition),
                createTeam("1. FC Union Berlin", "FCU", "https://example.com/logos/FCU.png", "Stadion An der Alten Forsterei", competition),
                createTeam("1. FSV Mainz 05", "M05", "https://example.com/logos/M05.png", "Mewa Arena", competition),
                createTeam("TSG Hoffenheim", "TSG", "https://example.com/logos/TSG.png", "PreZero Arena", competition),
                createTeam("FC Augsburg", "FCA", "https://example.com/logos/FCA.png", "WWK Arena", competition),
                createTeam("1. FC Heidenheim", "FCH", "https://example.com/logos/FCH.png", "Voith-Arena", competition),
                createTeam("FC St. Pauli", "STP", "https://example.com/logos/STP.png", "Millerntor-Stadion", competition),
                createTeam("Holstein Kiel", "KSV", "https://example.com/logos/KSV.png", "Holstein-Stadion", competition),
                createTeam("VfL Bochum", "BOC", "https://example.com/logos/BOC.png", "Vonovia Ruhrstadion", competition),
                createTeam("Hamburger SV", "HSV", "https://example.com/logos/HSV.png", "Volksparkstadion", competition),
                createTeam("Fortuna Dusseldorf", "F95", "https://example.com/logos/F95.png", "Merkur Spiel-Arena", competition)
        );
        teams = teamRepository.saveAll(teams);

        Team bayern = teams.get(0);
        Team dortmund = teams.get(1);
        squadsByTeamId.put(bayern.getId(), createBayernMunichPlayersAndManager(bayern));
        squadsByTeamId.put(dortmund.getId(), createDortmundPlayersAndManager(dortmund));

        for (int i = 2; i < teams.size(); i++) {
            Team team = teams.get(i);
            squadsByTeamId.put(team.getId(), createGeneratedSquadAndManager(team));
        }

        List<MatchFixture> fixtures = new ArrayList<>();
        fixtures.add(new MatchFixture(bayern, dortmund));
        fixtures.addAll(buildRemainingFixtures(teams, 29));

        Instant matchDateCursor = getDateRangeStart();
        for (MatchFixture fixture : fixtures) {
            MatchStatus status = matchDateCursor.isBefore(now) ? MatchStatus.FINISHED : MatchStatus.SCHEDULED;
            
            if (status == MatchStatus.FINISHED) {
                seedCompletedMatch(competition, fixture.home, fixture.away, matchDateCursor, squadsByTeamId);
            } else {
                seedScheduledMatch(competition, fixture.home, fixture.away, matchDateCursor);
            }
            matchDateCursor = matchDateCursor.plus(3, ChronoUnit.DAYS);
        }

        seedScheduledMatch(competition, bayern, dortmund, getFutureDate());
    }

    // -------------------------------------------------------------------------
    // Date Helper Methods
    // -------------------------------------------------------------------------

    /**
     * Returns a date that is 30 days in the past from today.
     * This ensures all matches are between last month and this month.
     */
    private Instant getDateRangeStart() {
        return Instant.now().minus(30, ChronoUnit.DAYS);
    }

    /**
     * Returns a date that is in the future (30 days from now).
     */
    private Instant getFutureDate() {
        return Instant.now().plus(30, ChronoUnit.DAYS);
    }

    /**
     * For any new matches that need to be seeded, use this to ensure dates
     * are within the last month to this month range.
     */
    private Instant getNextMatchDate(Instant currentDate) {
        // If current date is before the range start, use range start
        Instant rangeStart = getDateRangeStart();
        if (currentDate.isBefore(rangeStart)) {
            return rangeStart;
        }
        // If current date is after today + 30 days, cap it
        Instant rangeEnd = getFutureDate();
        if (currentDate.isAfter(rangeEnd)) {
            return rangeEnd;
        }
        return currentDate;
    }

    // -------------------------------------------------------------------------
    // Teams (Unchanged from original)
    // -------------------------------------------------------------------------

    private Team createTeam(String name, String code, String logoUrl, String stadium, Competition competition) {
        Team team = new Team();
        team.setName(name);
        team.setTeamCode(code);
        team.setLogoUrl(logoUrl);
        team.setStadium(stadium);
        team.setCompetitions(new HashSet<>(Arrays.asList(competition)));
        team.setCreatedAt(Instant.now());
        team.setUpdatedAt(Instant.now());
        return team;
    }

    // -------------------------------------------------------------------------
    // Arsenal / Chelsea (Unchanged)
    // -------------------------------------------------------------------------

    private List<Profile> createArsenalPlayersAndManager(Team team) {
        Profile manager = createManager("Mikel", "Arteta", "+2348012345620", "https://example.com/avatars/mikel-arteta.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("David", "Raya", "+2348012345601", "https://example.com/avatars/david-raya.png", 1, team, Position.GK),
                createPlayer("Ben", "White", "+2348012345602", "https://example.com/avatars/ben-white.png", 4, team, Position.RB),
                createPlayer("Jurrien", "Timber", "+2348012345603", "https://example.com/avatars/jurrien-timber.png", 12, team, Position.LB),
                createPlayer("William", "Saliba", "+2348012345604", "https://example.com/avatars/william-saliba.png", 2, team, Position.CB),
                createPlayer("Gabriel", "Magalhaes", "+2348012345605", "https://example.com/avatars/gabriel-magalhaes.png", 6, team, Position.CB),
                createPlayer("Jakub", "Kiwior", "+2348012345606", "https://example.com/avatars/jakub-kiwior.png", 15, team, Position.CB),
                createPlayer("Oleksandr", "Zinchenko", "+2348012345607", "https://example.com/avatars/oleksandr-zinchenko.png", 35, team, Position.CDM),
                createPlayer("Declan", "Rice", "+2348012345608", "https://example.com/avatars/declan-rice.png", 41, team, Position.CDM),
                createPlayer("Thomas", "Partey", "+2348012345609", "https://example.com/avatars/thomas-partey.png", 5, team, Position.CDM),
                createPlayer("Martin", "Odegaard", "+2348012345610", "https://example.com/avatars/martin-odegaard.png", 8, team, Position.CAM),
                createPlayer("Mikel", "Merino", "+2348012345611", "https://example.com/avatars/mikel-merino.png", 23, team, Position.CM),
                createPlayer("Jorginho", "Frello", "+2348012345612", "https://example.com/avatars/jorginho-frello.png", 20, team, Position.CM),
                createPlayer("Bukayo", "Saka", "+2348012345613", "https://example.com/avatars/bukayo-saka.png", 7, team, Position.RW),
                createPlayer("Gabriel", "Martinelli", "+2348012345614", "https://example.com/avatars/gabriel-martinelli.png", 11, team, Position.LW),
                createPlayer("Leandro", "Trossard", "+2348012345615", "https://example.com/avatars/leandro-trossard.png", 19, team, Position.LW),
                createPlayer("Kai", "Havertz", "+2348012345616", "https://example.com/avatars/kai-havertz.png", 29, team, Position.ST),
                createPlayer("Viktor", "Gyokeres", "+2348012345617", "https://example.com/avatars/viktor-gyokeres.png", 14, team, Position.ST),
                createPlayer("Gabriel", "Jesus", "+2348012345618", "https://example.com/avatars/gabriel-jesus.png", 9, team, Position.CF),
                createPlayer("Ethan", "Nwaneri", "+2348012345619", "https://example.com/avatars/ethan-nwaneri.png", 33, team, Position.CAM)
        );
        return profileRepository.saveAll(players);
    }

    private List<Profile> createChelseaPlayersAndManager(Team team) {
        Profile manager = createManager("Enzo", "Maresca", "+2348012345641", "https://example.com/avatars/enzo-maresca.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Robert", "Sánchez", "+2348012345621", "https://example.com/avatars/robert-sanchez.png", 1, team, Position.GK),
                createPlayer("Reece", "James", "+2348012345623", "https://example.com/avatars/reece-james.png", 24, team, Position.RB),
                createPlayer("Marc", "Cucurella", "+2348012345625", "https://example.com/avatars/marc-cucurella.png", 3, team, Position.LB),
                createPlayer("Levi", "Colwill", "+2348012345626", "https://example.com/avatars/levi-colwill.png", 26, team, Position.CB),
                createPlayer("Wesley", "Fofana", "+2348012345627", "https://example.com/avatars/wesley-fofana.png", 33, team, Position.CB),
                createPlayer("Moisés", "Caicedo", "+2348012345632", "https://example.com/avatars/moises-caicedo.png", 25, team, Position.CDM),
                createPlayer("Enzo", "Fernández", "+2348012345631", "https://example.com/avatars/enzo-fernandez.png", 8, team, Position.CM),
                createPlayer("Kiernan", "Dewsbury-Hall", "+2348012345634", "https://example.com/avatars/kiernan-dewsbury-hall.png", 22, team, Position.CM),
                createPlayer("Cole", "Palmer", "+2348012345636", "https://example.com/avatars/cole-palmer.png", 20, team, Position.RW),
                createPlayer("Nicolas", "Jackson", "+2348012345640", "https://example.com/avatars/nicolas-jackson.png", 15, team, Position.ST),
                createPlayer("Mykhailo", "Mudryk", "+2348012345637", "https://example.com/avatars/mykhailo-mudryk.png", 10, team, Position.LW),
                createPlayer("Filip", "Jörgensen", "+2348012345622", "https://example.com/avatars/filip-jorgensen.png", 13, team, Position.GK),
                createPlayer("Tosin", "Adarabioyo", "+2348012345628", "https://example.com/avatars/tosin-adarabioyo.png", 4, team, Position.CB),
                createPlayer("Malo", "Gusto", "+2348012345624", "https://example.com/avatars/malo-gusto.png", 27, team, Position.RB),
                createPlayer("Romeo", "Lavia", "+2348012345633", "https://example.com/avatars/romeo-lavia.png", 45, team, Position.CDM),
                createPlayer("Carney", "Chukwuemeka", "+2348012345635", "https://example.com/avatars/carney-chukwuemeka.png", 17, team, Position.CAM),
                createPlayer("Christopher", "Nkunku", "+2348012345639", "https://example.com/avatars/christopher-nkunku.png", 18, team, Position.CF),
                createPlayer("Noni", "Madueke", "+2348012345638", "https://example.com/avatars/noni-madueke.png", 11, team, Position.RW),
                createPlayer("Axel", "Disasi", "+2348012345629", "https://example.com/avatars/axel-disasi.png", 2, team, Position.CB),
                createPlayer("Trevoh", "Chalobah", "+2348012345630", "https://example.com/avatars/trevoh-chalobah.png", 14, team, Position.CB)
        );
        return profileRepository.saveAll(players);
    }

    // -------------------------------------------------------------------------
    // Real Madrid / Barcelona (Unchanged)
    // -------------------------------------------------------------------------

    private List<Profile> createRealMadridPlayersAndManager(Team team) {
        Profile manager = createManager("Carlo", "Ancelotti", "+2348012345660", "https://example.com/avatars/carlo-ancelotti.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Thibaut", "Courtois", "+2348012345642", "https://example.com/avatars/thibaut-courtois.png", 1, team, Position.GK),
                createPlayer("Dani", "Carvajal", "+2348012345643", "https://example.com/avatars/dani-carvajal.png", 2, team, Position.RB),
                createPlayer("Ferland", "Mendy", "+2348012345644", "https://example.com/avatars/ferland-mendy.png", 23, team, Position.LB),
                createPlayer("Antonio", "Rudiger", "+2348012345645", "https://example.com/avatars/antonio-rudiger.png", 22, team, Position.CB),
                createPlayer("Eder", "Militao", "+2348012345646", "https://example.com/avatars/eder-militao.png", 3, team, Position.CB),
                createPlayer("David", "Alaba", "+2348012345647", "https://example.com/avatars/david-alaba.png", 4, team, Position.CB),
                createPlayer("Aurelien", "Tchouameni", "+2348012345648", "https://example.com/avatars/aurelien-tchouameni.png", 18, team, Position.CDM),
                createPlayer("Eduardo", "Camavinga", "+2348012345649", "https://example.com/avatars/eduardo-camavinga.png", 12, team, Position.CM),
                createPlayer("Federico", "Valverde", "+2348012345650", "https://example.com/avatars/federico-valverde.png", 15, team, Position.CM),
                createPlayer("Jude", "Bellingham", "+2348012345651", "https://example.com/avatars/jude-bellingham.png", 5, team, Position.CAM),
                createPlayer("Vinicius", "Junior", "+2348012345652", "https://example.com/avatars/vinicius-junior.png", 7, team, Position.LW),
                createPlayer("Rodrygo", "Goes", "+2348012345653", "https://example.com/avatars/rodrygo-goes.png", 11, team, Position.RW),
                createPlayer("Kylian", "Mbappe", "+2348012345654", "https://example.com/avatars/kylian-mbappe.png", 9, team, Position.ST),
                createPlayer("Endrick", "Felipe", "+2348012345655", "https://example.com/avatars/endrick-felipe.png", 16, team, Position.ST),
                createPlayer("Brahim", "Diaz", "+2348012345656", "https://example.com/avatars/brahim-diaz.png", 21, team, Position.CAM),
                createPlayer("Andriy", "Lunin", "+2348012345657", "https://example.com/avatars/andriy-lunin.png", 13, team, Position.GK),
                createPlayer("Lucas", "Vazquez", "+2348012345658", "https://example.com/avatars/lucas-vazquez.png", 17, team, Position.RB),
                createPlayer("Fran", "Garcia", "+2348012345659", "https://example.com/avatars/fran-garcia.png", 20, team, Position.LB),
                createPlayer("Dani", "Ceballos", "+2348012345661", "https://example.com/avatars/dani-ceballos.png", 19, team, Position.CM)
        );
        return profileRepository.saveAll(players);
    }

    private List<Profile> createBarcelonaPlayersAndManager(Team team) {
        Profile manager = createManager("Hansi", "Flick", "+2348012345683", "https://example.com/avatars/hansi-flick.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Marc-Andre", "ter Stegen", "+2348012345662", "https://example.com/avatars/marc-andre-ter-stegen.png", 1, team, Position.GK),
                createPlayer("Jules", "Kounde", "+2348012345663", "https://example.com/avatars/jules-kounde.png", 23, team, Position.RB),
                createPlayer("Alejandro", "Balde", "+2348012345664", "https://example.com/avatars/alejandro-balde.png", 3, team, Position.LB),
                createPlayer("Ronald", "Araujo", "+2348012345665", "https://example.com/avatars/ronald-araujo.png", 4, team, Position.CB),
                createPlayer("Pau", "Cubarsi", "+2348012345666", "https://example.com/avatars/pau-cubarsi.png", 5, team, Position.CB),
                createPlayer("Inigo", "Martinez", "+2348012345667", "https://example.com/avatars/inigo-martinez.png", 25, team, Position.CB),
                createPlayer("Frenkie", "de Jong", "+2348012345668", "https://example.com/avatars/frenkie-de-jong.png", 21, team, Position.CDM),
                createPlayer("Pedri", "Gonzalez", "+2348012345669", "https://example.com/avatars/pedri-gonzalez.png", 8, team, Position.CM),
                createPlayer("Gavi", "Paez", "+2348012345670", "https://example.com/avatars/gavi-paez.png", 6, team, Position.CM),
                createPlayer("Raphinha", "Belloli", "+2348012345671", "https://example.com/avatars/raphinha-belloli.png", 11, team, Position.RW),
                createPlayer("Lamine", "Yamal", "+2348012345672", "https://example.com/avatars/lamine-yamal.png", 19, team, Position.RW),
                createPlayer("Robert", "Lewandowski", "+2348012345673", "https://example.com/avatars/robert-lewandowski.png", 9, team, Position.ST),
                createPlayer("Ferran", "Torres", "+2348012345674", "https://example.com/avatars/ferran-torres.png", 7, team, Position.ST),
                createPlayer("Dani", "Olmo", "+2348012345675", "https://example.com/avatars/dani-olmo.png", 20, team, Position.CAM),
                createPlayer("Marc", "Casado", "+2348012345676", "https://example.com/avatars/marc-casado.png", 17, team, Position.CDM),
                createPlayer("Inaki", "Pena", "+2348012345677", "https://example.com/avatars/inaki-pena.png", 13, team, Position.GK),
                createPlayer("Hector", "Fort", "+2348012345678", "https://example.com/avatars/hector-fort.png", 16, team, Position.RB),
                createPlayer("Andreas", "Christensen", "+2348012345679", "https://example.com/avatars/andreas-christensen.png", 15, team, Position.CB),
                createPlayer("Fermin", "Lopez", "+2348012345680", "https://example.com/avatars/fermin-lopez.png", 22, team, Position.CM)
        );
        return profileRepository.saveAll(players);
    }

    // -------------------------------------------------------------------------
    // PSG / Marseille (Unchanged)
    // -------------------------------------------------------------------------

    private List<Profile> createPsgPlayersAndManager(Team team) {
        Profile manager = createManager("Luis", "Enrique", "+2348012345720", "https://example.com/avatars/luis-enrique.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Gianluigi", "Donnarumma", "+2348012345701", "https://example.com/avatars/gianluigi-donnarumma.png", 1, team, Position.GK),
                createPlayer("Achraf", "Hakimi", "+2348012345702", "https://example.com/avatars/achraf-hakimi.png", 2, team, Position.RB),
                createPlayer("Nuno", "Mendes", "+2348012345703", "https://example.com/avatars/nuno-mendes.png", 25, team, Position.LB),
                createPlayer("Marquinhos", "Correa", "+2348012345704", "https://example.com/avatars/marquinhos-correa.png", 5, team, Position.CB),
                createPlayer("Willian", "Pacho", "+2348012345705", "https://example.com/avatars/willian-pacho.png", 51, team, Position.CB),
                createPlayer("Lucas", "Beraldo", "+2348012345706", "https://example.com/avatars/lucas-beraldo.png", 4, team, Position.CB),
                createPlayer("Vitinha", "Ferreira", "+2348012345707", "https://example.com/avatars/vitinha-ferreira.png", 17, team, Position.CDM),
                createPlayer("Joao", "Neves", "+2348012345708", "https://example.com/avatars/joao-neves.png", 87, team, Position.CM),
                createPlayer("Warren", "Zaire-Emery", "+2348012345709", "https://example.com/avatars/warren-zaire-emery.png", 33, team, Position.CM),
                createPlayer("Ousmane", "Dembele", "+2348012345710", "https://example.com/avatars/ousmane-dembele.png", 10, team, Position.RW),
                createPlayer("Bradley", "Barcola", "+2348012345711", "https://example.com/avatars/bradley-barcola.png", 29, team, Position.LW),
                createPlayer("Khvicha", "Kvaratskhelia", "+2348012345712", "https://example.com/avatars/khvicha-kvaratskhelia.png", 7, team, Position.LW),
                createPlayer("Goncalo", "Ramos", "+2348012345713", "https://example.com/avatars/goncalo-ramos.png", 9, team, Position.ST),
                createPlayer("Randal", "Kolo Muani", "+2348012345714", "https://example.com/avatars/randal-kolo-muani.png", 23, team, Position.ST),
                createPlayer("Fabian", "Ruiz", "+2348012345715", "https://example.com/avatars/fabian-ruiz.png", 8, team, Position.CAM),
                createPlayer("Arnau", "Tenas", "+2348012345716", "https://example.com/avatars/arnau-tenas.png", 36, team, Position.GK),
                createPlayer("Lucas", "Hernandez", "+2348012345717", "https://example.com/avatars/lucas-hernandez.png", 21, team, Position.LB),
                createPlayer("Presnel", "Kimpembe", "+2348012345718", "https://example.com/avatars/presnel-kimpembe.png", 3, team, Position.CB),
                createPlayer("Senny", "Mayulu", "+2348012345719", "https://example.com/avatars/senny-mayulu.png", 20, team, Position.CAM)
        );
        return profileRepository.saveAll(players);
    }

    private List<Profile> createMarseillePlayersAndManager(Team team) {
        Profile manager = createManager("Roberto", "De Zerbi", "+2348012345741", "https://example.com/avatars/roberto-de-zerbi.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Geronimo", "Rulli", "+2348012345721", "https://example.com/avatars/geronimo-rulli.png", 1, team, Position.GK),
                createPlayer("Jonathan", "Clauss", "+2348012345722", "https://example.com/avatars/jonathan-clauss.png", 2, team, Position.RB),
                createPlayer("Ulisses", "Garcia", "+2348012345723", "https://example.com/avatars/ulisses-garcia.png", 22, team, Position.LB),
                createPlayer("Leonardo", "Balerdi", "+2348012345724", "https://example.com/avatars/leonardo-balerdi.png", 4, team, Position.CB),
                createPlayer("Derek", "Cornelius", "+2348012345725", "https://example.com/avatars/derek-cornelius.png", 15, team, Position.CB),
                createPlayer("Facundo", "Medina", "+2348012345726", "https://example.com/avatars/facundo-medina.png", 24, team, Position.CB),
                createPlayer("Geoffrey", "Kondogbia", "+2348012345727", "https://example.com/avatars/geoffrey-kondogbia.png", 6, team, Position.CDM),
                createPlayer("Angel", "Gomes", "+2348012345728", "https://example.com/avatars/angel-gomes.png", 20, team, Position.CM),
                createPlayer("Amine", "Harit", "+2348012345729", "https://example.com/avatars/amine-harit.png", 20, team, Position.CAM),
                createPlayer("Mason", "Greenwood", "+2348012345730", "https://example.com/avatars/mason-greenwood.png", 10, team, Position.RW),
                createPlayer("Luis", "Henrique", "+2348012345731", "https://example.com/avatars/luis-henrique.png", 99, team, Position.LW),
                createPlayer("Neal", "Maupay", "+2348012345732", "https://example.com/avatars/neal-maupay.png", 27, team, Position.ST),
                createPlayer("Ismaila", "Sarr", "+2348012345733", "https://example.com/avatars/ismaila-sarr.png", 7, team, Position.RW),
                createPlayer("Robinio", "Vaz", "+2348012345734", "https://example.com/avatars/robinio-vaz.png", 9, team, Position.ST),
                createPlayer("Bilal", "Nadir", "+2348012345735", "https://example.com/avatars/bilal-nadir.png", 26, team, Position.CM),
                createPlayer("Rulani", "Mombo", "+2348012345736", "https://example.com/avatars/rulani-mombo.png", 30, team, Position.GK),
                createPlayer("Pierre-Emerick", "Highsmith", "+2348012345737", "https://example.com/avatars/pierre-emerick-highsmith.png", 12, team, Position.RB),
                createPlayer("Quentin", "Merlin", "+2348012345738", "https://example.com/avatars/quentin-merlin.png", 17, team, Position.LB),
                createPlayer("Enzo", "Molebe", "+2348012345739", "https://example.com/avatars/enzo-molebe.png", 33, team, Position.CB)
        );
        return profileRepository.saveAll(players);
    }

    // -------------------------------------------------------------------------
    // Juventus / Inter Milan (Unchanged)
    // -------------------------------------------------------------------------

    private List<Profile> createJuventusPlayersAndManager(Team team) {
        Profile manager = createManager("Igor", "Tudor", "+2348012345760", "https://example.com/avatars/igor-tudor.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Michele", "Di Gregorio", "+2348012345742", "https://example.com/avatars/michele-di-gregorio.png", 29, team, Position.GK),
                createPlayer("Andrea", "Cambiaso", "+2348012345743", "https://example.com/avatars/andrea-cambiaso.png", 27, team, Position.RB),
                createPlayer("Pierre", "Kalulu", "+2348012345744", "https://example.com/avatars/pierre-kalulu.png", 15, team, Position.LB),
                createPlayer("Gleison", "Bremer", "+2348012345745", "https://example.com/avatars/gleison-bremer.png", 3, team, Position.CB),
                createPlayer("Federico", "Gatti", "+2348012345746", "https://example.com/avatars/federico-gatti.png", 4, team, Position.CB),
                createPlayer("Lloyd", "Kelly", "+2348012345747", "https://example.com/avatars/lloyd-kelly.png", 32, team, Position.CB),
                createPlayer("Manuel", "Locatelli", "+2348012345748", "https://example.com/avatars/manuel-locatelli.png", 5, team, Position.CDM),
                createPlayer("Khephren", "Thuram", "+2348012345749", "https://example.com/avatars/khephren-thuram.png", 19, team, Position.CM),
                createPlayer("Teun", "Koopmeiners", "+2348012345750", "https://example.com/avatars/teun-koopmeiners.png", 8, team, Position.CAM),
                createPlayer("Kenan", "Yildiz", "+2348012345751", "https://example.com/avatars/kenan-yildiz.png", 10, team, Position.RW),
                createPlayer("Francisco", "Conceicao", "+2348012345752", "https://example.com/avatars/francisco-conceicao.png", 7, team, Position.RW),
                createPlayer("Nico", "Gonzalez", "+2348012345753", "https://example.com/avatars/nico-gonzalez.png", 11, team, Position.LW),
                createPlayer("Dusan", "Vlahovic", "+2348012345754", "https://example.com/avatars/dusan-vlahovic.png", 9, team, Position.ST),
                createPlayer("Jonathan", "David", "+2348012345755", "https://example.com/avatars/jonathan-david.png", 30, team, Position.ST),
                createPlayer("Weston", "McKennie", "+2348012345756", "https://example.com/avatars/weston-mckennie.png", 16, team, Position.CM),
                createPlayer("Mattia", "Perin", "+2348012345757", "https://example.com/avatars/mattia-perin.png", 36, team, Position.GK),
                createPlayer("Juan", "Cabal", "+2348012345758", "https://example.com/avatars/juan-cabal.png", 6, team, Position.CB),
                createPlayer("Vasilije", "Adzic", "+2348012345759", "https://example.com/avatars/vasilije-adzic.png", 40, team, Position.CAM),
                createPlayer("Timothy", "Weah", "+2348012345761", "https://example.com/avatars/timothy-weah.png", 22, team, Position.RB)
        );
        return profileRepository.saveAll(players);
    }

    private List<Profile> createInterMilanPlayersAndManager(Team team) {
        Profile manager = createManager("Cristian", "Chivu", "+2348012345783", "https://example.com/avatars/cristian-chivu.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Yann", "Sommer", "+2348012345762", "https://example.com/avatars/yann-sommer.png", 1, team, Position.GK),
                createPlayer("Denzel", "Dumfries", "+2348012345763", "https://example.com/avatars/denzel-dumfries.png", 2, team, Position.RB),
                createPlayer("Federico", "Dimarco", "+2348012345764", "https://example.com/avatars/federico-dimarco.png", 32, team, Position.LB),
                createPlayer("Alessandro", "Bastoni", "+2348012345765", "https://example.com/avatars/alessandro-bastoni.png", 95, team, Position.CB),
                createPlayer("Francesco", "Acerbi", "+2348012345766", "https://example.com/avatars/francesco-acerbi.png", 15, team, Position.CB),
                createPlayer("Yann", "Bisseck", "+2348012345767", "https://example.com/avatars/yann-bisseck.png", 31, team, Position.CB),
                createPlayer("Hakan", "Calhanoglu", "+2348012345768", "https://example.com/avatars/hakan-calhanoglu.png", 20, team, Position.CDM),
                createPlayer("Nicolo", "Barella", "+2348012345769", "https://example.com/avatars/nicolo-barella.png", 23, team, Position.CM),
                createPlayer("Henrikh", "Mkhitaryan", "+2348012345770", "https://example.com/avatars/henrikh-mkhitaryan.png", 22, team, Position.CM),
                createPlayer("Marcus", "Thuram", "+2348012345771", "https://example.com/avatars/marcus-thuram.png", 9, team, Position.ST),
                createPlayer("Lautaro", "Martinez", "+2348012345772", "https://example.com/avatars/lautaro-martinez.png", 10, team, Position.ST),
                createPlayer("Mehdi", "Taremi", "+2348012345773", "https://example.com/avatars/mehdi-taremi.png", 99, team, Position.ST),
                createPlayer("Piotr", "Zielinski", "+2348012345774", "https://example.com/avatars/piotr-zielinski.png", 4, team, Position.CAM),
                createPlayer("Davide", "Frattesi", "+2348012345775", "https://example.com/avatars/davide-frattesi.png", 16, team, Position.CM),
                createPlayer("Carlos", "Augusto", "+2348012345776", "https://example.com/avatars/carlos-augusto.png", 30, team, Position.LB),
                createPlayer("Josep", "Martinez", "+2348012345777", "https://example.com/avatars/josep-martinez.png", 12, team, Position.GK),
                createPlayer("Matteo", "Darmian", "+2348012345778", "https://example.com/avatars/matteo-darmian.png", 36, team, Position.RB),
                createPlayer("Stefan", "de Vrij", "+2348012345779", "https://example.com/avatars/stefan-de-vrij.png", 6, team, Position.CB),
                createPlayer("Petar", "Sucic", "+2348012345780", "https://example.com/avatars/petar-sucic.png", 19, team, Position.CM)
        );
        return profileRepository.saveAll(players);
    }

    // -------------------------------------------------------------------------
    // Bayern Munich / Borussia Dortmund (Unchanged)
    // -------------------------------------------------------------------------

    private List<Profile> createBayernMunichPlayersAndManager(Team team) {
        Profile manager = createManager("Vincent", "Kompany", "+2348012345800", "https://example.com/avatars/vincent-kompany.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Manuel", "Neuer", "+2348012345781", "https://example.com/avatars/manuel-neuer.png", 1, team, Position.GK),
                createPlayer("Josip", "Stanisic", "+2348012345782", "https://example.com/avatars/josip-stanisic.png", 4, team, Position.RB),
                createPlayer("Raphael", "Guerreiro", "+2348012345784", "https://example.com/avatars/raphael-guerreiro.png", 22, team, Position.LB),
                createPlayer("Dayot", "Upamecano", "+2348012345785", "https://example.com/avatars/dayot-upamecano.png", 2, team, Position.CB),
                createPlayer("Jonathan", "Tah", "+2348012345786", "https://example.com/avatars/jonathan-tah.png", 30, team, Position.CB),
                createPlayer("Min-jae", "Kim", "+2348012345787", "https://example.com/avatars/min-jae-kim.png", 3, team, Position.CB),
                createPlayer("Joshua", "Kimmich", "+2348012345788", "https://example.com/avatars/joshua-kimmich.png", 6, team, Position.CDM),
                createPlayer("Aleksandar", "Pavlovic", "+2348012345789", "https://example.com/avatars/aleksandar-pavlovic.png", 34, team, Position.CM),
                createPlayer("Jamal", "Musiala", "+2348012345790", "https://example.com/avatars/jamal-musiala.png", 42, team, Position.CAM),
                createPlayer("Michael", "Olise", "+2348012345791", "https://example.com/avatars/michael-olise.png", 17, team, Position.RW),
                createPlayer("Leroy", "Sane", "+2348012345792", "https://example.com/avatars/leroy-sane.png", 10, team, Position.LW),
                createPlayer("Kingsley", "Coman", "+2348012345793", "https://example.com/avatars/kingsley-coman.png", 11, team, Position.LW),
                createPlayer("Harry", "Kane", "+2348012345794", "https://example.com/avatars/harry-kane.png", 9, team, Position.ST),
                createPlayer("Thomas", "Muller", "+2348012345795", "https://example.com/avatars/thomas-muller.png", 25, team, Position.CF),
                createPlayer("Leon", "Goretzka", "+2348012345796", "https://example.com/avatars/leon-goretzka.png", 8, team, Position.CM),
                createPlayer("Sven", "Ulreich", "+2348012345797", "https://example.com/avatars/sven-ulreich.png", 26, team, Position.GK),
                createPlayer("Alphonso", "Davies", "+2348012345798", "https://example.com/avatars/alphonso-davies.png", 19, team, Position.LB),
                createPlayer("Eric", "Dier", "+2348012345799", "https://example.com/avatars/eric-dier.png", 5, team, Position.CB),
                createPlayer("Konrad", "Laimer", "+2348012345801", "https://example.com/avatars/konrad-laimer.png", 27, team, Position.CDM)
        );
        return profileRepository.saveAll(players);
    }

    private List<Profile> createDortmundPlayersAndManager(Team team) {
        Profile manager = createManager("Niko", "Kovac", "+2348012345823", "https://example.com/avatars/niko-kovac.png", team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = Arrays.asList(
                createPlayer("Gregor", "Kobel", "+2348012345802", "https://example.com/avatars/gregor-kobel.png", 1, team, Position.GK),
                createPlayer("Julian", "Ryerson", "+2348012345803", "https://example.com/avatars/julian-ryerson.png", 22, team, Position.RB),
                createPlayer("Ramy", "Bensebaini", "+2348012345804", "https://example.com/avatars/ramy-bensebaini.png", 3, team, Position.LB),
                createPlayer("Nico", "Schlotterbeck", "+2348012345805", "https://example.com/avatars/nico-schlotterbeck.png", 4, team, Position.CB),
                createPlayer("Waldemar", "Anton", "+2348012345806", "https://example.com/avatars/waldemar-anton.png", 5, team, Position.CB),
                createPlayer("Niklas", "Sule", "+2348012345807", "https://example.com/avatars/niklas-sule.png", 25, team, Position.CB),
                createPlayer("Emre", "Can", "+2348012345808", "https://example.com/avatars/emre-can.png", 23, team, Position.CDM),
                createPlayer("Felix", "Nmecha", "+2348012345809", "https://example.com/avatars/felix-nmecha.png", 8, team, Position.CM),
                createPlayer("Pascal", "Gross", "+2348012345810", "https://example.com/avatars/pascal-gross.png", 20, team, Position.CM),
                createPlayer("Karim", "Adeyemi", "+2348012345811", "https://example.com/avatars/karim-adeyemi.png", 27, team, Position.RW),
                createPlayer("Jamie", "Gittens", "+2348012345812", "https://example.com/avatars/jamie-gittens.png", 7, team, Position.LW),
                createPlayer("Donyell", "Malen", "+2348012345813", "https://example.com/avatars/donyell-malen.png", 21, team, Position.LW),
                createPlayer("Serhou", "Guirassy", "+2348012345814", "https://example.com/avatars/serhou-guirassy.png", 9, team, Position.ST),
                createPlayer("Maximilian", "Beier", "+2348012345815", "https://example.com/avatars/maximilian-beier.png", 42, team, Position.ST),
                createPlayer("Marcel", "Sabitzer", "+2348012345816", "https://example.com/avatars/marcel-sabitzer.png", 18, team, Position.CAM),
                createPlayer("Alexander", "Meyer", "+2348012345817", "https://example.com/avatars/alexander-meyer.png", 35, team, Position.GK),
                createPlayer("Yan", "Couto", "+2348012345818", "https://example.com/avatars/yan-couto.png", 2, team, Position.RB),
                createPlayer("Marcel", "Lotka", "+2348012345819", "https://example.com/avatars/marcel-lotka.png", 33, team, Position.CB),
                createPlayer("Daniel", "Svensson", "+2348012345820", "https://example.com/avatars/daniel-svensson.png", 26, team, Position.LB)
        );
        return profileRepository.saveAll(players);
    }

    // -------------------------------------------------------------------------
    // Generated squads (Unchanged)
    // -------------------------------------------------------------------------

    private List<Profile> createGeneratedSquadAndManager(Team team) {
        String managerFirst = FIRST_NAMES[rng.nextInt(FIRST_NAMES.length)];
        String managerLast = LAST_NAMES[rng.nextInt(LAST_NAMES.length)];

        Profile manager = createManager(managerFirst, managerLast, nextPhone(), avatarUrl(managerFirst, managerLast), team);
        manager = profileRepository.save(manager);

        team.setManager(manager);
        teamRepository.save(team);

        List<Profile> players = new ArrayList<>();
        for (int i = 0; i < SQUAD_TEMPLATE.length; i++) {
            String first = FIRST_NAMES[rng.nextInt(FIRST_NAMES.length)];
            String last = LAST_NAMES[rng.nextInt(LAST_NAMES.length)];
            players.add(createPlayer(first, last, nextPhone(), avatarUrl(first, last), i + 1, team, SQUAD_TEMPLATE[i]));
        }
        return profileRepository.saveAll(players);
    }

    // -------------------------------------------------------------------------
    // Profile Creation Helpers
    // -------------------------------------------------------------------------

    private Profile createManager(String firstName, String lastName, String phone, String avatar, Team team) {
        Profile manager = new Profile();
        manager.setFirstName(firstName);
        manager.setLastName(lastName);
        manager.setFullName(firstName + " " + lastName);
        manager.setPhoneNumber(phone);
        manager.setAvatarUrl(avatar);
        manager.setSquadNumber(null);
        manager.setTeam(team);
        manager.setRole(Role.MANAGER);
        manager.setPosition(Position.MANAGER);
        manager.setCreatedAt(Instant.now());
        manager.setUpdatedAt(Instant.now());
        return manager;
    }

    private Profile createPlayer(String firstName, String lastName, String phone, String avatar, Integer number, Team team, Position position) {
        Profile profile = new Profile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setFullName(firstName + " " + lastName);
        profile.setPhoneNumber(phone);
        profile.setAvatarUrl(avatar);
        profile.setSquadNumber(number);
        profile.setTeam(team);
        profile.setRole(Role.PLAYER);
        profile.setPosition(position);

        // Fields introduced on the updated Profile entity.
        profile.setHeight(defaultHeight(position));
        profile.setDateOfBirth(generatedDateOfBirth(firstName, lastName));

        // status defaults to ACTIVE and captainStatus defaults to NONE in Profile.
        // preferredFoot is intentionally left nullable for generated players.
        profile.setCreatedAt(Instant.now());
        profile.setUpdatedAt(Instant.now());
        return profile;
    }

    private int defaultHeight(Position position) {
        return switch (position) {
            case GK -> 190;
            case CB -> 186;
            case LB, RB -> 178;
            case CDM, CM, CAM -> 180;
            case LW, RW, CF, ST -> 181;
            default -> 180;
        };
    }

    private LocalDate generatedDateOfBirth(String firstName, String lastName) {
        int hash = Math.floorMod((firstName + ":" + lastName).hashCode(), Integer.MAX_VALUE);
        int year = 1994 + (hash % 9);
        int month = 1 + (hash % 12);
        int day = 1 + (hash % 26);
        return LocalDate.of(year, month, day);
    }

    // -------------------------------------------------------------------------
    // Transfer Seeding
    // -------------------------------------------------------------------------

    private Map<Long, List<Profile>> buildExistingSquads() {
        Map<Long, List<Profile>> squads = new HashMap<>();

        for (Profile profile : profileRepository.findAll()) {
            if (profile.getRole() != Role.PLAYER || profile.getTeam() == null || profile.getTeam().getId() == null) {
                continue;
            }
            squads.computeIfAbsent(profile.getTeam().getId(), key -> new ArrayList<>()).add(profile);
        }

        return squads;
    }

    private void seedTransfers(Map<Long, List<Profile>> squadsByTeamId) {
        if (transferRepository.count() > 0) {
            return;
        }

        Map<String, Team> teams = teamRepository.findAll().stream()
                .collect(Collectors.toMap(Team::getName, team -> team));

        Map<String, Profile> players = squadsByTeamId.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toMap(Profile::getFullName, player -> player, (first, second) -> first));

        List<Transfer> transfers = new ArrayList<>();

        // Vinicius Junior: Flamengo -> Real Madrid
        addTransfer(transfers, players, teams, "Vinicius Junior", "Flamengo", "Real Madrid",
            TransferType.PERMANENT, "45000000.00", "2018-07-12");

        // Jude Bellingham: Borussia Dortmund -> Real Madrid
        addTransfer(transfers, players, teams, "Jude Bellingham", "Borussia Dortmund", "Real Madrid",
            TransferType.PERMANENT, "113000000.00", "2023-06-14");

        // Kylian Mbappe: PSG -> Real Madrid (free transfer)
        addTransfer(transfers, players, teams, "Kylian Mbappe", "Paris Saint-Germain", "Real Madrid",
            TransferType.FREE, null, "2024-07-01");

        // Robert Lewandowski: Bayern Munich -> FC Barcelona
        addTransfer(transfers, players, teams, "Robert Lewandowski", "Bayern Munich", "FC Barcelona",
            TransferType.PERMANENT, "45000000.00", "2022-07-19");

        // Achraf Hakimi: Real Madrid -> Inter Milan -> PSG
        addTransfer(transfers, players, teams, "Achraf Hakimi", "Real Madrid", "Inter Milan",
            TransferType.PERMANENT, "43000000.00", "2020-09-01");
        addTransfer(transfers, players, teams, "Achraf Hakimi", "Inter Milan", "Paris Saint-Germain",
            TransferType.PERMANENT, "60000000.00", "2021-07-06");

        // Example loan record so the player page can display loan history.
        addTransfer(transfers, players, teams, "Mason Greenwood", "Manchester United", "Olympique de Marseille",
            TransferType.LOAN, "0.00", "2025-08-01");

        transferRepository.saveAll(transfers);
    }

    private void addTransfer(
            List<Transfer> transfers,
            Map<String, Profile> players,
            Map<String, Team> teams,
            String playerName,
            String fromTeamName,
            String toTeamName,
            TransferType transferType,
            String fee,
            String transferDate
    ) {
        Profile player = players.get(playerName);
        Team fromTeam = teams.get(fromTeamName);
        Team toTeam = teams.get(toTeamName);

        if (player == null || fromTeam == null || toTeam == null) {
            return;
        }

        Transfer transfer = new Transfer();
        transfer.setPlayer(player);
        transfer.setFrom(fromTeam);
        transfer.setTo(toTeam);
        transfer.setTransferType(transferType);
        transfer.setFee(fee == null ? null : new BigDecimal(fee));
        transfer.setTransferDate(LocalDate.parse(transferDate));
        transfer.setCreatedAt(Instant.now());
        transfer.setUpdatedAt(Instant.now());
        transfers.add(transfer);
    }

    private String nextPhone() {
        return String.format("+234901%06d", phoneCounter++);
    }

    private String avatarUrl(String first, String last) {
        return "https://example.com/avatars/" + first.toLowerCase() + "-" + last.toLowerCase() + ".png";
    }

    // -------------------------------------------------------------------------
    // Fixtures
    // -------------------------------------------------------------------------

    private record MatchFixture(Team home, Team away) {
    }

    /**
     * Builds {@code count} fixtures for a league.
     *
     * IMPORTANT: this guarantees every team in {@code teams} appears in at
     * least one fixture before topping up the rest randomly. Previously this
     * shuffled every possible ordered pair and just took the first
     * {@code count}, which meant a team could end up in zero fixtures purely
     * by chance (e.g. with 20 teams and only 29-30 fixtures seeded out of 380
     * possible pairings). A team with no seeded matches has no rows in the
     * standings table, which is what caused matches to show only one team.
     */
    private List<MatchFixture> buildRemainingFixtures(List<Team> teams, int count) {
        List<MatchFixture> all = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = 0; j < teams.size(); j++) {
                if (i == j) {
                    continue;
                }
                all.add(new MatchFixture(teams.get(i), teams.get(j)));
            }
        }
        return selectFixturesCoveringAllTeams(teams, all, count);
    }

    /**
     * Selects up to {@code count} fixtures from {@code candidates}, first
     * guaranteeing every team in {@code teams} is paired at least once, then
     * filling the remainder with a random shuffle of the candidate pool.
     */
    private List<MatchFixture> selectFixturesCoveringAllTeams(
            List<Team> teams,
            List<MatchFixture> candidates,
            int count
    ) {
        // 1. Guarantee coverage: pair every team off once (randomly).
        List<Team> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams, rng);

        List<MatchFixture> guaranteed = new ArrayList<>();
        for (int i = 0; i + 1 < shuffledTeams.size(); i += 2) {
            Team a = shuffledTeams.get(i);
            Team b = shuffledTeams.get(i + 1);
            guaranteed.add(rng.nextBoolean() ? new MatchFixture(a, b) : new MatchFixture(b, a));
        }
        // Odd team out: pair with a random other team so it isn't left uncovered.
        if (shuffledTeams.size() % 2 == 1 && shuffledTeams.size() > 1) {
            Team last = shuffledTeams.get(shuffledTeams.size() - 1);
            Team opponent = shuffledTeams.get(rng.nextInt(shuffledTeams.size() - 1));
            guaranteed.add(rng.nextBoolean() ? new MatchFixture(last, opponent) : new MatchFixture(opponent, last));
        }

        // 2. Fill the remainder randomly from the candidate pool, skipping
        // fixtures already used in the guaranteed pass.
        List<MatchFixture> shuffledCandidates = new ArrayList<>(candidates);
        Collections.shuffle(shuffledCandidates, rng);

        java.util.Set<Long> usedTeamPairs = new HashSet<>();
        for (MatchFixture fixture : guaranteed) {
            usedTeamPairs.add(pairKey(fixture.home, fixture.away));
        }

        List<MatchFixture> result = new ArrayList<>(guaranteed);
        for (MatchFixture fixture : shuffledCandidates) {
            if (result.size() >= count) {
                break;
            }
            long key = pairKey(fixture.home, fixture.away);
            if (usedTeamPairs.contains(key)) {
                continue;
            }
            usedTeamPairs.add(key);
            result.add(fixture);
        }

        // If we still haven't hit count (small team pools), allow repeats
        // from the shuffled candidates rather than under-filling.
        int cursor = 0;
        while (result.size() < count && !shuffledCandidates.isEmpty()) {
            result.add(shuffledCandidates.get(cursor % shuffledCandidates.size()));
            cursor++;
        }

        return result.stream().limit(count).toList();
    }

    private long pairKey(Team home, Team away) {
        long h = home.getId() != null ? home.getId() : System.identityHashCode(home);
        long a = away.getId() != null ? away.getId() : System.identityHashCode(away);
        return h * 1_000_003L + a;
    }

    // -------------------------------------------------------------------------
    // Match + lineup + event seeding (Unchanged)
    // -------------------------------------------------------------------------

    private void seedCompletedMatch(
            Competition competition,
            Team home,
            Team away,
            Instant matchDate,
            Map<Long, List<Profile>> squadsByTeamId
    ) {
        Match match = new Match();
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setStadium(home.getName() + " Stadium");
        match.setMatchDate(matchDate);
        match.setMatchType(MatchType.REGULAR);
        match.setPeriod(MatchPeriod.FULL_TIME);
        match.setStatus(MatchStatus.FINISHED);
        match.setStartedAt(matchDate);
        match.setPeriodStartedAt(matchDate);
        match.setCompetition(competition);
        match.setMatchPeriods(new ArrayList<>(SEEDED_MATCH_PERIODS));
        match.setHomeScore(0);
        match.setAwayScore(0);
        match.setCreatedAt(Instant.now());
        match.setUpdatedAt(Instant.now());
        match = matchRepository.save(match);

        List<Profile> homeSquad = squadsByTeamId.get(home.getId());
        List<Profile> awaySquad = squadsByTeamId.get(away.getId());

        MatchLineup homeLineup = createLineup(match, home, homeSquad, randomFormation());
        MatchLineup awayLineup = createLineup(match, away, awaySquad, randomFormation());

        match.setHomeLineup(homeLineup);
        match.setAwayLineup(awayLineup);

        MatchScoreline scoreline = generateAndSaveEvents(match, home, away, homeLineup, awayLineup);

        match.setHomeScore(scoreline.homeGoals());
        match.setAwayScore(scoreline.awayGoals());
        match.setUpdatedAt(Instant.now());
        matchRepository.save(match);
    }

    private void seedScheduledMatch(Competition competition, Team home, Team away, Instant matchDate) {
        Match match = new Match();
        match.setHomeTeam(home);
        match.setAwayTeam(away);
        match.setStadium(home.getName() + " Stadium");
        match.setMatchDate(matchDate);
        match.setMatchType(MatchType.REGULAR);
        match.setPeriod(MatchPeriod.PRE_MATCH);
        match.setStatus(MatchStatus.SCHEDULED);
        match.setCompetition(competition);
        match.setMatchPeriods(new ArrayList<>());
        match.setHomeScore(0);
        match.setAwayScore(0);
        match.setCreatedAt(Instant.now());
        match.setUpdatedAt(Instant.now());
        matchRepository.save(match);
    }

    private Formation randomFormation() {
        return FORMATIONS[rng.nextInt(FORMATIONS.length)];
    }

    private MatchLineup createLineup(Match match, Team team, List<Profile> squad, Formation formation) {
        MatchLineup lineup = new MatchLineup();
        lineup.setMatch(match);
        lineup.setTeam(team);
        lineup.setFormation(formation);
        lineup.setCreatedAt(Instant.now());
        lineup.setUpdatedAt(Instant.now());

        int startersCount = Math.min(11, squad.size());
        int missingCount = squad.size() > 13 ? 2 : 0;
        int benchCount = squad.size() - startersCount - missingCount;

        List<LineupPlayer> players = new ArrayList<>();
        for (int i = 0; i < squad.size(); i++) {
            Profile profile = squad.get(i);
            LineupPlayer lp = new LineupPlayer();
            lp.setMatchLineup(lineup);
            lp.setPlayer(profile);
            lp.setPosition(profile.getPosition());
            lp.setCreatedAt(Instant.now());
            lp.setUpdatedAt(Instant.now());

            if (i < startersCount) {
                lp.setStatus(LineupStatus.STARTER);
            } else if (i < startersCount + benchCount) {
                lp.setStatus(LineupStatus.SUBSTITUTE);
            } else {
                lp.setStatus(LineupStatus.MISSING);
                lp.setMissingReason(i % 2 == 0 ? MissingReason.INJURED : MissingReason.SUSPENDED);
            }
            players.add(lp);
        }

        lineup.setPlayers(players);
        lineup.setCaptain(squad.get(Math.min(9, startersCount - 1)).getPosition() == null
                ? squad.get(0)
                : squad.get(Math.min(9, startersCount - 1)));

        return matchLineupRepository.save(lineup);
    }

    private record MatchScoreline(int homeGoals, int awayGoals) {
    }

    private MatchEvent buildEvent(
            Match match,
            Team team,
            MatchPeriod period,
            int minute,
            int second,
            EventType type,
            Profile primary,
            Profile secondary
    ) {
        MatchEvent event = new MatchEvent();
        event.setMatch(match);
        event.setTeam(team);
        event.setPeriod(period);
        event.setMinute(minute);
        event.setSecond(second);
        event.setEventType(type);
        event.setPrimaryPlayer(primary);
        event.setSecondaryPlayer(secondary);
        event.setCreatedAt(Instant.now());
        event.setUpdatedAt(Instant.now());
        return event;
    }

    private MatchScoreline generateAndSaveEvents(
            Match match,
            Team home,
            Team away,
            MatchLineup homeLineup,
            MatchLineup awayLineup
    ) {
        List<MatchEvent> events = new ArrayList<>();

        List<LineupPlayer> homeStarters = startersOf(homeLineup);
        List<LineupPlayer> awayStarters = startersOf(awayLineup);
        List<LineupPlayer> homeBench = benchOf(homeLineup);
        List<LineupPlayer> awayBench = benchOf(awayLineup);

        Profile homeGk = goalkeeperOf(homeStarters);
        Profile awayGk = goalkeeperOf(awayStarters);

        events.add(buildEvent(match, home, MatchPeriod.FIRST_HALF, 0, 0, EventType.KICK_OFF, null, null));

        int homeGoals = 0;
        int awayGoals = 0;

        int homeGoalCount = weightedGoalCount();
        int awayGoalCount = weightedGoalCount();

        for (int g = 0; g < homeGoalCount; g++) {
            int minute = randomMinute();
            boolean ownGoal = rng.nextInt(12) == 0;
            Team scoringTeam = ownGoal ? away : home;
            Profile scorer = ownGoal ? randomFieldPlayer(awayStarters) : randomFieldPlayer(homeStarters);
            EventType type = ownGoal ? EventType.OWN_GOAL : randomGoalType();
            Profile assist = ownGoal ? null : randomAssistFor(type, homeStarters, scorer);
            events.add(buildEvent(match, scoringTeam, periodForMinute(minute), minute, rng.nextInt(60), type, scorer, assist));
            homeGoals++;
        }

        for (int g = 0; g < awayGoalCount; g++) {
            int minute = randomMinute();
            boolean ownGoal = rng.nextInt(12) == 0;
            Team scoringTeam = ownGoal ? home : away;
            Profile scorer = ownGoal ? randomFieldPlayer(homeStarters) : randomFieldPlayer(awayStarters);
            EventType type = ownGoal ? EventType.OWN_GOAL : randomGoalType();
            Profile assist = ownGoal ? null : randomAssistFor(type, awayStarters, scorer);
            events.add(buildEvent(match, scoringTeam, periodForMinute(minute), minute, rng.nextInt(60), type, scorer, assist));
            awayGoals++;
        }

        int homeShotsOnTarget = 1 + rng.nextInt(2);
        int awayShotsOnTarget = 1 + rng.nextInt(2);
        addTeamEvents(events, match, home, homeStarters, EventType.SHOT_ON_TARGET, homeShotsOnTarget);
        addTeamEvents(events, match, away, awayStarters, EventType.SHOT_ON_TARGET, awayShotsOnTarget);
        addSaveEvents(events, match, away, awayGk, homeShotsOnTarget);
        addSaveEvents(events, match, home, homeGk, awayShotsOnTarget);

        addTeamEvents(events, match, home, homeStarters, EventType.SHOT_OFF_TARGET, 1 + rng.nextInt(2));
        addTeamEvents(events, match, away, awayStarters, EventType.SHOT_OFF_TARGET, 1 + rng.nextInt(2));
        addTeamEvents(events, match, home, homeStarters, EventType.SHOT_BLOCKED, rng.nextInt(2));
        addTeamEvents(events, match, away, awayStarters, EventType.SHOT_BLOCKED, rng.nextInt(2));
        addTeamEvents(events, match, home, homeStarters, EventType.CORNER, 1 + rng.nextInt(3));
        addTeamEvents(events, match, away, awayStarters, EventType.CORNER, 1 + rng.nextInt(3));
        addTeamEvents(events, match, home, homeStarters, EventType.FOUL, 1 + rng.nextInt(3));
        addTeamEvents(events, match, away, awayStarters, EventType.FOUL, 1 + rng.nextInt(3));
        addTeamEvents(events, match, home, homeStarters, EventType.OFFSIDE, rng.nextInt(2));
        addTeamEvents(events, match, away, awayStarters, EventType.OFFSIDE, rng.nextInt(2));

        int cardCount = 1 + rng.nextInt(3);
        for (int i = 0; i < cardCount; i++) {
            boolean isHome = rng.nextBoolean();
            Team team = isHome ? home : away;
            List<LineupPlayer> starters = isHome ? homeStarters : awayStarters;
            int minute = randomMinute();
            boolean straightRed = rng.nextInt(15) == 0;
            EventType type = straightRed ? EventType.RED_CARD : EventType.YELLOW_CARD;
            events.add(buildEvent(match, team, periodForMinute(minute), minute, rng.nextInt(60), type, randomFieldPlayer(starters), null));
        }

        addSubstitutions(events, match, home, homeStarters, homeBench, 2 + rng.nextInt(2));
        addSubstitutions(events, match, away, awayStarters, awayBench, 2 + rng.nextInt(2));

        events.add(buildEvent(match, home, MatchPeriod.FULL_TIME, 90, 0, EventType.FULL_TIME, null, null));

        matchEventRepository.saveAll(events);
        return new MatchScoreline(homeGoals, awayGoals);
    }

    private List<LineupPlayer> startersOf(MatchLineup lineup) {
        return lineup.getPlayers().stream()
                .filter(lp -> lp.getStatus() == LineupStatus.STARTER)
                .collect(Collectors.toList());
    }

    private List<LineupPlayer> benchOf(MatchLineup lineup) {
        return lineup.getPlayers().stream()
                .filter(lp -> lp.getStatus() == LineupStatus.SUBSTITUTE)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("null")
    private Profile goalkeeperOf(List<LineupPlayer> starters) {
        return starters.stream()
                .filter(lp -> lp.getPosition() == Position.GK)
                .map(LineupPlayer::getPlayer)
                .findFirst()
                .orElse(starters.get(0).getPlayer());
    }

    private Profile randomFieldPlayer(List<LineupPlayer> starters) {
        List<LineupPlayer> outfield = starters.stream()
                .filter(lp -> lp.getPosition() != Position.GK)
                .toList();
        if (outfield.isEmpty()) {
            return starters.get(rng.nextInt(starters.size())).getPlayer();
        }
        return outfield.get(rng.nextInt(outfield.size())).getPlayer();
    }

    private Profile randomAssistFor(EventType goalType, List<LineupPlayer> starters, Profile scorer) {
        if (!ASSISTABLE_GOAL_TYPES.contains(goalType)) {
            return null;
        }
        if (rng.nextInt(100) >= ASSIST_CHANCE_PERCENT) {
            return null;
        }
        List<LineupPlayer> candidates = starters.stream()
                .filter(lp -> lp.getPosition() != Position.GK)
                .filter(lp -> !lp.getPlayer().getId().equals(scorer.getId()))
                .toList();

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.get(rng.nextInt(candidates.size())).getPlayer();
    }

    private void addTeamEvents(
            List<MatchEvent> events,
            Match match,
            Team team,
            List<LineupPlayer> starters,
            EventType type,
            int count
    ) {
        for (int i = 0; i < count; i++) {
            int minute = randomMinute();
            events.add(buildEvent(match, team, periodForMinute(minute), minute, rng.nextInt(60), type, randomFieldPlayer(starters), null));
        }
    }

    private void addSaveEvents(List<MatchEvent> events, Match match, Team defendingTeam, Profile defendingGk, int count) {
        for (int i = 0; i < count; i++) {
            int minute = randomMinute();
            events.add(buildEvent(match, defendingTeam, periodForMinute(minute), minute, rng.nextInt(60), EventType.SAVE, defendingGk, null));
        }
    }

    private void addSubstitutions(
            List<MatchEvent> events,
            Match match,
            Team team,
            List<LineupPlayer> starters,
            List<LineupPlayer> bench,
            int count
    ) {
        List<LineupPlayer> offCandidates = starters.stream()
                .filter(lp -> lp.getPosition() != Position.GK)
                .collect(Collectors.toList());
        Collections.shuffle(offCandidates, rng);
        List<LineupPlayer> onCandidates = new ArrayList<>(bench);
        Collections.shuffle(onCandidates, rng);

        int actual = Math.min(count, Math.min(offCandidates.size(), onCandidates.size()));
        for (int i = 0; i < actual; i++) {
            int minute = 46 + rng.nextInt(40);
            Profile off = offCandidates.get(i).getPlayer();
            Profile on = onCandidates.get(i).getPlayer();
            events.add(buildEvent(match, team, MatchPeriod.SECOND_HALF, minute, rng.nextInt(60), EventType.SUBSTITUTION, off, on));
        }
    }

    private int weightedGoalCount() {
        int r = rng.nextInt(100);
        if (r < 25) return 0;
        if (r < 55) return 1;
        if (r < 80) return 2;
        if (r < 95) return 3;
        return 4;
    }

    private EventType randomGoalType() {
        return GOAL_TYPES[rng.nextInt(GOAL_TYPES.length)];
    }

    private int randomMinute() {
        return 1 + rng.nextInt(90);
    }

    private MatchPeriod periodForMinute(int minute) {
        return minute <= 45 ? MatchPeriod.FIRST_HALF : MatchPeriod.SECOND_HALF;
    }
}