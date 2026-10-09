// package com.livescore.app.config;

// import java.math.BigDecimal;
// import java.time.Instant;
// import java.time.LocalDate;
// import java.util.ArrayList;
// import java.util.List;
// import java.util.Random;

// import org.springframework.boot.CommandLineRunner;
// import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
// import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
// import org.springframework.stereotype.Component;
// import org.springframework.transaction.annotation.Transactional;

// import com.livescore.app.auth.User;
// import com.livescore.app.auth.UserRepository;
// import com.livescore.app.auth.enums.Role;
// import com.livescore.app.league.League;
// import com.livescore.app.league.LeagueRepository;
// import com.livescore.app.leagueSubscription.LeagueSubscription;
// import com.livescore.app.leagueSubscription.LeagueSubscriptionRepository;
// import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
// import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;
// import com.livescore.app.profile.Profile;
// import com.livescore.app.profile.ProfileRepository;
// import com.livescore.app.profile.enums.Position;
// import com.livescore.app.team.Team;
// import com.livescore.app.team.TeamRepository;

// import lombok.RequiredArgsConstructor;

// /**
//  * MINIMAL seed: one Premier League, six Premier League clubs with full squads and managers, and a
//  * handful of test logins. NO competitions, matches, lineups, events or
//  * transfers: create those yourself through the app.
//  *
//  * <p>Enable with {@code app.seed.mode=minimal}. Runs only on an empty database
//  * (the platform admin is topped up regardless).
//  *
//  * <p>Logins (password {@value #SEED_PASSWORD}):
//  * <ul>
//  *   <li>{@value #ADMIN_EMAIL} - platform ADMIN</li>
//  *   <li>{@value #LEAGUE_ADMIN_EMAIL} - LEAGUE_ADMIN</li>
//  *   <li>{@value #MODERATOR_EMAIL} - MODERATOR</li>
//  *   <li>{@value #MANAGER_1_EMAIL} - MANAGER of Arsenal</li>
//  *   <li>{@value #MANAGER_2_EMAIL} - MANAGER of Manchester City</li>
//  * </ul>
//  *
//  * <p>Well-known password: never enable this in production.
//  */
// @Component
// @ConditionalOnProperty(name = "app.seed.mode", havingValue = "minimal")
// @RequiredArgsConstructor
// public class MinimalDataSeeder implements CommandLineRunner {

//     private final LeagueRepository leagueRepository;
//     private final LeagueSubscriptionRepository subscriptionRepository;
//     private final TeamRepository teamRepository;
//     private final ProfileRepository profileRepository;
//     private final UserRepository userRepository;

//     static final String SEED_PASSWORD = "Password123";
//     static final String ADMIN_EMAIL = "ericemeka732@gmail.com";
//     static final String LEAGUE_ADMIN_EMAIL = "leagueadmin@livescore.app";
//     static final String MODERATOR_EMAIL = "moderator@livescore.app";
//     static final String MANAGER_1_EMAIL = "manager1@livescore.app";
//     static final String MANAGER_2_EMAIL = "manager2@livescore.app";

//     /** name | code | stadium */
//     private static final String[][] TEAMS = {
//             {"Arsenal", "ARS", "Emirates Stadium"},
//             {"Manchester City", "MCI", "Etihad Stadium"},
//             {"Liverpool", "LIV", "Anfield"},
//             {"Chelsea", "CHE", "Stamford Bridge"},
//             {"Manchester United", "MUN", "Old Trafford"},
//             {"Tottenham Hotspur", "TOT", "Tottenham Hotspur Stadium"}
//     };

//     private static final String[] FIRST_NAMES = {
//             "James", "Daniel", "Michael", "Thomas", "Ryan", "Callum", "Harry", "Jack", "Sam", "Luke",
//             "Oliver", "George", "Charlie", "Josh", "Ben", "Adam", "Connor", "Aaron", "Liam", "Ethan",
//             "Mason", "Tyler", "Kyle", "Jamie", "Alex", "Marcus", "Andre", "Bruno", "Diego", "Rafael"
//     };

//     private static final String[] LAST_NAMES = {
//             "Johnson", "Smith", "Williams", "Brown", "Taylor", "Anderson", "Thomas", "Jackson", "White", "Harris",
//             "Martin", "Clark", "Lewis", "Walker", "Young", "Allen", "King", "Wright", "Scott", "Green",
//             "Baker", "Adams", "Nelson", "Carter", "Mitchell", "Perez", "Roberts", "Turner", "Phillips", "Campbell"
//     };

