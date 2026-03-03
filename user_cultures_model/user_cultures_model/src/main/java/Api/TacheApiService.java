package Api;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * Module Tâche : 2 API externes
 * 1) Open-Meteo : météo pour une date (gratuit, sans clé)
 * 2) Quotable : citation aléatoire pour motivation
 */
public class TacheApiService {

    private static final HttpClient httpClient = HttpClient.newBuilder().build();
    // Tunis par défaut (latitude, longitude)
    private static final double LAT = 36.8;
    private static final double LON = 10.2;

    /** API 1 : Météo pour une date donnée (Open-Meteo) */
    public static CompletableFuture<WeatherResult> getWeatherForDate(LocalDate date) {
        String start = date.toString();
        String url = "https://api.open-meteo.com/v1/forecast?latitude=" + LAT + "&longitude=" + LON
                + "&daily=weathercode,temperature_2m_max,temperature_2m_min&start_date=" + start + "&end_date=" + start;
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    if (res.statusCode() != 200)
                        return new WeatherResult(null, null, null);
                    String body = res.body();
                    try {
                        String tempMax = extractJsonArrayFirst(body, "temperature_2m_max");
                        String tempMin = extractJsonArrayFirst(body, "temperature_2m_min");
                        String code = extractJsonArrayFirst(body, "weathercode");
                        return new WeatherResult(tempMax, tempMin, code);
                    } catch (Exception e) {
                        return new WeatherResult(null, null, null);
                    }
                })
                .exceptionally(ex -> new WeatherResult(null, null, null));
    }

    private static String extractJsonArrayFirst(String json, String key) {
        int i = json.indexOf("\"" + key + "\":[");
        if (i == -1) return null;
        i = json.indexOf("[", i) + 1;
        int j = json.indexOf("]", i);
        if (j == -1) return null;
        String arr = json.substring(i, j).trim();
        if (arr.contains(",")) arr = arr.substring(0, arr.indexOf(",")).trim();
        return arr;
    }

    /** API 2 : Citation aléatoire (Quotable) */
    public static CompletableFuture<QuoteResult> getRandomQuote() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.quotable.io/random?maxLength=120"))
                .GET()
                .build();
        return httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(res -> {
                    if (res.statusCode() != 200)
                        return new QuoteResult(null, null);
                    String body = res.body();
                    String content = extractJsonString(body, "content");
                    String author = extractJsonString(body, "author");
                    return new QuoteResult(content, author);
                })
                .exceptionally(ex -> new QuoteResult(null, null));
    }

    private static String extractJsonString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int i = json.indexOf(search);
        if (i == -1) return null;
        i += search.length();
        int j = i;
        while (j < json.length() && json.charAt(j) != '"') {
            if (json.charAt(j) == '\\') j++;
            j++;
        }
        return json.substring(i, j).replace("\\\"", "\"");
    }

    public static class WeatherResult {
        public final String tempMax;
        public final String tempMin;
        public final String weatherCode;
        public WeatherResult(String tempMax, String tempMin, String weatherCode) {
            this.tempMax = tempMax;
            this.tempMin = tempMin;
            this.weatherCode = weatherCode;
        }
        public String getSummary() {
            if (tempMax != null && tempMin != null)
                return tempMin + "°C / " + tempMax + "°C";
            return "—";
        }
    }

    public static class QuoteResult {
        public final String content;
        public final String author;
        public QuoteResult(String content, String author) {
            this.content = content;
            this.author = author;
        }
    }
}
