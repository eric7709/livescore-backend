package com.livescore.app.config;

import java.util.List;

import com.livescore.app.leagueSubscription.enums.SubscriptionPlan;
import com.livescore.app.leagueSubscription.enums.SubscriptionStatus;

/**
 * Static data used by {@link DataSeeder}: flagship leagues, hand-crafted
 * squads, community league specs and curated transfers. No logic lives here.
 */
final class SeedCatalog {

    private SeedCatalog() {
    }

    // -------------------------------------------------------------------------
    // Specs
    // -------------------------------------------------------------------------

    /** Spec for a generated community league. */
    record GeneratedLeagueSpec(
            String name,
            String slug,
            String region,
            String code,
            SubscriptionPlan plan,
            SubscriptionStatus status,
            boolean active,
            String[] towns) {
    }

    /**
     * Spec for a flagship league. {@code teams} rows are "name|code|stadium".
     * {@code featuredSquads} are hand-crafted squads for the first teams; each
     * array starts with the manager ("First|Last") followed by players
     * ("First|Last|number|POSITION").
     */
    record FlagshipSpec(
            String name,
            String slug,
            String description,
            SubscriptionPlan plan,
            String competitionName,
            String code,
            String startDate,
            String endDate,
            String[] teams,
            String[][] featuredSquads) {
    }

    static final String[] TEAM_SUFFIXES = {
            "United", "Rovers", "Athletic", "City", "Wanderers", "Town", "Rangers", "Albion"
    };

    // -------------------------------------------------------------------------
    // Community leagues
    // -------------------------------------------------------------------------