//     private static final Position[] SQUAD_TEMPLATE = {
//             Position.GK, Position.RB, Position.CB, Position.CB, Position.LB,
//             Position.CDM, Position.CM, Position.CM, Position.RW, Position.ST, Position.LW,
//             Position.GK, Position.CB, Position.LB, Position.CDM, Position.CAM, Position.CF, Position.ST,
//             Position.RB, Position.CM
//     };

//     private final String seedPasswordHash = new BCryptPasswordEncoder().encode(SEED_PASSWORD);
//     private final Random rng = new Random(42);
//     private final Instant now = Instant.now();
//     private int phoneCounter = 700;

//     @Override
//     @Transactional
//     public void run(String... args) {
//         seedPlatformAdmin();

//         if (leagueRepository.count() > 0) {
//             return;
//         }

//         League league = seedLeague();
//         seedLeagueAdmin(league);
//         seedModerator(league);

//         List<Team> teams = new ArrayList<>();
//         for (String[] row : TEAMS) {
//             teams.add(createTeam(row[0], row[1], row[2], league));
//         }
//         teams = new ArrayList<>(teamRepository.saveAll(teams));

//         for (int i = 0; i < teams.size(); i++) {
//             Team team = teams.get(i);
//             Profile manager = createSquadAndManager(team);

//             // The first two managers get a login.
//             if (i == 0) {
//                 createManagerLogin(MANAGER_1_EMAIL, manager, league);
//             } else if (i == 1) {
//                 createManagerLogin(MANAGER_2_EMAIL, manager, league);
//             }
//         }
//     }

//     // -------------------------------------------------------------------------
//     // Accounts
//     // -------------------------------------------------------------------------

//     private void seedPlatformAdmin() {
//         boolean exists = userRepository.findByEmail(ADMIN_EMAIL).isPresent()
//                 || userRepository.findAll().stream()
//                         .anyMatch(u -> u.getRole() == Role.ADMIN || u.getRole() == Role.SUPERADMIN);
//         if (exists) {
//             return;
//         }

//         User admin = new User();
//         admin.setFirstName("Eric");
//         admin.setLastName("Livescore");
//         admin.setEmail(ADMIN_EMAIL);
//         admin.setPassword(seedPasswordHash);
//         admin.setEnabled(true);
//         admin.setRole(Role.ADMIN);
//         userRepository.save(admin);
//     }

//     /** League admin = Profile + User, scoped to the league. */
//     private void seedLeagueAdmin(League league) {
//         Profile profile = new Profile();
//         profile.setFirstName("Test");
//         profile.setLastName("LeagueAdmin");
//         profile.setFullName("Test LeagueAdmin");
//         profile.setPhoneNumber("+234800000001");
//         profile.setAvatarUrl(avatarUrl("Test", "LeagueAdmin"));
//         profile.setRole(Role.LEAGUE_ADMIN);
//         profile.setLeague(league);
//         profile.setCreatedAt(now);
//         profile.setUpdatedAt(now);
//         profile = profileRepository.save(profile);

//         User user = new User();
//         user.setFirstName("Test");
//         user.setLastName("LeagueAdmin");
//         user.setEmail(LEAGUE_ADMIN_EMAIL);
//         user.setPassword(seedPasswordHash);
//         user.setEnabled(true);
//         user.setRole(Role.LEAGUE_ADMIN);
//         user.setLeague(league);
//         user.setProfile(profile);
//         userRepository.save(user);
//     }

//     /** Moderator = User only, scoped to the league. */
//     private void seedModerator(League league) {
//         User user = new User();
//         user.setFirstName("Test");
//         user.setLastName("Moderator");
//         user.setEmail(MODERATOR_EMAIL);
//         user.setPassword(seedPasswordHash);
//         user.setEnabled(true);
//         user.setRole(Role.MODERATOR);
//         user.setLeague(league);
//         userRepository.save(user);
//     }

//     private void createManagerLogin(String email, Profile manager, League league) {
//         User user = new User();
//         user.setFirstName(manager.getFirstName());
//         user.setLastName(manager.getLastName());
//         user.setEmail(email);
//         user.setPassword(seedPasswordHash);
//         user.setEnabled(true);
//         user.setRole(Role.MANAGER);
//         user.setLeague(league);
//         user.setProfile(manager);
//         userRepository.save(user);
//     }

//     // -------------------------------------------------------------------------
//     // League + subscription
//     // -------------------------------------------------------------------------

