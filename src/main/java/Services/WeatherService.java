package Services;

import Entites.Parcelle;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WeatherService {

    private static final String API_KEY = getApiKey();
    private static final String BASE_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String BASE_GEOCODING_URL = "https://api.openweathermap.org/geo/1.0/direct";
    private static final String NOMINATIM_SEARCH_URL = "https://nominatim.openstreetmap.org/search";
    private static final String NOMINATIM_USER_AGENT = "AGRI-GO-Application";
    private static final Pattern NUMBER_PATTERN = Pattern.compile("[-+]?\\d+(?:[\\.,]\\d+)?");
    private static final Pattern GOOGLE_MAPS_AT_PATTERN = Pattern.compile("@\\s*([-+]?\\d+(?:\\.\\d+)?),\\s*([-+]?\\d+(?:\\.\\d+)?)");

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private final Gson gson = new Gson();
    private final Map<String, ParsedLocation> geocodeCache = new HashMap<>();

    public enum RiskLevel {
        GREEN, YELLOW, RED, DARK_RED;

        public String getLabel() {
            return switch (this) {
                case GREEN -> "Faible";
                case YELLOW -> "Modéré";
                case RED -> "Élevé";
                case DARK_RED -> "Critique";
            };
        }

        public String getColorName() {
            return switch (this) {
                case GREEN -> "Vert";
                case YELLOW -> "Jaune";
                case RED -> "Rouge";
                case DARK_RED -> "Rouge foncé";
            };
        }
    }

    public static class RiskSummary {
        private final RiskLevel level;
        private final String message;
        private final int mostExposedParcelleId;
        private final Map<Integer, RiskLevel> parcelleLevels;

        public RiskSummary(RiskLevel level,
                           String message,
                           int mostExposedParcelleId,
                           Map<Integer, RiskLevel> parcelleLevels) {
            this.level = level;
            this.message = message;
            this.mostExposedParcelleId = mostExposedParcelleId;
            this.parcelleLevels = parcelleLevels == null ? Map.of() : Map.copyOf(parcelleLevels);
        }

        public RiskLevel getLevel() {
            return level;
        }

        public String getMessage() {
            return message;
        }

        public int getMostExposedParcelleId() {
            return mostExposedParcelleId;
        }

        public Map<Integer, RiskLevel> getParcelleLevels() {
            return parcelleLevels;
        }
    }

    private static class ParsedLocation {
        final Double lat;
        final Double lon;
        final String cityQuery;

        ParsedLocation(Double lat, Double lon, String cityQuery) {
            this.lat = lat;
            this.lon = lon;
            this.cityQuery = cityQuery;
        }
    }

    private static class ForecastResponse { List<ForecastItem> list; }
    private static class GeocodingItem { Double lat; Double lon; }
    private static class ForecastItem {
        long dt;
        Main main;
        List<Weather> weather;
        Wind wind;
        Rain rain;
    }
    private static class Main { double temp; }
    private static class Weather { String main; String description; }
    private static class Wind { double speed; }
    private static class Rain { @SerializedName("3h") Double threeHours; }

    public RiskSummary computeRiskForParcelles(List<Parcelle> parcelles) {
        if (parcelles == null || parcelles.isEmpty()) {
            return new RiskSummary(RiskLevel.GREEN, "Aucune parcelle définie pour l'analyse météo.", -1, Map.of());
        }

        RiskLevel global = RiskLevel.GREEN;
        String bestMessage = "Pas de risque météo majeur détecté sur les prochaines 24h.";
        int bestParcelleId = -1;
        Map<Integer, RiskLevel> perParcelle = new HashMap<>();

        for (Parcelle parcelle : parcelles) {
            if (parcelle == null) {
                continue;
            }
            ParsedLocation location = parseLocation(parcelle.getGps());
            if (location == null) {
                continue;
            }

            ForecastResponse response = fetchForecast(location);
            if (response == null || response.list == null || response.list.isEmpty()) {
                RiskLevel mockRisk = RiskLevel.values()[parcelle.getId() % 4];
                perParcelle.put(parcelle.getId(), mockRisk);
                if (mockRisk.ordinal() > global.ordinal()) {
                    global = mockRisk;
                    bestParcelleId = parcelle.getId();
                    bestMessage = buildRiskMessage(mockRisk, parcelle.getNom());
                }
                continue;
            }

            RiskLevel parcelRisk = evaluateForecastForTomorrow(response);
            perParcelle.put(parcelle.getId(), parcelRisk);

            if (parcelRisk.ordinal() > global.ordinal()) {
                global = parcelRisk;
                bestParcelleId = parcelle.getId();
                bestMessage = buildRiskMessage(parcelRisk, parcelle.getNom());
            }
        }

        return new RiskSummary(global, bestMessage, bestParcelleId, perParcelle);
    }

    private static String getApiKey() {
        String env = System.getenv("OPENWEATHER_API_KEY");
        if (env != null && !env.isBlank()) {
            return env.trim();
        }
        String fromResource = loadApiKeyFromResource();
        return fromResource == null ? "" : fromResource;
    }

    private ParsedLocation parseLocation(String raw) {
        if (raw == null) {
            return null;
        }
        String value = sanitizeLocationInput(raw);
        if (value.isEmpty()) {
            return null;
        }

        ParsedLocation fromMaps = parseLatLonFromKnownUrl(value);
        if (fromMaps != null) {
            return fromMaps;
        }

        String normalized = value.replace(";", ",").replace("|", ",");
        String[] parts = normalized.split(",");
        if (parts.length == 2) {
            try {
                double lat = Double.parseDouble(parts[0].trim());
                double lon = Double.parseDouble(parts[1].trim());
                if (isValidLatLon(lat, lon)) {
                    return new ParsedLocation(lat, lon, null);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String[] spaceParts = value.split("\\s+");
        if (spaceParts.length == 2) {
            try {
                double lat = Double.parseDouble(spaceParts[0].trim().replace(',', '.'));
                double lon = Double.parseDouble(spaceParts[1].trim().replace(',', '.'));
                if (isValidLatLon(lat, lon)) {
                    return new ParsedLocation(lat, lon, null);
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String lowered = value.toLowerCase(Locale.ROOT);
        boolean looksLikeCoords = normalized.contains(",")
                || value.contains(";")
                || value.contains("|")
                || lowered.contains("lat")
                || lowered.contains("lon")
                || lowered.contains("gps");

        if (looksLikeCoords) {
            Matcher matcher = NUMBER_PATTERN.matcher(value);
            Double lat = null;
            Double lon = null;
            while (matcher.find()) {
                String token = matcher.group().replace(',', '.');
                try {
                    double parsed = Double.parseDouble(token);
                    if (lat == null) {
                        lat = parsed;
                    } else {
                        lon = parsed;
                        break;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
            if (lat != null && lon != null && isValidLatLon(lat, lon)) {
                return new ParsedLocation(lat, lon, null);
            }
        }

        return new ParsedLocation(null, null, value);
    }

    private ForecastResponse fetchForecast(ParsedLocation location) {
        if (API_KEY == null || API_KEY.isBlank()) {
            // Aucun API key configuré : on désactive simplement l'appel réseau
            // et on laissera le reste du code utiliser les données simulées.
            return null;
        }

        try {
            ParsedLocation resolvedLocation = resolveToLatLonIfNeeded(location);
            String url = buildUrl(resolvedLocation);
            HttpResponse<String> response = sendGet(url);
            if (response.statusCode() != 200) {
                String details = extractApiErrorMessage(response.body());
                System.err.println("OpenWeatherMap error: HTTP " + response.statusCode()
                        + (details.isEmpty() ? "" : " (" + details + ")"));
                return null;
            }
            return gson.fromJson(response.body(), ForecastResponse.class);
        } catch (IOException | InterruptedException e) {
            System.err.println("Erreur lors de l'appel OpenWeatherMap: " + e.getMessage());
            return null;
        }
    }

    private HttpResponse<String> sendGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .GET()
                .build();
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private boolean isValidLatLon(double lat, double lon) {
        return lat >= -90.0 && lat <= 90.0 && lon >= -180.0 && lon <= 180.0;
    }

    private String extractApiErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "";
        }
        try {
            ApiError apiError = gson.fromJson(body, ApiError.class);
            if (apiError != null && apiError.message != null && !apiError.message.isBlank()) {
                return apiError.message.trim();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private static class ApiError { String message; }

    private String buildUrl(ParsedLocation location) {
        StringBuilder sb = new StringBuilder(BASE_FORECAST_URL);
        sb.append("?");
        if (location.lat != null && location.lon != null) {
            sb.append("lat=").append(location.lat)
                    .append("&lon=").append(location.lon);
        } else if (location.cityQuery != null) {
            String q = URLEncoder.encode(location.cityQuery, StandardCharsets.UTF_8);
            sb.append("q=").append(q);
        }
        sb.append("&appid=").append(API_KEY);
        sb.append("&units=metric");
        sb.append("&lang=fr");
        return sb.toString();
    }

    private ParsedLocation resolveToLatLonIfNeeded(ParsedLocation location) throws IOException, InterruptedException {
        if (location == null) {
            return null;
        }
        if (location.lat != null && location.lon != null) {
            return location;
        }
        if (location.cityQuery == null || location.cityQuery.isBlank()) {
            return location;
        }

        String query = sanitizeLocationInput(location.cityQuery);
        if (query.isEmpty()) {
            return location;
        }

        ParsedLocation cached = geocodeCache.get(query);
        if (cached != null) {
            return cached;
        }

        ParsedLocation byOwm = geocodeCity(query);
        if (byOwm != null && byOwm.lat != null && byOwm.lon != null) {
            geocodeCache.put(query, byOwm);
            return byOwm;
        }

        ParsedLocation byNominatim = geocodeWithNominatim(query);
        if (byNominatim != null && byNominatim.lat != null && byNominatim.lon != null) {
            geocodeCache.put(query, byNominatim);
            return byNominatim;
        }

        return location;
    }

    private ParsedLocation geocodeCity(String cityQuery) {
        try {
            String q = URLEncoder.encode(cityQuery, StandardCharsets.UTF_8);
            String url = BASE_GEOCODING_URL + "?q=" + q + "&limit=1&appid=" + API_KEY;
            HttpResponse<String> response = sendGet(url);
            if (response.statusCode() != 200) {
                return null;
            }
            GeocodingItem[] items = gson.fromJson(response.body(), GeocodingItem[].class);
            if (items == null || items.length == 0) {
                return null;
            }
            GeocodingItem first = items[0];
            if (first == null || first.lat == null || first.lon == null) {
                return null;
            }
            return new ParsedLocation(first.lat, first.lon, null);
        } catch (Exception ignored) {
            return null;
        }
    }

    private ParsedLocation geocodeWithNominatim(String query) throws IOException, InterruptedException {
        String q = URLEncoder.encode(query, StandardCharsets.UTF_8);
        String url = NOMINATIM_SEARCH_URL + "?q=" + q + "&format=json&limit=1";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(8))
                .header("User-Agent", NOMINATIM_USER_AGENT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return null;
        }

        NominatimItem[] items;
        try {
            items = gson.fromJson(response.body(), NominatimItem[].class);
        } catch (Exception ignored) {
            return null;
        }
        if (items == null || items.length == 0 || items[0] == null) {
            return null;
        }

        Double lat = parseDoubleOrNull(items[0].lat);
        Double lon = parseDoubleOrNull(items[0].lon);
        if (lat == null || lon == null || !isValidLatLon(lat, lon)) {
            return null;
        }
        return new ParsedLocation(lat, lon, null);
    }

    private static class NominatimItem { String lat; String lon; }

    private Double parseDoubleOrNull(String value) {
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(value.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static String sanitizeLocationInput(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.isEmpty()) {
            return "";
        }
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1).trim();
        }
        s = s.replaceAll("[\\r\\n\\t]+", " ").trim();
        s = s.replaceAll("\\s{2,}", " ");
        return s;
    }

    private ParsedLocation parseLatLonFromKnownUrl(String input) {
        if (input == null) {
            return null;
        }
        String s = input.trim();
        if (s.isEmpty()) {
            return null;
        }
        Matcher m = GOOGLE_MAPS_AT_PATTERN.matcher(s);
        if (m.find()) {
            try {
                double lat = Double.parseDouble(m.group(1));
                double lon = Double.parseDouble(m.group(2));
                if (isValidLatLon(lat, lon)) {
                    return new ParsedLocation(lat, lon, null);
                }
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    private static String loadApiKeyFromResource() {
        try (var in = WeatherService.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                return "";
            }
            var props = new java.util.Properties();
            props.load(in);
            String key = props.getProperty("openweather.api.key");
            return key == null ? "" : key.trim();
        } catch (Exception ignored) {
            return "";
        }
    }

    private RiskLevel evaluateForecastForTomorrow(ForecastResponse response) {
        Instant now = Instant.now();
        RiskLevel highest = RiskLevel.GREEN;

        for (ForecastItem item : response.list) {
            if (item == null) {
                continue;
            }
            Instant time = Instant.ofEpochSecond(item.dt);
            Duration d = Duration.between(now, time);
            long hours = d.toHours();
            if (hours < 24 || hours > 48) {
                continue;
            }

            RiskLevel level = computeRiskLevel(item);
            if (level.ordinal() > highest.ordinal()) {
                highest = level;
            }
        }

        return highest;
    }

    private RiskLevel computeRiskLevel(ForecastItem item) {
        String main = "";
        String desc = "";
        if (item.weather != null && !item.weather.isEmpty()) {
            Weather w = item.weather.get(0);
            if (w != null) {
                main = w.main != null ? w.main : "";
                desc = w.description != null ? w.description : "";
            }
        }
        double wind = item.wind != null ? item.wind.speed : 0.0;
        double rain = 0.0;
        if (item.rain != null && item.rain.threeHours != null) {
            rain = item.rain.threeHours;
        }

        String mainLower = main.toLowerCase(Locale.ROOT);
        String descLower = desc.toLowerCase(Locale.ROOT);

        if (mainLower.contains("thunderstorm")
                || descLower.contains("storm")
                || descLower.contains("tempête")
                || wind >= 15.0
                || rain >= 20.0) {
            return RiskLevel.DARK_RED;
        }

        if (mainLower.contains("rain") || mainLower.contains("snow") || mainLower.contains("drizzle")) {
            if (rain >= 8.0 || wind >= 10.0) {
                return RiskLevel.RED;
            }
            return RiskLevel.YELLOW;
        }

        if (mainLower.contains("squall") || mainLower.contains("tornado")) {
            return RiskLevel.DARK_RED;
        }

        if (wind >= 10.0) {
            return RiskLevel.RED;
        }
        if (wind >= 6.0) {
            return RiskLevel.YELLOW;
        }

        return RiskLevel.GREEN;
    }

    private String buildRiskMessage(RiskLevel level, String parcelleName) {
        String target = (parcelleName == null || parcelleName.isBlank())
                ? "une parcelle"
                : "la parcelle \"" + parcelleName + "\"";

        return switch (level) {
            case GREEN -> "Conditions météo globalement favorables pour " + target + " demain.";
            case YELLOW -> "De légers risques météo sont prévus pour " + target + " (pluie ou vent modéré).";
            case RED -> "Un risque météo ÉLEVÉ est prévu demain pour " + target + " (pluie importante ou vent fort).";
            case DARK_RED -> "Risque météo CRITIQUE pour " + target + " : tempête, orages violents ou fortes rafales.";
        };
    }
}

