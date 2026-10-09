package com.livescore.app.config;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.livescore.app.auth.User;
import com.livescore.app.auth.UserRepository;
import com.livescore.app.auth.enums.Role;
import com.livescore.app.config.SeedCatalog.FlagshipSpec;
import com.livescore.app.config.SeedCatalog.GeneratedLeagueSpec;
import com.livescore.app.competition.Competition;
import com.livescore.app.competition.CompetitionRepository;
import com.livescore.app.competition.enums.CompetitionLegFormat;
import com.livescore.app.competition.enums.CompetitionScope;
import com.livescore.app.competition.enums.CompetitionStatus;
import com.livescore.app.competition.enums.CompetitionType;
import com.livescore.app.league.League;
import com.livescore.app.league.LeagueRepository;
import com.livescore.app.leagueSubscription.LeagueSubscription;
import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;
import com.livescore.app.match.Match;
import com.livescore.app.match.MatchRepository;
import com.livescore.app.match.enums.MatchPeriod;
import com.livescore.app.match.enums.MatchStatus;
import com.livescore.app.match.enums.MatchType;
import com.livescore.app.matchEvents.MatchEvent;
import com.livescore.app.matchEvents.MatchEventRepository;
import com.livescore.app.matchEvents.enums.EventType;
import com.livescore.app.matchLineup.LineupPlayer;
import com.livescore.app.matchLineup.MatchLineup;
import com.livescore.app.matchLineup.MatchLineupRepository;
import com.livescore.app.matchLineup.enums.Formation;
import com.livescore.app.matchLineup.enums.LineupStatus;
import com.livescore.app.matchLineup.enums.MissingReason;
import com.livescore.app.profile.Profile;
import com.livescore.app.profile.ProfileRepository;
import com.livescore.app.profile.enums.Position;
import com.livescore.app.team.Team;
import com.livescore.app.team.TeamRepository;
import com.livescore.app.transfer.Transfer;
import com.livescore.app.transfer.TransferRepository;
import com.livescore.app.transfer.enums.TransferType;

import lombok.RequiredArgsConstructor;

/**
 * Seeds the platform admin, leagues, staff, subscriptions, teams, squads,
 * competitions, matches, lineups, events and transfers. Static data lives in
 * {@link SeedCatalog}.
 *
 * <p>Ownership model used by the seed data:
 * <ul>
 *   <li>A single platform {@code ADMIN} user (no league, no profile) is seeded
 *       unless an ADMIN or SUPERADMIN already exists.</li>
 *   <li>Every {@link Profile} (owner, admin, manager, player) belongs to exactly
 *       one league via {@code profile.league}.</li>
 *   <li>League owners and admins get a Profile + User.</li>
 *   <li>Moderators get a User only (no Profile); their scope is
 *       {@code user.league} and {@code user.role}.</li>
 *   <li>Every league gets a {@link LeagueSubscription} history that matches the
 *       plan/status stored on the league.</li>
 *   <li>Every {@link Transfer} is intra-league: both clubs share the same
 *       league, and {@code transfer.league} is that league.</li>
 * </ul>
 *
 * <p><b>Test logins</b> (Premier League, password {@value #SEED_PASSWORD}):
 * <ul>
 *   <li>{@value #TEST_MANAGER_1_EMAIL} - MANAGER of the first Premier League team</li>
 *   <li>{@value #TEST_MANAGER_2_EMAIL} - MANAGER of the second Premier League team</li>
 *   <li>{@value #TEST_LEAGUE_ADMIN_EMAIL} - LEAGUE_ADMIN</li>
 *   <li>{@value #TEST_MODERATOR_EMAIL} - MODERATOR</li>
 *   <li>{@value #ADMIN_EMAIL} - platform ADMIN</li>
 * </ul>
 *
 * <p>The seeder only runs on an empty database (the admin and the test logins
 * are topped up even on an existing one). To reseed, wipe the data first (for
 * example run once with {@code spring.jpa.hibernate.ddl-auto=create}).
 *
 * <p>Seed accounts use a well-known password, so never enable this in
 * production with {@code app.seed.enabled=false}. It is on by default.
 */