//     private League seedLeague() {
//         League league = League.builder()
//                 .name("Premier League")
//                 .slug("premier-league")
//                 .description("Minimal seeded Premier League for manual testing.")
//                 .logoUrl("https://example.com/logos/leagues/premier-league.png")
//                 .subscriptionPlan(SubscriptionPlan.PROFESSIONAL)
//                 .subscriptionStatus(SubscriptionStatus.ACTIVE)
//                 .active(true)
//                 .build();
//         league = leagueRepository.save(league);

//         LocalDate today = LocalDate.now();
//         LeagueSubscription subscription = new LeagueSubscription();
//         subscription.setLeague(league);
//         subscription.setPlan(SubscriptionPlan.PROFESSIONAL);
//         subscription.setStatus(SubscriptionStatus.ACTIVE);
//         subscription.setAmount(new BigDecimal("480.00"));
//         subscription.setStartDate(today.minusDays(30));
//         subscription.setEndDate(today.plusDays(335));
//         subscription.setPaymentReference("SEED-premier-league-current");
//         subscription.setCreatedAt(now);
//         subscription.setUpdatedAt(now);
//         subscriptionRepository.save(subscription);

//         return league;
//     }

//     // -------------------------------------------------------------------------
//     // Teams and squads
//     // -------------------------------------------------------------------------

//     private Team createTeam(String name, String code, String stadium, League league) {
//         Team team = new Team();
//         team.setName(name);
//         team.setTeamCode(code);
//         team.setLogoUrl("https://example.com/logos/" + code + ".png");
//         team.setStadium(stadium);
//         team.setLeague(league);
//         team.setCreatedAt(now);
//         team.setUpdatedAt(now);
//         return team;
//     }

//     /** Creates a manager plus a 20-man squad; returns the saved manager. */
//     private Profile createSquadAndManager(Team team) {
//         Profile manager = baseProfile(randomFirst(), randomLast(), team, Role.MANAGER, Position.MANAGER);
//         manager.setSquadNumber(null);
//         manager = profileRepository.save(manager);
//         team.setManager(manager);
//         teamRepository.save(team);

//         List<Profile> players = new ArrayList<>();
//         for (int i = 0; i < SQUAD_TEMPLATE.length; i++) {
//             Profile player = baseProfile(randomFirst(), randomLast(), team, Role.PLAYER, SQUAD_TEMPLATE[i]);
//             player.setSquadNumber(i + 1);
//             player.setHeight(defaultHeight(SQUAD_TEMPLATE[i]));
//             player.setDateOfBirth(generatedDateOfBirth(player.getFirstName(), player.getLastName()));
//             players.add(player);
//         }
//         profileRepository.saveAll(players);

//         return manager;
//     }

//     private Profile baseProfile(String first, String last, Team team, Role role, Position position) {
//         Profile profile = new Profile();
//         profile.setFirstName(first);
//         profile.setLastName(last);
//         profile.setFullName(first + " " + last);
//         profile.setPhoneNumber(String.format("+234901%06d", phoneCounter++));
//         profile.setAvatarUrl(avatarUrl(first, last));
//         profile.setTeam(team);
//         profile.setLeague(team.getLeague());
//         profile.setRole(role);
//         profile.setPosition(position);
//         profile.setCreatedAt(now);
//         profile.setUpdatedAt(now);
//         return profile;
//     }

//     private int defaultHeight(Position position) {
//         return switch (position) {
//             case GK -> 190;
//             case CB -> 186;
//             case LB, RB -> 178;
//             case CDM, CM, CAM -> 180;
//             case LW, RW, CF, ST -> 181;
//             default -> 180;
//         };
//     }

//     private LocalDate generatedDateOfBirth(String first, String last) {
//         int hash = Math.floorMod((first + ":" + last).hashCode(), Integer.MAX_VALUE);
//         return LocalDate.of(1994 + (hash % 9), 1 + (hash % 12), 1 + (hash % 26));
//     }

//     // -------------------------------------------------------------------------
//     // Util
//     // -------------------------------------------------------------------------

//     private String randomFirst() {
//         return FIRST_NAMES[rng.nextInt(FIRST_NAMES.length)];
//     }

//     private String randomLast() {
//         return LAST_NAMES[rng.nextInt(LAST_NAMES.length)];
//     }

//     private String avatarUrl(String first, String last) {
//         String slug = (first + "-" + last).toLowerCase().replaceAll("\\s+", "-");
//         return "https://example.com/avatars/" + slug + ".png";
//     }
// }