    static final List<GeneratedLeagueSpec> GENERATED_LEAGUES = List.of(
            new GeneratedLeagueSpec("Lagos Metro Football League", "lagos-metro-football-league", "Lagos", "LGS",
                    SubscriptionPlan.PROFESSIONAL, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Ikeja", "Surulere", "Yaba", "Lekki", "Ajah", "Badagry", "Epe", "Ikorodu"}),
            new GeneratedLeagueSpec("Abuja Capital Football League", "abuja-capital-football-league", "Abuja", "ABJ",
                    SubscriptionPlan.BASIC, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Garki", "Wuse", "Maitama", "Gwarinpa", "Kubwa", "Lugbe", "Jabi", "Asokoro"}),
            new GeneratedLeagueSpec("Port Harcourt Garden City League", "port-harcourt-garden-city-league", "Port Harcourt", "PHC",
                    SubscriptionPlan.BASIC, SubscriptionStatus.TRIAL, true,
                    new String[]{"Rumuola", "Diobu", "Trans-Amadi", "Eleme", "Bonny", "Okrika", "Oyigbo", "Obio"}),
            new GeneratedLeagueSpec("Kano Northern Football League", "kano-northern-football-league", "Kano", "KAN",
                    SubscriptionPlan.FREE, SubscriptionStatus.TRIAL, true,
                    new String[]{"Nasarawa", "Fagge", "Gwale", "Tarauni", "Dala", "Ungogo", "Kumbotso", "Minjibir"}),
            new GeneratedLeagueSpec("Ibadan Oyo State League", "ibadan-oyo-state-league", "Ibadan", "IBD",
                    SubscriptionPlan.BASIC, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Bodija", "Mokola", "Dugbe", "Agodi", "Iwo Road", "Challenge", "Sango", "Apata"}),
            new GeneratedLeagueSpec("Accra Coastal League", "accra-coastal-league", "Accra", "ACC",
                    SubscriptionPlan.PROFESSIONAL, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Osu", "Labadi", "Tema", "Madina", "Kasoa", "Teshie", "Nungua", "Dansoman"}),
            new GeneratedLeagueSpec("Nairobi City Football League", "nairobi-city-football-league", "Nairobi", "NBO",
                    SubscriptionPlan.PROFESSIONAL, SubscriptionStatus.PAST_DUE, true,
                    new String[]{"Kilimani", "Westlands", "Karen", "Kasarani", "Embakasi", "Langata", "Eastleigh", "Roysambu"}),
            new GeneratedLeagueSpec("Johannesburg Metro League", "johannesburg-metro-league", "Johannesburg", "JNB",
                    SubscriptionPlan.ENTERPRISE, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Soweto", "Sandton", "Alexandra", "Randburg", "Midrand", "Benoni", "Roodepoort", "Germiston"}),
            new GeneratedLeagueSpec("Cairo Nile Football League", "cairo-nile-football-league", "Cairo", "CAI",
                    SubscriptionPlan.BASIC, SubscriptionStatus.SUSPENDED, false,
                    new String[]{"Maadi", "Zamalek", "Heliopolis", "Nasr", "Giza", "Shubra", "Dokki", "Helwan"}),
            new GeneratedLeagueSpec("Casablanca Atlas League", "casablanca-atlas-league", "Casablanca", "CAS",
                    SubscriptionPlan.FREE, SubscriptionStatus.EXPIRED, false,
                    new String[]{"Anfa", "Maarif", "Hay Hassani", "Sidi Maarouf", "Ain Sebaa", "Bouskoura", "Mohammedia", "Berrechid"}),
            new GeneratedLeagueSpec("Dakar Teranga League", "dakar-teranga-league", "Dakar", "DKR",
                    SubscriptionPlan.BASIC, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Plateau", "Medina", "Yoff", "Ouakam", "Pikine", "Guediawaye", "Rufisque", "Thies"}),
            new GeneratedLeagueSpec("Lisbon Tagus League", "lisbon-tagus-league", "Lisbon", "LIS",
                    SubscriptionPlan.PROFESSIONAL, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Alfama", "Belem", "Lumiar", "Ajuda", "Oeiras", "Cascais", "Amadora", "Sintra"}),
            new GeneratedLeagueSpec("Amsterdam Canal League", "amsterdam-canal-league", "Amsterdam", "AMS",
                    SubscriptionPlan.ENTERPRISE, SubscriptionStatus.ACTIVE, true,
                    new String[]{"Jordaan", "Oost", "Noord", "Zuid", "Bijlmer", "Buitenveldert", "Sloten", "Osdorp"}),
            new GeneratedLeagueSpec("Toronto Lakeshore League", "toronto-lakeshore-league", "Toronto", "TOR",
                    SubscriptionPlan.FREE, SubscriptionStatus.CANCELLED, false,
                    new String[]{"Etobicoke", "Scarborough", "Leslieville", "Danforth", "Parkdale", "Rexdale", "Weston", "Riverdale"})
    );

    // -------------------------------------------------------------------------
    // Curated transfers
    // -------------------------------------------------------------------------

    /**
     * Hand-picked transfers where both clubs are in the same seeded league.
     * Row: player | from | to | type | fee (null = none) | date.
     * Fees and dates are approximate seed data.
     */
    static final String[][] CURATED_TRANSFERS = {
            // Premier League
            {"Declan Rice", "West Ham United", "Arsenal", "PERMANENT", "105000000.00", "2023-07-15"},
            {"Kai Havertz", "Chelsea", "Arsenal", "PERMANENT", "65000000.00", "2023-06-28"},
            {"Jorginho Frello", "Chelsea", "Arsenal", "PERMANENT", "12000000.00", "2023-01-31"},
            {"Ben White", "Brighton & Hove Albion", "Arsenal", "PERMANENT", "50000000.00", "2021-07-30"},
            {"Moises Caicedo", "Brighton & Hove Albion", "Chelsea", "PERMANENT", "115000000.00", "2023-08-14"},
            {"Marc Cucurella", "Brighton & Hove Albion", "Chelsea", "PERMANENT", "62000000.00", "2022-08-05"},
            {"Cole Palmer", "Manchester City", "Chelsea", "PERMANENT", "42500000.00", "2023-09-01"},
            {"Wesley Fofana", "Leicester City", "Chelsea", "PERMANENT", "70000000.00", "2022-09-01"},
            {"Kiernan Dewsbury-Hall", "Leicester City", "Chelsea", "PERMANENT", "30000000.00", "2024-08-01"},
            {"Tosin Adarabioyo", "Fulham", "Chelsea", "FREE", null, "2024-07-01"},

            // La Liga
            {"Jules Kounde", "Sevilla", "FC Barcelona", "PERMANENT", "50000000.00", "2022-07-29"},
            {"Inigo Martinez", "Athletic Bilbao", "FC Barcelona", "FREE", null, "2023-07-01"},
            {"Fran Garcia", "Rayo Vallecano", "Real Madrid", "PERMANENT", "5000000.00", "2023-06-20"},
            {"Dani Ceballos", "Real Betis", "Real Madrid", "PERMANENT", "16500000.00", "2017-07-14"},

            // Ligue 1
            {"Bradley Barcola", "Olympique Lyonnais", "Paris Saint-Germain", "PERMANENT", "45000000.00", "2023-07-20"},
            {"Jonathan Clauss", "RC Lens", "Olympique de Marseille", "PERMANENT", "12000000.00", "2023-07-04"},
            {"Facundo Medina", "RC Lens", "Olympique de Marseille", "PERMANENT", "15000000.00", "2023-08-25"},
            {"Angel Gomes", "LOSC Lille", "Olympique de Marseille", "FREE", null, "2024-07-01"},

            // Serie A
            {"Teun Koopmeiners", "Atalanta", "Juventus", "PERMANENT", "58000000.00", "2024-08-30"},
            {"Gleison Bremer", "Torino FC", "Juventus", "PERMANENT", "41000000.00", "2022-07-21"},
            {"Andrea Cambiaso", "Genoa CFC", "Juventus", "PERMANENT", "12000000.00", "2023-07-14"},
            {"Michele Di Gregorio", "Monza", "Juventus", "PERMANENT", "18000000.00", "2024-07-01"},
            {"Hakan Calhanoglu", "AC Milan", "Inter Milan", "FREE", null, "2021-07-01"},
            {"Henrikh Mkhitaryan", "AS Roma", "Inter Milan", "FREE", null, "2022-07-01"},
            {"Piotr Zielinski", "SSC Napoli", "Inter Milan", "FREE", null, "2024-07-01"},
            {"Nicolo Barella", "Cagliari Calcio", "Inter Milan", "PERMANENT", "12000000.00", "2019-07-04"},

            // Bundesliga
            {"Jonathan Tah", "Bayer Leverkusen", "Bayern Munich", "PERMANENT", "25000000.00", "2025-06-01"},
            {"Konrad Laimer", "RB Leipzig", "Bayern Munich", "FREE", null, "2023-07-01"},
            {"Dayot Upamecano", "RB Leipzig", "Bayern Munich", "PERMANENT", "42500000.00", "2021-07-01"},
            {"Serhou Guirassy", "VfB Stuttgart", "Borussia Dortmund", "PERMANENT", "18000000.00", "2024-07-01"},
            {"Waldemar Anton", "VfB Stuttgart", "Borussia Dortmund", "PERMANENT", "22500000.00", "2024-07-01"},
            {"Maximilian Beier", "TSG Hoffenheim", "Borussia Dortmund", "PERMANENT", "22500000.00", "2024-07-01"},
            {"Felix Nmecha", "VfL Wolfsburg", "Borussia Dortmund", "PERMANENT", "30000000.00", "2023-07-01"},
            {"Niklas Sule", "Bayern Munich", "Borussia Dortmund", "FREE", null, "2022-07-01"},
            {"Julian Ryerson", "1. FC Union Berlin", "Borussia Dortmund", "PERMANENT", "3000000.00", "2023-07-01"},
    };

    // -------------------------------------------------------------------------
    // Hand-crafted squads (first two clubs of each flagship league)
    // -------------------------------------------------------------------------

    private static final String[][] ARSENAL_CHELSEA = {
            {"Mikel|Arteta",
                    "David|Raya|1|GK", "Ben|White|4|RB", "Jurrien|Timber|12|LB", "William|Saliba|2|CB",
                    "Gabriel|Magalhaes|6|CB", "Jakub|Kiwior|15|CB", "Oleksandr|Zinchenko|35|CDM",
                    "Declan|Rice|41|CDM", "Thomas|Partey|5|CDM", "Martin|Odegaard|8|CAM", "Mikel|Merino|23|CM",
                    "Jorginho|Frello|20|CM", "Bukayo|Saka|7|RW", "Gabriel|Martinelli|11|LW",
                    "Leandro|Trossard|19|LW", "Kai|Havertz|29|ST", "Viktor|Gyokeres|14|ST",
                    "Gabriel|Jesus|9|CF", "Ethan|Nwaneri|33|CAM"},
            {"Enzo|Maresca",
                    "Robert|Sanchez|1|GK", "Reece|James|24|RB", "Marc|Cucurella|3|LB", "Levi|Colwill|26|CB",
                    "Wesley|Fofana|33|CB", "Moises|Caicedo|25|CDM", "Enzo|Fernandez|8|CM",
                    "Kiernan|Dewsbury-Hall|22|CM", "Cole|Palmer|20|RW", "Nicolas|Jackson|15|ST",
                    "Mykhailo|Mudryk|10|LW", "Filip|Jorgensen|13|GK", "Tosin|Adarabioyo|4|CB",
                    "Malo|Gusto|27|RB", "Romeo|Lavia|45|CDM", "Carney|Chukwuemeka|17|CAM",
                    "Christopher|Nkunku|18|CF", "Noni|Madueke|11|RW", "Axel|Disasi|2|CB",
                    "Trevoh|Chalobah|14|CB"}
    };

    private static final String[][] REAL_BARCELONA = {
            {"Carlo|Ancelotti",
                    "Thibaut|Courtois|1|GK", "Dani|Carvajal|2|RB", "Ferland|Mendy|23|LB", "Antonio|Rudiger|22|CB",
                    "Eder|Militao|3|CB", "David|Alaba|4|CB", "Aurelien|Tchouameni|18|CDM",
                    "Eduardo|Camavinga|12|CM", "Federico|Valverde|15|CM", "Jude|Bellingham|5|CAM",
                    "Vinicius|Junior|7|LW", "Rodrygo|Goes|11|RW", "Kylian|Mbappe|9|ST", "Endrick|Felipe|16|ST",
                    "Brahim|Diaz|21|CAM", "Andriy|Lunin|13|GK", "Lucas|Vazquez|17|RB", "Fran|Garcia|20|LB",
                    "Dani|Ceballos|19|CM"},
            {"Hansi|Flick",
                    "Marc-Andre|ter Stegen|1|GK", "Jules|Kounde|23|RB", "Alejandro|Balde|3|LB",
                    "Ronald|Araujo|4|CB", "Pau|Cubarsi|5|CB", "Inigo|Martinez|25|CB", "Frenkie|de Jong|21|CDM",
                    "Pedri|Gonzalez|8|CM", "Gavi|Paez|6|CM", "Raphinha|Belloli|11|RW", "Lamine|Yamal|19|RW",
                    "Robert|Lewandowski|9|ST", "Ferran|Torres|7|ST", "Dani|Olmo|20|CAM", "Marc|Casado|17|CDM",
                    "Inaki|Pena|13|GK", "Hector|Fort|16|RB", "Andreas|Christensen|15|CB", "Fermin|Lopez|22|CM"}
    };

    private static final String[][] PSG_MARSEILLE = {
            {"Luis|Enrique",
                    "Gianluigi|Donnarumma|1|GK", "Achraf|Hakimi|2|RB", "Nuno|Mendes|25|LB",
                    "Marquinhos|Correa|5|CB", "Willian|Pacho|51|CB", "Lucas|Beraldo|4|CB",
                    "Vitinha|Ferreira|17|CDM", "Joao|Neves|87|CM", "Warren|Zaire-Emery|33|CM",
                    "Ousmane|Dembele|10|RW", "Bradley|Barcola|29|LW", "Khvicha|Kvaratskhelia|7|LW",
                    "Goncalo|Ramos|9|ST", "Randal|Kolo Muani|23|ST", "Fabian|Ruiz|8|CAM", "Arnau|Tenas|36|GK",
                    "Lucas|Hernandez|21|LB", "Presnel|Kimpembe|3|CB", "Senny|Mayulu|20|CAM"},
            {"Roberto|De Zerbi",
                    "Geronimo|Rulli|1|GK", "Jonathan|Clauss|2|RB", "Ulisses|Garcia|22|LB",
                    "Leonardo|Balerdi|4|CB", "Derek|Cornelius|15|CB", "Facundo|Medina|24|CB",
                    "Geoffrey|Kondogbia|6|CDM", "Angel|Gomes|20|CM", "Amine|Harit|14|CAM",
                    "Mason|Greenwood|10|RW", "Luis|Henrique|99|LW", "Neal|Maupay|27|ST", "Ismaila|Sarr|7|RW",
                    "Robinio|Vaz|9|ST", "Bilal|Nadir|26|CM", "Rulani|Mombo|30|GK",
                    "Pierre-Emerick|Highsmith|12|RB", "Quentin|Merlin|17|LB", "Enzo|Molebe|33|CB"}
    };

    private static final String[][] JUVENTUS_INTER = {
            {"Igor|Tudor",
                    "Michele|Di Gregorio|29|GK", "Andrea|Cambiaso|27|RB", "Pierre|Kalulu|15|LB",
                    "Gleison|Bremer|3|CB", "Federico|Gatti|4|CB", "Lloyd|Kelly|32|CB", "Manuel|Locatelli|5|CDM",
                    "Khephren|Thuram|19|CM", "Teun|Koopmeiners|8|CAM", "Kenan|Yildiz|10|RW",
                    "Francisco|Conceicao|7|RW", "Nico|Gonzalez|11|LW", "Dusan|Vlahovic|9|ST",
                    "Jonathan|David|30|ST", "Weston|McKennie|16|CM", "Mattia|Perin|36|GK", "Juan|Cabal|6|CB",
                    "Vasilije|Adzic|40|CAM", "Timothy|Weah|22|RB"},
            {"Cristian|Chivu",
                    "Yann|Sommer|1|GK", "Denzel|Dumfries|2|RB", "Federico|Dimarco|32|LB",
                    "Alessandro|Bastoni|95|CB", "Francesco|Acerbi|15|CB", "Yann|Bisseck|31|CB",
                    "Hakan|Calhanoglu|20|CDM", "Nicolo|Barella|23|CM", "Henrikh|Mkhitaryan|22|CM",
                    "Marcus|Thuram|9|ST", "Lautaro|Martinez|10|ST", "Mehdi|Taremi|99|ST",
                    "Piotr|Zielinski|4|CAM", "Davide|Frattesi|16|CM", "Carlos|Augusto|30|LB",
                    "Josep|Martinez|12|GK", "Matteo|Darmian|36|RB", "Stefan|de Vrij|6|CB", "Petar|Sucic|19|CM"}
    };

    private static final String[][] BAYERN_DORTMUND = {
            {"Vincent|Kompany",
                    "Manuel|Neuer|1|GK", "Josip|Stanisic|4|RB", "Raphael|Guerreiro|22|LB", "Dayot|Upamecano|2|CB",
                    "Jonathan|Tah|30|CB", "Min-jae|Kim|3|CB", "Joshua|Kimmich|6|CDM",
                    "Aleksandar|Pavlovic|34|CM", "Jamal|Musiala|42|CAM", "Michael|Olise|17|RW",
                    "Leroy|Sane|10|LW", "Kingsley|Coman|11|LW", "Harry|Kane|9|ST", "Thomas|Muller|25|CF",
                    "Leon|Goretzka|8|CM", "Sven|Ulreich|26|GK", "Alphonso|Davies|19|LB", "Eric|Dier|5|CB",
                    "Konrad|Laimer|27|CDM"},
            {"Niko|Kovac",
                    "Gregor|Kobel|1|GK", "Julian|Ryerson|22|RB", "Ramy|Bensebaini|3|LB",
                    "Nico|Schlotterbeck|4|CB", "Waldemar|Anton|5|CB", "Niklas|Sule|25|CB", "Emre|Can|23|CDM",
                    "Felix|Nmecha|8|CM", "Pascal|Gross|20|CM", "Karim|Adeyemi|27|RW", "Jamie|Gittens|7|LW",
                    "Donyell|Malen|21|LW", "Serhou|Guirassy|9|ST", "Maximilian|Beier|42|ST",
                    "Marcel|Sabitzer|18|CAM", "Alexander|Meyer|35|GK", "Yan|Couto|2|RB", "Marcel|Lotka|33|CB",
                    "Daniel|Svensson|26|LB"}
    };

    // -------------------------------------------------------------------------
    // Flagship leagues
    // -------------------------------------------------------------------------

    static final List<FlagshipSpec> FLAGSHIP_LEAGUES = List.of(
            new FlagshipSpec("Premier League", "premier-league",
                    "Top tier of English club football.", SubscriptionPlan.ENTERPRISE,
                    "Premier League 2025/26", "EPL", "2025-08-15T15:00:00", "2026-05-24T17:00:00",
                    new String[]{
                            "Arsenal|ARS|Emirates Stadium", "Chelsea|CHE|Stamford Bridge",
                            "Manchester City|MCI|Etihad Stadium", "Liverpool|LIV|Anfield",
                            "Tottenham Hotspur|TOT|Tottenham Hotspur Stadium",
                            "Manchester United|MUN|Old Trafford", "Newcastle United|NEW|St. James' Park",
                            "Aston Villa|AVL|Villa Park", "West Ham United|WHU|London Stadium",
                            "Brighton & Hove Albion|BHA|Amex Stadium", "Brentford|BRE|Gtech Community Stadium",
                            "Fulham|FUL|Craven Cottage", "Crystal Palace|CRY|Selhurst Park",
                            "Wolverhampton Wanderers|WOL|Molineux Stadium", "Everton|EVE|Goodison Park",
                            "Nottingham Forest|NFO|City Ground", "Bournemouth|BOU|Vitality Stadium",
                            "Leicester City|LEI|King Power Stadium", "Ipswich Town|IPS|Portman Road",
                            "Southampton|SOU|St. Mary's Stadium"},
                    ARSENAL_CHELSEA),

            new FlagshipSpec("La Liga", "la-liga",
                    "Top tier of Spanish club football.", SubscriptionPlan.PROFESSIONAL,
                    "La Liga 2025/26", "LFP", "2025-08-16T19:00:00", "2026-05-24T19:00:00",
                    new String[]{
                            "Real Madrid|RMA|Santiago Bernabeu", "FC Barcelona|BAR|Spotify Camp Nou",
                            "Atletico Madrid|ATM|Civitas Metropolitano", "Athletic Bilbao|ATH|San Mames",
                            "Real Sociedad|RSO|Reale Arena", "Real Betis|BET|Estadio Benito Villamarin",
                            "Villarreal|VIL|Estadio de la Ceramica", "Valencia|VAL|Mestalla",
                            "Sevilla|SEV|Ramon Sanchez Pizjuan", "Girona|GIR|Estadi Montilivi",
                            "Celta Vigo|CEL|Balaidos", "Osasuna|OSA|El Sadar",
                            "Rayo Vallecano|RAY|Campo de Futbol de Vallecas", "Mallorca|MLL|Son Moix",
                            "Getafe|GET|Coliseum Alfonso Perez", "Alaves|ALA|Mendizorrotza",
                            "Espanyol|ESP|RCDE Stadium", "Leganes|LEG|Butarque",
                            "Las Palmas|LPA|Estadio de Gran Canaria", "Valladolid|VLL|Jose Zorrilla"},
                    REAL_BARCELONA),

            new FlagshipSpec("Ligue 1", "ligue-1",
                    "Top tier of French club football.", SubscriptionPlan.PROFESSIONAL,
                    "Ligue 1 2025/26", "L1", "2025-08-15T19:00:00", "2026-05-17T19:00:00",
                    new String[]{
                            "Paris Saint-Germain|PSG|Parc des Princes", "Olympique de Marseille|OM|Orange Velodrome",
                            "AS Monaco|ASM|Stade Louis II", "Olympique Lyonnais|OL|Groupama Stadium",
                            "LOSC Lille|LIL|Stade Pierre-Mauroy", "OGC Nice|NICE|Allianz Riviera",
                            "RC Lens|RCL|Stade Bollaert-Delelis", "Stade Rennais|REN|Roazhon Park",
                            "RC Strasbourg|STR|Stade de la Meinau", "Stade Brestois|BRE29|Stade Francis-Le Ble",
                            "Toulouse FC|TFC|Stadium de Toulouse", "Montpellier HSC|MHSC|Stade de la Mosson",
                            "FC Nantes|FCN|Stade de la Beaujoire", "Angers SCO|SCO|Stade Raymond-Kopa",
                            "Le Havre AC|HAC|Stade Oceane", "AJ Auxerre|AJA|Stade Abbe-Deschamps",
                            "Stade de Reims|SDR|Stade Auguste-Delaune", "Clermont Foot|CF63|Stade Gabriel-Montpied",
                            "FC Metz|FCM|Stade Saint-Symphorien", "Paris FC|PFC|Stade Charlety"},
                    PSG_MARSEILLE),

            new FlagshipSpec("Serie A", "serie-a",
                    "Top tier of Italian club football.", SubscriptionPlan.PROFESSIONAL,
                    "Serie A 2025/26", "LNPA", "2025-08-23T18:00:00", "2026-05-24T18:00:00",
                    new String[]{
                            "Juventus|JUV|Allianz Stadium", "Inter Milan|INT|San Siro", "AC Milan|MIL|San Siro",
                            "SSC Napoli|NAP|Stadio Diego Armando Maradona", "AS Roma|ROM|Stadio Olimpico",
                            "SS Lazio|LAZ|Stadio Olimpico", "Atalanta|ATA|Gewiss Stadium",
                            "Fiorentina|FIO|Stadio Artemio Franchi", "Bologna FC|BOL|Stadio Renato Dall'Ara",
                            "Torino FC|TOR|Stadio Olimpico Grande Torino", "Udinese|UDI|Bluenergy Stadium",
                            "Genoa CFC|GEN|Stadio Luigi Ferraris", "Hellas Verona|VER|Stadio Marcantonio Bentegodi",
                            "Cagliari Calcio|CAG|Unipol Domus", "Empoli FC|EMP|Stadio Carlo Castellani",
                            "Parma Calcio|PAR|Stadio Ennio Tardini", "Como 1907|COM|Stadio Giuseppe Sinigaglia",
                            "Venezia FC|VEN|Stadio Pier Luigi Penzo", "Lecce|LEC|Stadio Via del Mare",
                            "Monza|MON|U-Power Stadium"},
                    JUVENTUS_INTER),

            new FlagshipSpec("Bundesliga", "bundesliga",
                    "Top tier of German club football.", SubscriptionPlan.PROFESSIONAL,
                    "Bundesliga 2025/26", "BL", "2025-08-22T18:30:00", "2026-05-16T17:30:00",
                    new String[]{
                            "Bayern Munich|FCB|Allianz Arena", "Borussia Dortmund|BVB|Signal Iduna Park",
                            "RB Leipzig|RBL|Red Bull Arena", "Bayer Leverkusen|B04|BayArena",
                            "VfB Stuttgart|VFB|MHPArena", "Eintracht Frankfurt|SGE|Deutsche Bank Park",
                            "SC Freiburg|SCF|Europa-Park Stadion", "VfL Wolfsburg|WOB|Volkswagen Arena",
                            "Borussia Monchengladbach|BMG|Borussia-Park", "Werder Bremen|SVW|Weserstadion",
                            "1. FC Union Berlin|FCU|Stadion An der Alten Forsterei", "1. FSV Mainz 05|M05|Mewa Arena",
                            "TSG Hoffenheim|TSG|PreZero Arena", "FC Augsburg|FCA|WWK Arena",
                            "1. FC Heidenheim|FCH|Voith-Arena", "FC St. Pauli|STP|Millerntor-Stadion",
                            "Holstein Kiel|KSV|Holstein-Stadion", "VfL Bochum|BOC|Vonovia Ruhrstadion",
                            "Hamburger SV|HSV|Volksparkstadion", "Fortuna Dusseldorf|F95|Merkur Spiel-Arena"},
                    BAYERN_DORTMUND)
    );
}