@Component
@ConditionalOnProperty(name = "app.seed.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    // -------------------------------------------------------------------------
    // Dependencies
    // -------------------------------------------------------------------------

    private final CompetitionRepository competitionRepository;
    private final TeamRepository teamRepository;
    private final ProfileRepository profileRepository;
    private final MatchRepository matchRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final MatchEventRepository matchEventRepository;
    private final TransferRepository transferRepository;
    private final LeagueRepository leagueRepository;
    private final LeagueSubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------

    static final String SEED_PASSWORD = "Password123";
    static final String ADMIN_EMAIL = "ericemeka732@gmail.com";

    static final String TEST_MANAGER_1_EMAIL = "manager1@livescore.app";
    static final String TEST_MANAGER_2_EMAIL = "manager2@livescore.app";
    static final String TEST_LEAGUE_ADMIN_EMAIL = "leagueadmin@livescore.app";
    static final String TEST_MODERATOR_EMAIL = "moderator@livescore.app";
    static final String TEST_LEAGUE_ADMIN_PHONE = "+234800000001";

    /** Competition code of the Premier League flagship (see SeedCatalog). */
    static final String PREMIER_LEAGUE_CODE = "EPL";

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

    /** Squad template used to generate a realistic 20-man squad for any team. */
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

    private static final Set<EventType> ASSISTABLE_GOAL_TYPES = Set.of(
            EventType.GOAL, EventType.FREE_KICK_GOAL);

    private static final int ASSIST_CHANCE_PERCENT = 70;

    private static final int GENERATED_LEAGUE_FIXTURES = 12;
    private static final int GENERATED_CUP_FIXTURES = 6;
    private static final int GENERATED_TOURNAMENT_FIXTURES = 6;
    private static final int GENERATED_TRANSFERS_PER_LEAGUE = 4;

    private static final List<MatchPeriod> SEEDED_MATCH_PERIODS =
            List.of(MatchPeriod.FIRST_HALF, MatchPeriod.SECOND_HALF);

    /** Yearly subscription price per plan. */
    private static final Map<SubscriptionPlan, BigDecimal> PLAN_PRICE = Map.of(
            SubscriptionPlan.FREE, new BigDecimal("0.00"),
            SubscriptionPlan.BASIC, new BigDecimal("120.00"),
            SubscriptionPlan.PROFESSIONAL, new BigDecimal("480.00"),
            SubscriptionPlan.ENTERPRISE, new BigDecimal("1500.00"));

    // -------------------------------------------------------------------------
    // State
    // -------------------------------------------------------------------------

    /**
     * Hashed once and reused for every seeded account. BCrypt is deliberately
     * slow, so hashing per user (about 100 accounts) dominated startup time.
     */
    private final String seedPasswordHash = new BCryptPasswordEncoder().encode(SEED_PASSWORD);

    /** Drives squads, names and match results. Seeded so re-runs are stable. */
    private final Random rng = new Random(42);

    /** Separate RNG for league staff so adding leagues doesn't shift squads. */
    private final Random staffRng = new Random(7);

    /** Fixed "now" so status calculations are consistent across the seed run. */
    private final Instant now = Instant.now();

    private int phoneCounter = 700;
    private int staffPhoneCounter = 100;
    private int moderatorCounter = 100;
    private int leagueCounter = 0;

    /** The test logins are attached to the Premier League only. */
    private boolean testLoginsSeeded = false;

    /** Teams keyed by competition code, used to build the Champions League pool. */
    private final Map<String, List<Team>> teamsByCompetitionCode = new LinkedHashMap<>();

    // -------------------------------------------------------------------------
    // Records
    // -------------------------------------------------------------------------

    private record MatchFixture(Team home, Team away) {
    }

    private record MatchScoreline(int homeGoals, int awayGoals) {
    }

    // =========================================================================
    // ENTRY POINT
    // =========================================================================

    @Override
    @Transactional
    public void run(String... args) {
        seedAdmin();

        if (leagueRepository.count() > 0 || competitionRepository.count() > 0) {
            topUpExistingData();
            return;
        }

        Map<Long, List<Profile>> squadsByTeamId = new HashMap<>();

        for (FlagshipSpec spec : SeedCatalog.FLAGSHIP_LEAGUES) {
            seedFlagship(spec, squadsByTeamId);
        }
        seedChampionsLeague(squadsByTeamId);
        seedGeneratedLeagues(squadsByTeamId);

        // Transfers last: everything they reference must already exist.
        seedTransfers(squadsByTeamId);
    }

    /** Data already exists: only fill in what is missing. */
    private void topUpExistingData() {
        if (subscriptionRepository.count() == 0) {
            leagueRepository.findAll().forEach(this::seedSubscriptions);
        }
        if (transferRepository.count() == 0) {
            seedTransfers(buildExistingSquads());
        }
        topUpTestLogins();
    }

    // =========================================================================
    // PLATFORM ADMIN
    // =========================================================================

    /**
     * Seeds one platform admin (no league, no profile) unless the admin email
     * is taken or an ADMIN / SUPERADMIN account already exists.
     */
    private void seedAdmin() {
        boolean exists = userRepository.findByEmail(ADMIN_EMAIL).isPresent()
                || userRepository.findAll().stream()
                        .anyMatch(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.SUPERADMIN);
        if (exists) {
            return;
        }

        User admin = new User();
        admin.setFirstName("Eric");
        admin.setLastName("Livescore");
        admin.setEmail(ADMIN_EMAIL);
        admin.setPassword(seedPasswordHash);
        admin.setEnabled(true);
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    // =========================================================================
    // TEST LOGINS (2 managers, 1 league admin, 1 moderator)
    // =========================================================================

    /**
     * Creates the well-known test accounts. Idempotent: any account whose email
     * already exists is skipped.
     */
    private void seedTestLogins(League league, Team teamOne, Team teamTwo) {
        createManagerLogin(TEST_MANAGER_1_EMAIL, teamOne);
        createManagerLogin(TEST_MANAGER_2_EMAIL, teamTwo);
        createTestLeagueAdmin(league);
        createTestModerator(league);
    }

    /** Gives the team's existing manager Profile a User account. */
    private void createManagerLogin(String email, Team team) {
        if (userRepository.findByEmail(email).isPresent()) {
            return;
        }
        Profile manager = team.getManager();
        if (manager == null) {
            return;
        }

        User user = new User();
        user.setFirstName(manager.getFirstName());
        user.setLastName(manager.getLastName());
        user.setEmail(email);
        user.setPassword(seedPasswordHash);
        user.setEnabled(true);
        user.setRole(Role.MANAGER);
        user.setLeague(team.getLeague());
        user.setProfile(manager);
        userRepository.save(user);
    }

    /** League admin = Profile + User, scoped to the league. */
    private void createTestLeagueAdmin(League league) {
        if (userRepository.findByEmail(TEST_LEAGUE_ADMIN_EMAIL).isPresent()) {
            return;
        }

        Profile profile = new Profile();
        profile.setFirstName("Test");
        profile.setLastName("LeagueAdmin");
        profile.setFullName("Test LeagueAdmin");
        profile.setPhoneNumber(TEST_LEAGUE_ADMIN_PHONE);
        profile.setAvatarUrl(avatarUrl("Test", "LeagueAdmin"));
        profile.setRole(Role.LEAGUE_ADMIN);
        profile.setLeague(league);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profile = profileRepository.save(profile);

        User user = new User();
        user.setFirstName("Test");
        user.setLastName("LeagueAdmin");
        user.setEmail(TEST_LEAGUE_ADMIN_EMAIL);
        user.setPassword(seedPasswordHash);
        user.setEnabled(true);
        user.setRole(Role.LEAGUE_ADMIN);
        user.setLeague(league);
        user.setProfile(profile);
        userRepository.save(user);
    }

    /** Moderator = User only, scoped to the league. */
    private void createTestModerator(League league) {
        if (userRepository.findByEmail(TEST_MODERATOR_EMAIL).isPresent()) {
            return;
        }

        User user = new User();
        user.setFirstName("Test");
        user.setLastName("Moderator");
        user.setEmail(TEST_MODERATOR_EMAIL);
        user.setPassword(seedPasswordHash);
        user.setEnabled(true);
        user.setRole(Role.MODERATOR);
        user.setLeague(league);
        userRepository.save(user);
    }

    /**
     * Existing database: attach the test logins to the Premier League (found
     * via its competition code), using the first two of its teams that have a
     * manager without a User account.
     */
    private void topUpTestLogins() {
        League premierLeague = competitionRepository.findAll().stream()
                .filter(c -> PREMIER_LEAGUE_CODE.equals(c.getCompetitionCode()))
                .map(Competition::getLeague)
                .findFirst()
                .orElse(null);
        if (premierLeague == null) {
            return;
        }

        Set<Long> managedProfileIds = userRepository.findAll().stream()
                .map(User::getProfile)
                .filter(p -> p != null && p.getId() != null)
                .map(Profile::getId)
                .collect(Collectors.toSet());

        List<Team> candidates = teamRepository.findAll().stream()
                .filter(t -> t.getLeague() != null && premierLeague.getId().equals(t.getLeague().getId()))
                .filter(t -> t.getManager() != null && !managedProfileIds.contains(t.getManager().getId()))
                .sorted(Comparator.comparing(Team::getId))
                .toList();

        if (candidates.size() >= 2) {
            seedTestLogins(premierLeague, candidates.get(0), candidates.get(1));
        }
    }

    // =========================================================================
    // FLAGSHIP LEAGUES
    // =========================================================================

    private void seedFlagship(FlagshipSpec spec, Map<Long, List<Profile>> squadsByTeamId) {
        League league = createLeague(spec.name(), spec.slug(), spec.description(), spec.plan());

        List<Team> teams = new ArrayList<>();
        for (String row : spec.teams()) {
            String[] parts = row.split("\\|");
            teams.add(createTeam(parts[0], parts[1], league, parts[2]));
        }
        teams = new ArrayList<>(teamRepository.saveAll(teams));

        Competition competition = createCompetition(
                league, spec.competitionName(), spec.code(),
                CompetitionScope.NATIONAL, CompetitionType.LEAGUE, CompetitionLegFormat.DOUBLE,
                CompetitionStatus.SCHEDULED, 38,
                LocalDateTime.parse(spec.startDate()), LocalDateTime.parse(spec.endDate()),
                teams);
        teamsByCompetitionCode.put(spec.code(), teams);

        // Hand-crafted squads for the featured clubs, generated for the rest.
        for (int i = 0; i < teams.size(); i++) {
            Team team = teams.get(i);
            List<Profile> squad = i < spec.featuredSquads().length
                    ? createSquadFromSpec(team, spec.featuredSquads()[i])
                    : createGeneratedSquadAndManager(team);
            squadsByTeamId.put(team.getId(), squad);
        }

        // Test logins: Premier League only, managers of its first two teams.
        if (!testLoginsSeeded && PREMIER_LEAGUE_CODE.equals(spec.code()) && teams.size() >= 2) {
            seedTestLogins(league, teams.get(0), teams.get(1));
            testLoginsSeeded = true;
        }

        Team featuredHome = teams.get(0);
        Team featuredAway = teams.get(1);

        List<MatchFixture> fixtures = new ArrayList<>();
        fixtures.add(new MatchFixture(featuredHome, featuredAway));
        fixtures.addAll(buildRemainingFixtures(teams, 29));

        seedCompetitionFixtures(competition, fixtures, daysFromNow(-30), squadsByTeamId);
        seedScheduledMatch(competition, featuredHome, featuredAway, daysFromNow(30));
    }

    // =========================================================================
    // CHAMPIONS LEAGUE
    // =========================================================================

    private void seedChampionsLeague(Map<Long, List<Profile>> squadsByTeamId) {
        League league = createLeague(
                "UEFA Club Competitions",
                "uefa-club-competitions",
                "European club competitions run across national leagues.",
                SubscriptionPlan.ENTERPRISE);

        // Top 4 from each flagship league (20), topped up to 32 from remaining teams.
        List<Team> allTeams = teamRepository.findAll();
        List<Team> uclTeams = new ArrayList<>();
        for (String code : List.of("EPL", "LFP", "L1", "LNPA", "BL")) {
            List<Team> leagueTeams = teamsByCompetitionCode.getOrDefault(code, List.of());
            uclTeams.addAll(leagueTeams.subList(0, Math.min(4, leagueTeams.size())));
        }
        for (Team team : allTeams) {
            if (uclTeams.size() >= 32) {
                break;
            }
            if (!uclTeams.contains(team)) {
                uclTeams.add(team);
            }
        }

        Competition competition = createCompetition(
                league, "UEFA Champions League 2025/26", "UCL",
                CompetitionScope.INTERNATIONAL, CompetitionType.LEAGUE, CompetitionLegFormat.DOUBLE,
                CompetitionStatus.SCHEDULED, 8,
                LocalDateTime.parse("2025-09-16T20:00:00"), LocalDateTime.parse("2026-05-30T20:00:00"),
                uclTeams);

        // Group stage: groups of 4, every pair plays home and away.
        Collections.shuffle(uclTeams, rng);
        List<MatchFixture> fixtures = new ArrayList<>();
        for (int i = 0; i + 4 <= uclTeams.size(); i += 4) {
            List<Team> group = uclTeams.subList(i, i + 4);
            for (int a = 0; a < group.size(); a++) {
                for (int b = a + 1; b < group.size(); b++) {
                    fixtures.add(new MatchFixture(group.get(a), group.get(b)));
                    fixtures.add(new MatchFixture(group.get(b), group.get(a)));
                }
            }
        }

        List<MatchFixture> selected = selectFixturesCoveringAllTeams(uclTeams, fixtures, 30);
        seedCompetitionFixtures(competition, selected, daysFromNow(-30), squadsByTeamId);

        // One guaranteed future fixture.
        seedScheduledMatch(competition, uclTeams.get(0), uclTeams.get(1), daysFromNow(30));
    }

    // =========================================================================
    // LEAGUES, STAFF, SUBSCRIPTIONS
    // =========================================================================

    private League createLeague(String name, String slug, String description, SubscriptionPlan plan) {
        return createLeague(name, slug, description, plan, SubscriptionStatus.ACTIVE, true);
    }

    private League createLeague(
            String name, String slug, String description,
            SubscriptionPlan plan, SubscriptionStatus status, boolean active) {
        League league = League.builder()
                .name(name)
                .slug(slug)
                .description(description)
                .logoUrl("https://example.com/logos/leagues/" + slug + ".png")
                .subscriptionPlan(plan)
                .subscriptionStatus(status)
                .active(active)
                .build();
        league = leagueRepository.save(league);
        seedLeagueStaff(league);
        seedSubscriptions(league);
        return league;
    }

    /**
     * An owner and two admins (each a Profile plus User), and two moderators
     * (User only). Every third league has one disabled moderator.
     */
    private void seedLeagueStaff(League league) {
        int index = leagueCounter++;

        createStaffMember(Role.LEAGUE_OWNER, league);
        createStaffMember(Role.LEAGUE_ADMIN, league);
        createStaffMember(Role.LEAGUE_ADMIN, league);

        createModerator(league, true);
        createModerator(league, index % 3 != 0);
    }

    /** Creates a league-scoped Profile plus its owning User account. */
    private void createStaffMember(Role role, League league) {
        String first = randomFirstName(staffRng);
        String last = randomLastName(staffRng);
        String phone = String.format("+234802%06d", staffPhoneCounter++);

        Profile profile = new Profile();
        profile.setFirstName(first);
        profile.setLastName(last);
        profile.setFullName(first + " " + last);
        profile.setPhoneNumber(phone);
        profile.setAvatarUrl(avatarUrl(first, last));
        profile.setRole(role);
        profile.setLeague(league);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
        profile = profileRepository.save(profile);

        User user = new User();
        user.setFirstName(first);
        user.setLastName(last);
        user.setEmail("staff" + phone.replaceAll("[^0-9]", "") + "@livescore.app");
        user.setPassword(seedPasswordHash);
        user.setEnabled(true);
        user.setRole(role);
        user.setLeague(league);
        user.setProfile(profile);
        userRepository.save(user);
    }

    /** A moderator is a User with no Profile, scoped by user.league / user.role. */
    private void createModerator(League league, boolean enabled) {
        User user = new User();
        user.setFirstName(randomFirstName(staffRng));
        user.setLastName(randomLastName(staffRng));
        user.setEmail(String.format("moderator%06d@livescore.app", moderatorCounter++));
        user.setPassword(seedPasswordHash);
        user.setEnabled(enabled);
        user.setRole(Role.MODERATOR);
        user.setLeague(league);
        userRepository.save(user);
    }

    /**
     * Subscription history matching the plan/status stored on the league: one
     * current row, plus an expired previous-year row for paid leagues that are
     * active or past due.
     */
    private void seedSubscriptions(League league) {
        SubscriptionPlan plan = league.getSubscriptionPlan();
        SubscriptionStatus status = league.getSubscriptionStatus();
        LocalDate today = LocalDate.now();

        LocalDate start;
        LocalDate end;
        BigDecimal amount = PLAN_PRICE.get(plan);

        switch (status) {
            case TRIAL -> {
                start = today.minusDays(10);
                end = today.plusDays(20);
                amount = BigDecimal.ZERO;
            }
            case PAST_DUE -> {
                start = today.minusDays(340);
                end = today.minusDays(5);
            }
            case SUSPENDED -> {
                start = today.minusDays(120);
                end = today.plusDays(245);
            }
            case CANCELLED -> {
                start = today.minusDays(200);
                end = today.plusDays(165);
            }
            case EXPIRED -> {
                start = today.minusDays(400);
                end = today.minusDays(35);
            }
            default -> { // ACTIVE
                start = today.minusDays(90);
                end = start.plusDays(365);
            }
        }

        List<LeagueSubscription> rows = new ArrayList<>();
        rows.add(newSubscription(league, plan, status, amount, start, end, "current"));

        boolean paid = plan != SubscriptionPlan.FREE;
        if (paid && (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.PAST_DUE)) {
            rows.add(newSubscription(
                    league, plan, SubscriptionStatus.EXPIRED, PLAN_PRICE.get(plan),
                    start.minusDays(365), start.minusDays(1), "previous"));
        }

        subscriptionRepository.saveAll(rows);
    }

    private LeagueSubscription newSubscription(
            League league, SubscriptionPlan plan, SubscriptionStatus status,
            BigDecimal amount, LocalDate start, LocalDate end, String period) {
        LeagueSubscription subscription = new LeagueSubscription();
        subscription.setLeague(league);
        subscription.setPlan(plan);
        subscription.setStatus(status);
        subscription.setAmount(amount);
        subscription.setStartDate(start);
        subscription.setEndDate(end);
        // Free plans and trials have no payment, so no reference.
        boolean hasPayment = plan != SubscriptionPlan.FREE && amount.signum() > 0;
        subscription.setPaymentReference(hasPayment ? "SEED-" + league.getSlug() + "-" + period : null);
        subscription.setCreatedAt(now);
        subscription.setUpdatedAt(now);
        return subscription;
    }

    // =========================================================================
    // GENERATED COMMUNITY LEAGUES
    // =========================================================================

    private void seedGeneratedLeagues(Map<Long, List<Profile>> squadsByTeamId) {
        List<Team> existingTeams = teamRepository.findAll();
        Set<String> usedTeamNames = existingTeams.stream()
                .map(Team::getName)
                .collect(Collectors.toCollection(HashSet::new));
        Set<String> usedTeamCodes = existingTeams.stream()
                .map(Team::getTeamCode)
                .collect(Collectors.toCollection(HashSet::new));

        LocalDateTime today = LocalDateTime.now();
        List<GeneratedLeagueSpec> specs = SeedCatalog.GENERATED_LEAGUES;

        for (int i = 0; i < specs.size(); i++) {
            GeneratedLeagueSpec spec = specs.get(i);
            boolean active = spec.active();

            League league = createLeague(
                    spec.name(), spec.slug(),
                    "Community football in " + spec.region() + ".",
                    spec.plan(), spec.status(), active);

            // Teams: one per town, with globally unique names and codes.
            List<Team> teams = new ArrayList<>();
            for (int t = 0; t < spec.towns().length; t++) {
                String town = spec.towns()[t];
                String suffix = SeedCatalog.TEAM_SUFFIXES[(i + t) % SeedCatalog.TEAM_SUFFIXES.length];

                String name = town + " " + suffix;
                while (!usedTeamNames.add(name)) {
                    name = name + " II";
                }
                teams.add(createTeam(name, uniqueCode(town, usedTeamCodes), league, town + " Community Ground"));
            }
            teams = new ArrayList<>(teamRepository.saveAll(teams));
            for (Team team : teams) {
                squadsByTeamId.put(team.getId(), createGeneratedSquadAndManager(team));
            }

            // 1) Main league.
            Competition mainLeague = createCompetition(
                    league, spec.region() + " League 2026/27", spec.code() + "-L",
                    CompetitionScope.LOCAL, CompetitionType.LEAGUE, CompetitionLegFormat.DOUBLE,
                    active ? CompetitionStatus.ONGOING : CompetitionStatus.COMPLETED,
                    2 * (teams.size() - 1),
                    active ? today.minusDays(45) : today.minusDays(300),
                    active ? today.plusDays(120) : today.minusDays(30),
                    teams);
            seedCompetitionFixtures(
                    mainLeague, buildRemainingFixtures(teams, GENERATED_LEAGUE_FIXTURES),
                    active ? daysFromNow(-30) : daysFromNow(-300), squadsByTeamId);

            // 2) Knockout cup (only played while the league is active).
            Competition cup = createCompetition(
                    league, spec.region() + " Cup 2026/27", spec.code() + "-C",
                    CompetitionScope.LOCAL, CompetitionType.CUP, CompetitionLegFormat.SINGLE,
                    active ? CompetitionStatus.ONGOING : CompetitionStatus.CANCELLED,
                    3, today.minusDays(20), today.plusDays(90), teams);
            if (active) {
                seedCompetitionFixtures(
                        cup, buildRemainingFixtures(teams, GENERATED_CUP_FIXTURES),
                        daysFromNow(-20), squadsByTeamId);
            }

            // 3) Every second league runs a six-team tournament.
            if (i % 2 == 0) {
                List<Team> tournamentTeams = new ArrayList<>(teams.subList(0, 6));
                Competition tournament = createCompetition(
                        league, spec.region() + " Champions Tournament", spec.code() + "-T",
                        CompetitionScope.LOCAL, CompetitionType.TOURNAMENT, CompetitionLegFormat.MIXED,
                        active ? CompetitionStatus.SCHEDULED : CompetitionStatus.COMPLETED,
                        4,
                        active ? today.plusDays(10) : today.minusDays(200),
                        active ? today.plusDays(60) : today.minusDays(150),
                        tournamentTeams);
                seedCompetitionFixtures(
                        tournament, buildRemainingFixtures(tournamentTeams, GENERATED_TOURNAMENT_FIXTURES),
                        active ? daysFromNow(10) : daysFromNow(-200), squadsByTeamId);
            }

            // 4) Every third league has a finished pre-season friendly series.
            if (i % 3 == 0) {
                List<Team> friendlyTeams = new ArrayList<>(teams.subList(0, 4));
                Competition friendly = createCompetition(
                        league, spec.region() + " Pre-season Friendlies", spec.code() + "-F",
                        CompetitionScope.LOCAL, CompetitionType.FRIENDLY, CompetitionLegFormat.SINGLE,
                        CompetitionStatus.COMPLETED,
                        1, today.minusDays(70), today.minusDays(55), friendlyTeams);
                seedCompetitionFixtures(
                        friendly, buildRemainingFixtures(friendlyTeams, 2),
                        daysFromNow(-70), squadsByTeamId);
            }
        }
    }

    /** First three letters of the town, made unique with a numeric suffix if needed. */
    private String uniqueCode(String town, Set<String> usedCodes) {
        String letters = town.replaceAll("[^A-Za-z]", "").toUpperCase();
        String base = letters.substring(0, Math.min(3, letters.length()));
        String code = base;
        int n = 2;
        while (!usedCodes.add(code)) {
            code = base + n++;
        }
        return code;
    }

    // =========================================================================
    // COMPETITIONS
    // =========================================================================

    private Competition createCompetition(
            League league, String name, String code,
            CompetitionScope scope, CompetitionType type, CompetitionLegFormat legFormat,
            CompetitionStatus status, int totalRounds,
            LocalDateTime startDate, LocalDateTime endDate,
            List<Team> teams) {

        Competition competition = new Competition();
        competition.setLeague(league);
        competition.setName(name);
        competition.setCompetitionCode(code);
        competition.setLogoUrl("https://example.com/logos/competitions/" + code.toLowerCase() + ".png");
        competition.setScope(scope);
        competition.setStatus(status);
        competition.setLegFormat(legFormat);
        competition.setCompetitionType(type);
        competition.setTotalTeams(teams.size());
        competition.setTotalRounds(totalRounds);
        competition.setStartDate(startDate);
        competition.setEndDate(endDate);
        competition.setCreatedAt(now);
        competition.setUpdatedAt(now);
        competition = competitionRepository.save(competition);

        competition.addTeams(teams);
        return competitionRepository.save(competition);
    }

    /** Past dates become finished matches, future ones scheduled. */
    private void seedCompetitionFixtures(
            Competition competition,
            List<MatchFixture> fixtures,
            Instant start,
            Map<Long, List<Profile>> squadsByTeamId) {

        Instant cursor = start;
        for (MatchFixture fixture : fixtures) {
            if (cursor.isBefore(now)) {
                seedCompletedMatch(competition, fixture.home(), fixture.away(), cursor, squadsByTeamId);
            } else {
                seedScheduledMatch(competition, fixture.home(), fixture.away(), cursor);
            }
            cursor = cursor.plus(3, ChronoUnit.DAYS);
        }
    }

    private Instant daysFromNow(long days) {
        return now.plus(days, ChronoUnit.DAYS);
    }

    // =========================================================================
    // TEAMS AND SQUADS
    // =========================================================================

    private Team createTeam(String name, String code, League league, String stadium) {
        Team team = new Team();
        team.setName(name);
        team.setTeamCode(code);
        team.setLogoUrl("https://example.com/logos/" + code + ".png");
        team.setStadium(stadium);
        team.setLeague(league);
        team.setCreatedAt(now);
        team.setUpdatedAt(now);
        return team;
    }

    /**
     * Builds a hand-crafted squad. {@code spec[0]} is the manager
     * ("First|Last"); the rest are players ("First|Last|number|POSITION").
     */
    private List<Profile> createSquadFromSpec(Team team, String[] spec) {
        String[] m = spec[0].split("\\|");
        assignManager(team, createManager(m[0], m[1], team));

        List<Profile> players = new ArrayList<>();
        for (int i = 1; i < spec.length; i++) {
            String[] p = spec[i].split("\\|");
            players.add(createPlayer(p[0], p[1], Integer.parseInt(p[2]), team, Position.valueOf(p[3])));
        }
        return profileRepository.saveAll(players);
    }

    private List<Profile> createGeneratedSquadAndManager(Team team) {
        assignManager(team, createManager(randomFirstName(rng), randomLastName(rng), team));

        List<Profile> players = new ArrayList<>();
        for (int i = 0; i < SQUAD_TEMPLATE.length; i++) {
            players.add(createPlayer(
                    randomFirstName(rng), randomLastName(rng), i + 1, team, SQUAD_TEMPLATE[i]));
        }
        return profileRepository.saveAll(players);
    }

    private void assignManager(Team team, Profile manager) {
        team.setManager(profileRepository.save(manager));
        teamRepository.save(team);
    }

    private Profile createManager(String firstName, String lastName, Team team) {
        Profile manager = baseProfile(firstName, lastName, team, Role.MANAGER, Position.MANAGER);
        manager.setSquadNumber(null);
        return manager;
    }

    private Profile createPlayer(String firstName, String lastName, Integer number, Team team, Position position) {
        Profile player = baseProfile(firstName, lastName, team, Role.PLAYER, position);
        player.setSquadNumber(number);
        player.setHeight(defaultHeight(position));
        player.setDateOfBirth(generatedDateOfBirth(firstName, lastName));
        return player;
    }

    private Profile baseProfile(String firstName, String lastName, Team team, Role role, Position position) {
        Profile profile = new Profile();
        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setFullName(firstName + " " + lastName);
        profile.setPhoneNumber(nextPhone());
        profile.setAvatarUrl(avatarUrl(firstName, lastName));
        profile.setTeam(team);
        profile.setLeague(team.getLeague());
        profile.setRole(role);
        profile.setPosition(position);
        profile.setCreatedAt(now);
        profile.setUpdatedAt(now);
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
        return LocalDate.of(1994 + (hash % 9), 1 + (hash % 12), 1 + (hash % 26));
    }

    // =========================================================================
    // TRANSFERS (intra-league only: from and to clubs share the same league)
    // =========================================================================

    /** Rebuilds teamId -> PLAYER list from the DB, for the top-up path. */
    private Map<Long, List<Profile>> buildExistingSquads() {
        Map<Long, List<Profile>> squads = new HashMap<>();
        for (Profile profile : profileRepository.findAll()) {
            if (profile.getRole() != Role.PLAYER
                    || profile.getTeam() == null
                    || profile.getTeam().getId() == null) {
                continue;
            }
            squads.computeIfAbsent(profile.getTeam().getId(), key -> new ArrayList<>()).add(profile);
        }
        return squads;
    }

    /**
     * Rules: both clubs must belong to the same league; transfer.league is
     * that league; every league gets some transfers so no page is empty.
     */
    private void seedTransfers(Map<Long, List<Profile>> squadsByTeamId) {
        if (transferRepository.count() > 0) {
            return;
        }

        Map<String, Team> teamsByName = new HashMap<>();
        Map<String, Team> teamsByCode = new HashMap<>();
        Map<Long, List<Team>> teamsByLeagueId = new LinkedHashMap<>();
        for (Team team : teamRepository.findAll()) {
            if (team.getName() != null) {
                teamsByName.put(team.getName(), team);
            }
            if (team.getTeamCode() != null) {
                teamsByCode.put(team.getTeamCode(), team);
            }
            if (team.getLeague() != null) {
                teamsByLeagueId
                        .computeIfAbsent(team.getLeague().getId(), key -> new ArrayList<>())
                        .add(team);
            }
        }

        Map<String, Profile> playersByFullName = squadsByTeamId.values().stream()
                .flatMap(List::stream)
                .filter(p -> p.getFullName() != null)
                .collect(Collectors.toMap(Profile::getFullName, p -> p, (first, second) -> first));

        List<Transfer> transfers = new ArrayList<>();
        Set<Long> usedPlayerIds = new HashSet<>();

        // 1) Curated transfers.
        for (String[] row : SeedCatalog.CURATED_TRANSFERS) {
            Profile player = playersByFullName.get(row[0]);
            Transfer transfer = buildTransfer(
                    player,
                    resolveTeam(teamsByName, teamsByCode, row[1]),
                    resolveTeam(teamsByName, teamsByCode, row[2]),
                    TransferType.valueOf(row[3]),
                    row[4] == null ? null : new BigDecimal(row[4]),
                    LocalDate.parse(row[5]));
            if (transfer != null) {
                transfers.add(transfer);
                usedPlayerIds.add(player.getId());
            }
        }

        // 2) Generated transfers so every league has its own history.
        Random transferRng = new Random(99);
        for (List<Team> leagueTeams : teamsByLeagueId.values()) {
            if (leagueTeams.size() < 2) {
                continue;
            }

            int created = 0;
            for (int attempts = 0; attempts < 40 && created < GENERATED_TRANSFERS_PER_LEAGUE; attempts++) {
                Team to = leagueTeams.get(transferRng.nextInt(leagueTeams.size()));
                Team from = leagueTeams.get(transferRng.nextInt(leagueTeams.size()));
                if (to.getId().equals(from.getId())) {
                    continue;
                }

                List<Profile> squad = squadsByTeamId.get(to.getId());
                if (squad == null || squad.isEmpty()) {
                    continue;
                }

                Profile player = squad.get(transferRng.nextInt(squad.size()));
                if (!usedPlayerIds.add(player.getId())) {
                    continue;
                }

                int roll = transferRng.nextInt(100);
                TransferType type;
                BigDecimal fee;
                if (roll < 60) {
                    type = TransferType.PERMANENT;
                    fee = BigDecimal.valueOf((1 + transferRng.nextInt(50)) * 10_000L).setScale(2);
                } else if (roll < 80) {
                    type = TransferType.LOAN;
                    fee = new BigDecimal("0.00");
                } else {
                    type = TransferType.FREE;
                    fee = null;
                }

                LocalDate date = LocalDate.now().minusDays(30L + transferRng.nextInt(700));
                Transfer transfer = buildTransfer(player, from, to, type, fee, date);
                if (transfer != null) {
                    transfers.add(transfer);
                    created++;
                }
            }
        }

        transferRepository.saveAll(transfers);
    }

    /**
     * Returns null when the transfer would be invalid: a missing player/team,
     * a club with no league, or clubs in different leagues.
     */
    private Transfer buildTransfer(
            Profile player, Team from, Team to,
            TransferType type, BigDecimal fee, LocalDate date) {

        if (player == null || from == null || to == null
                || from.getLeague() == null || to.getLeague() == null
                || !from.getLeague().getId().equals(to.getLeague().getId())) {
            return null;
        }

        Transfer transfer = new Transfer();
        transfer.setPlayer(player);
        transfer.setFrom(from);
        transfer.setTo(to);
        transfer.setTransferType(type);
        transfer.setFee(fee);
        transfer.setTransferDate(date);
        transfer.setLeague(to.getLeague());
        transfer.setCreatedAt(now);
        transfer.setUpdatedAt(now);
        return transfer;
    }

    /** Resolves a team by exact name first, then by team code. */
    private Team resolveTeam(Map<String, Team> byName, Map<String, Team> byCode, String key) {
        if (key == null) {
            return null;
        }
        Team team = byName.get(key);
        return team != null ? team : byCode.get(key);
    }

    // =========================================================================
    // FIXTURES
    // =========================================================================

    /** Builds {@code count} fixtures, guaranteeing every team plays at least once. */
    private List<MatchFixture> buildRemainingFixtures(List<Team> teams, int count) {
        List<MatchFixture> all = new ArrayList<>();
        for (int i = 0; i < teams.size(); i++) {
            for (int j = 0; j < teams.size(); j++) {
                if (i != j) {
                    all.add(new MatchFixture(teams.get(i), teams.get(j)));
                }
            }
        }
        return selectFixturesCoveringAllTeams(teams, all, count);
    }

    /**
     * Selects up to {@code count} fixtures: first pairs every team off once
     * (guaranteeing coverage), then fills the remainder randomly.
     */
    private List<MatchFixture> selectFixturesCoveringAllTeams(
            List<Team> teams, List<MatchFixture> candidates, int count) {

        // 1) Guarantee coverage.
        List<Team> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams, rng);

        List<MatchFixture> guaranteed = new ArrayList<>();
        for (int i = 0; i + 1 < shuffledTeams.size(); i += 2) {
            guaranteed.add(randomHomeAway(shuffledTeams.get(i), shuffledTeams.get(i + 1)));
        }
        if (shuffledTeams.size() % 2 == 1 && shuffledTeams.size() > 1) {
            Team last = shuffledTeams.get(shuffledTeams.size() - 1);
            Team opponent = shuffledTeams.get(rng.nextInt(shuffledTeams.size() - 1));
            guaranteed.add(randomHomeAway(last, opponent));
        }

        // 2) Fill the remainder randomly, skipping already-used pairings.
        List<MatchFixture> shuffledCandidates = new ArrayList<>(candidates);
        Collections.shuffle(shuffledCandidates, rng);

        Set<Long> usedPairs = new HashSet<>();
        guaranteed.forEach(f -> usedPairs.add(pairKey(f.home(), f.away())));

        List<MatchFixture> result = new ArrayList<>(guaranteed);
        for (MatchFixture f : shuffledCandidates) {
            if (result.size() >= count) {
                break;
            }
            if (usedPairs.add(pairKey(f.home(), f.away()))) {
                result.add(f);
            }
        }

        // 3) If the pool is too small, allow repeats rather than under-fill.
        for (int cursor = 0; result.size() < count && !shuffledCandidates.isEmpty(); cursor++) {
            result.add(shuffledCandidates.get(cursor % shuffledCandidates.size()));
        }

        return result.stream().limit(count).toList();
    }

    private MatchFixture randomHomeAway(Team a, Team b) {
        return rng.nextBoolean() ? new MatchFixture(a, b) : new MatchFixture(b, a);
    }

    private long pairKey(Team home, Team away) {
        long h = home.getId() != null ? home.getId() : System.identityHashCode(home);
        long a = away.getId() != null ? away.getId() : System.identityHashCode(away);
        return h * 1_000_003L + a;
    }

    // =========================================================================
    // MATCH + LINEUP + EVENT SEEDING
    // =========================================================================

    private void seedCompletedMatch(
            Competition competition, Team home, Team away, Instant matchDate,
            Map<Long, List<Profile>> squadsByTeamId) {

        List<Profile> homeSquad = squadsByTeamId.get(home.getId());
        List<Profile> awaySquad = squadsByTeamId.get(away.getId());
        if (homeSquad == null || homeSquad.isEmpty() || awaySquad == null || awaySquad.isEmpty()) {
            return; // can't build lineups without squads
        }

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
        match.setCreatedAt(now);
        match.setUpdatedAt(now);
        match = matchRepository.save(match);

        MatchLineup homeLineup = createLineup(match, home, homeSquad, randomFormation());
        MatchLineup awayLineup = createLineup(match, away, awaySquad, randomFormation());
        match.setHomeLineup(homeLineup);
        match.setAwayLineup(awayLineup);

        MatchScoreline scoreline = generateAndSaveEvents(match, home, away, homeLineup, awayLineup);
        match.setHomeScore(scoreline.homeGoals());
        match.setAwayScore(scoreline.awayGoals());
        match.setUpdatedAt(now);
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
        match.setCreatedAt(now);
        match.setUpdatedAt(now);
        matchRepository.save(match);
    }

    private Formation randomFormation() {
        return FORMATIONS[rng.nextInt(FORMATIONS.length)];
    }

    /** First 11 start, then the bench, then two unavailable players (squads over 13). */
    private MatchLineup createLineup(Match match, Team team, List<Profile> squad, Formation formation) {
        MatchLineup lineup = new MatchLineup();
        lineup.setMatch(match);
        lineup.setTeam(team);
        lineup.setFormation(formation);
        lineup.setCreatedAt(now);
        lineup.setUpdatedAt(now);

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
            lp.setCreatedAt(now);
            lp.setUpdatedAt(now);

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
        lineup.setCaptain(pickCaptain(squad, startersCount));
        return matchLineupRepository.save(lineup);
    }

    /** Prefers a midfielder around index 9, never a goalkeeper. */
    private Profile pickCaptain(List<Profile> squad, int startersCount) {
        for (int i = Math.min(9, startersCount - 1); i >= 0; i--) {
            if (squad.get(i).getPosition() != Position.GK) {
                return squad.get(i);
            }
        }
        return squad.get(0);
    }

    private MatchScoreline generateAndSaveEvents(
            Match match, Team home, Team away,
            MatchLineup homeLineup, MatchLineup awayLineup) {

        List<MatchEvent> events = new ArrayList<>();

        List<LineupPlayer> homeStarters = playersWithStatus(homeLineup, LineupStatus.STARTER);
        List<LineupPlayer> awayStarters = playersWithStatus(awayLineup, LineupStatus.STARTER);
        List<LineupPlayer> homeBench = playersWithStatus(homeLineup, LineupStatus.SUBSTITUTE);
        List<LineupPlayer> awayBench = playersWithStatus(awayLineup, LineupStatus.SUBSTITUTE);

        Profile homeGk = goalkeeperOf(homeStarters);
        Profile awayGk = goalkeeperOf(awayStarters);

        events.add(buildEvent(match, home, MatchPeriod.FIRST_HALF, 0, 0, EventType.KICK_OFF, null, null));

        int homeGoals = weightedGoalCount();
        int awayGoals = weightedGoalCount();
        addGoals(events, match, home, away, homeStarters, awayStarters, homeGoals);
        addGoals(events, match, away, home, awayStarters, homeStarters, awayGoals);

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
            int minute = randomMinute();
            EventType type = rng.nextInt(15) == 0 ? EventType.RED_CARD : EventType.YELLOW_CARD;
            events.add(buildEvent(match, isHome ? home : away, periodForMinute(minute),
                    minute, rng.nextInt(60), type,
                    randomFieldPlayer(isHome ? homeStarters : awayStarters), null));
        }

        addSubstitutions(events, match, home, homeStarters, homeBench, 2 + rng.nextInt(2));
        addSubstitutions(events, match, away, awayStarters, awayBench, 2 + rng.nextInt(2));

        events.add(buildEvent(match, home, MatchPeriod.FULL_TIME, 90, 0, EventType.FULL_TIME, null, null));

        // Chronological order, so ids follow the match timeline.
        events.sort(Comparator.comparingInt(MatchEvent::getMinute).thenComparingInt(MatchEvent::getSecond));

        matchEventRepository.saveAll(events);
        return new MatchScoreline(homeGoals, awayGoals);
    }

    /**
     * Adds {@code count} goals credited to {@code scoring}. About 1 in 12 is an
     * own goal, scored by a player of the opposing side.
     */
    private void addGoals(
            List<MatchEvent> events, Match match,
            Team scoring, Team conceding,
            List<LineupPlayer> scoringStarters, List<LineupPlayer> concedingStarters,
            int count) {

        for (int g = 0; g < count; g++) {
            int minute = randomMinute();
            boolean ownGoal = rng.nextInt(12) == 0;

            Team eventTeam = ownGoal ? conceding : scoring;
            Profile scorer = randomFieldPlayer(ownGoal ? concedingStarters : scoringStarters);
            EventType type = ownGoal ? EventType.OWN_GOAL : randomGoalType();
            Profile assist = ownGoal ? null : randomAssistFor(type, scoringStarters, scorer);

            events.add(buildEvent(match, eventTeam, periodForMinute(minute),
                    minute, rng.nextInt(60), type, scorer, assist));
        }
    }

    private MatchEvent buildEvent(
            Match match, Team team, MatchPeriod period,
            int minute, int second, EventType type,
            Profile primary, Profile secondary) {

        MatchEvent event = new MatchEvent();
        event.setMatch(match);
        event.setTeam(team);
        event.setPeriod(period);
        event.setMinute(minute);
        event.setSecond(second);
        event.setEventType(type);
        event.setPrimaryPlayer(primary);
        event.setSecondaryPlayer(secondary);
        event.setCreatedAt(now);
        event.setUpdatedAt(now);
        return event;
    }

    private List<LineupPlayer> playersWithStatus(MatchLineup lineup, LineupStatus status) {
        return lineup.getPlayers().stream()
                .filter(lp -> lp.getStatus() == status)
                .collect(Collectors.toList());
    }

    private Profile goalkeeperOf(List<LineupPlayer> starters) {
        return starters.stream()
                .filter(lp -> lp.getPosition() == Position.GK)
                .map(LineupPlayer::getPlayer)
                .findFirst()
                .orElseGet(() -> starters.get(0).getPlayer());
    }

    private Profile randomFieldPlayer(List<LineupPlayer> starters) {
        List<LineupPlayer> outfield = starters.stream()
                .filter(lp -> lp.getPosition() != Position.GK)
                .toList();
        List<LineupPlayer> pool = outfield.isEmpty() ? starters : outfield;
        return pool.get(rng.nextInt(pool.size())).getPlayer();
    }

    private Profile randomAssistFor(EventType goalType, List<LineupPlayer> starters, Profile scorer) {
        if (!ASSISTABLE_GOAL_TYPES.contains(goalType) || rng.nextInt(100) >= ASSIST_CHANCE_PERCENT) {
            return null;
        }
        List<LineupPlayer> candidates = starters.stream()
                .filter(lp -> lp.getPosition() != Position.GK)
                .filter(lp -> !lp.getPlayer().getId().equals(scorer.getId()))
                .toList();
        return candidates.isEmpty() ? null : candidates.get(rng.nextInt(candidates.size())).getPlayer();
    }

    private void addTeamEvents(
            List<MatchEvent> events, Match match, Team team,
            List<LineupPlayer> starters, EventType type, int count) {
        for (int i = 0; i < count; i++) {
            int minute = randomMinute();
            events.add(buildEvent(match, team, periodForMinute(minute),
                    minute, rng.nextInt(60), type, randomFieldPlayer(starters), null));
        }
    }

    private void addSaveEvents(
            List<MatchEvent> events, Match match, Team defendingTeam, Profile defendingGk, int count) {
        for (int i = 0; i < count; i++) {
            int minute = randomMinute();
            events.add(buildEvent(match, defendingTeam, periodForMinute(minute),
                    minute, rng.nextInt(60), EventType.SAVE, defendingGk, null));
        }
    }

    private void addSubstitutions(
            List<MatchEvent> events, Match match, Team team,
            List<LineupPlayer> starters, List<LineupPlayer> bench, int count) {

        List<LineupPlayer> off = starters.stream()
                .filter(lp -> lp.getPosition() != Position.GK)
                .collect(Collectors.toList());
        List<LineupPlayer> on = new ArrayList<>(bench);
        Collections.shuffle(off, rng);
        Collections.shuffle(on, rng);

        int actual = Math.min(count, Math.min(off.size(), on.size()));
        for (int i = 0; i < actual; i++) {
            events.add(buildEvent(match, team, MatchPeriod.SECOND_HALF,
                    46 + rng.nextInt(40), rng.nextInt(60), EventType.SUBSTITUTION,
                    off.get(i).getPlayer(), on.get(i).getPlayer()));
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

    /** 1 to 89, so the full-time event at minute 90 always sorts last. */
    private int randomMinute() {
        return 1 + rng.nextInt(89);
    }

    private MatchPeriod periodForMinute(int minute) {
        return minute <= 45 ? MatchPeriod.FIRST_HALF : MatchPeriod.SECOND_HALF;
    }

    // =========================================================================
    // UTIL
    // =========================================================================

    private String randomFirstName(Random random) {
        return FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
    }

    private String randomLastName(Random random) {
        return LAST_NAMES[random.nextInt(LAST_NAMES.length)];
    }

    private String nextPhone() {
        return String.format("+234901%06d", phoneCounter++);
    }

    private String avatarUrl(String first, String last) {
        String slug = (first + "-" + last).toLowerCase().replaceAll("\\s+", "-");
        return "https://example.com/avatars/" + slug + ".png";
    }
}