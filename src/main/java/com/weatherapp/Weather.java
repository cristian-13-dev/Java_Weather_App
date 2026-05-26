package com.weatherapp;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;
import java.util.concurrent.CompletableFuture;

public class Weather {
  String city;

  Weather(String city) {
    this.city = city;
  }

  private final HttpClient weatherClient = HttpClient.newHttpClient();

  void getWeatherDetails() throws IOException, InterruptedException {
    LocalTime localTime = LocalTime.now();

    GeoCode geoCode = new GeoCode();

    Coordinates coordinates = geoCode.getGeoLocation(city);

    if (coordinates == null) {
      System.out.println("\u001B[31mCity not found. Please check the name.\u001B[0m");
      return;
    }

    String weatherUrl = buildWeatherUrl(coordinates);
    String airQualityUrl = buildAirQualityUrl(coordinates);

    CompletableFuture<String> weatherFuture = fetchJsonAsync(weatherUrl);
    CompletableFuture<String> airQualityFuture = fetchJsonAsync(airQualityUrl);

    WeatherDetails details = weatherFuture
      .thenCombine(airQualityFuture, (weatherResponseBody, airQualityResponseBody) -> {
        JSONObject weatherJson = new JSONObject(weatherResponseBody);
        JSONObject airQualityJson = new JSONObject(airQualityResponseBody);

        return parseWeatherDetails(weatherJson, airQualityJson);
      })
      .join();

    printWeather(localTime, details);
  }

  private String buildWeatherUrl(Coordinates coordinates) {
    return "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m,relative_humidity_2m,weather_code,apparent_temperature,precipitation_probability,wind_speed_10m,wind_gusts_10m,uv_index&timezone=auto"
      .formatted(coordinates.latitude, coordinates.longitude);
  }

  private String buildAirQualityUrl(Coordinates coordinates) {
    return "https://air-quality-api.open-meteo.com/v1/air-quality?latitude=%f&longitude=%f&hourly=european_aqi&timezone=auto"
      .formatted(coordinates.latitude, coordinates.longitude);
  }

  private static String getEuropeanAqiDescription(int aqi) {
    if (aqi <= 20) return Text.printGreen("%d (Good)".formatted(aqi));
    if (aqi <= 40) return Text.printYellow("%d (Fair)".formatted(aqi));
    if (aqi <= 60) return Text.printOrange("%d (Moderate)".formatted(aqi));
    if (aqi <= 80) return Text.printRed("%d (Poor)".formatted(aqi));
    if (aqi <= 100) return Text.printPurple("%d (Very poor)".formatted(aqi));
    return Text.printBlack("%d (Extremely poor)".formatted(aqi));
  }

  private WeatherDetails parseWeatherDetails(JSONObject weatherJson, JSONObject airQualityJson) {
    JSONObject hourly = weatherJson.getJSONObject("hourly");
    JSONObject airQualityHourly = airQualityJson.getJSONObject("hourly");
    int currentHour = LocalTime.now().getHour();

    return new WeatherDetails(
      hourly.getJSONArray("temperature_2m").getDouble(currentHour),
      hourly.getJSONArray("relative_humidity_2m").getInt(currentHour),
      hourly.getJSONArray("weather_code").getInt(currentHour),
      hourly.getJSONArray("apparent_temperature").getDouble(currentHour),
      hourly.getJSONArray("precipitation_probability").getInt(currentHour),
      hourly.getJSONArray("wind_speed_10m").getDouble(currentHour),
      hourly.getJSONArray("wind_gusts_10m").getDouble(currentHour),
      hourly.getJSONArray("uv_index").getDouble(currentHour),
      airQualityHourly.getJSONArray("european_aqi").getInt(currentHour)
    );
  }

  private CompletableFuture<String> fetchJsonAsync(String url) {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .build();

    return weatherClient.sendAsync(request, HttpResponse.BodyHandlers.ofString()).thenApply(HttpResponse::body);
  }

  private void printWeather(LocalTime localTime, WeatherDetails details) {
    final String PURPLE = "\u001B[35m";
    final String RESET = "\u001B[0m";
    final String BOLD = "\u001B[1m";

    int hour = localTime.getHour();
    int minutes = localTime.getMinute();
    int seconds = localTime.getSecond();

    System.out.println("==============================");
    System.out.printf(BOLD + PURPLE + "Time:" + RESET + " %02d:%02d:%02d%n", hour, minutes, seconds);
    System.out.println(BOLD + PURPLE + "Weather:" + RESET + " " + getWeatherDescription(details.weatherCode()));
    System.out.println(BOLD + PURPLE + "Temperature:" + RESET + " " + details.temperature() + "°C");
    System.out.println(BOLD + PURPLE + "Feels like:" + RESET + " " + details.apparentTemperature() + "°C");
    System.out.println(BOLD + PURPLE + "Humidity:" + RESET + " " + details.humidity() + "%");
    System.out.println(BOLD + PURPLE + "Rain chance:" + RESET + " " + details.precipitationProbability() + "%");
    System.out.println(BOLD + PURPLE + "Wind:" + RESET + " " + details.windSpeed() + " km/h");
    System.out.println(BOLD + PURPLE + "Gusts:" + RESET + " " + details.windGusts() + " km/h");
    System.out.println(BOLD + PURPLE + "UV index:" + RESET + " " + details.uvIndex());
    System.out.println(BOLD + PURPLE + "Air quality:" + RESET + " " + getEuropeanAqiDescription(details.europeanAqi()));
    System.out.println("==============================");
  }

  private static String getWeatherDescription(int weatherCode) {
    return switch (weatherCode) {
      case 0 -> "Clear sky ☀️";
      case 1 -> "Mainly clear 🌤️";
      case 2 -> "Partly cloudy ⛅";
      case 3 -> "Overcast ☁️";

      case 45 -> "Fog 🌫️";
      case 48 -> "Depositing rime fog 🌫️";

      case 51 -> "Light drizzle 🌦️";
      case 53 -> "Moderate drizzle 🌦️";
      case 55 -> "Dense drizzle 🌧️";

      case 61 -> "Slight rain 🌧️";
      case 63 -> "Moderate rain 🌧️";
      case 65 -> "Heavy rain 🌧️";

      case 71 -> "Slight snow 🌨️";
      case 73 -> "Moderate snow 🌨️";
      case 75 -> "Heavy snow ❄️";

      case 80 -> "Slight rain showers 🌦️";
      case 81 -> "Moderate rain showers 🌧️";
      case 82 -> "Violent rain showers ⛈️";

      case 95 -> "Thunderstorm ⛈️";
      case 96 -> "Thunderstorm with slight hail ⛈️";
      case 99 -> "Thunderstorm with heavy hail 🌩️";

      default -> "Unknown weather code ❓";
    };
  }
}