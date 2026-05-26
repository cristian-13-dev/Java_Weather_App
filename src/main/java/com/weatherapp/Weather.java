package com.weatherapp;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalTime;

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

    String weatherUrl = buildUrl(coordinates);
    String responseBody = fetchWeatherJson(weatherUrl);

    JSONObject weatherJson = new JSONObject(responseBody);

    WeatherDetails details = parseWeatherDetails(weatherJson);
    printWeather(localTime, details);
  }

  private static String buildUrl(Coordinates coordinates) {
    return "https://api.open-meteo.com/v1/forecast?latitude=%f&longitude=%f&hourly=temperature_2m,relative_humidity_2m,weather_code"
      .formatted(coordinates.latitude, coordinates.longitude);
  }

  private String fetchWeatherJson(String weatherUrl) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(weatherUrl))
      .build();

    return weatherClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
  }

  private WeatherDetails parseWeatherDetails(JSONObject weatherJson) {
    JSONObject hourly = weatherJson.getJSONObject("hourly");

    return new WeatherDetails(
      hourly.getJSONArray("temperature_2m").getDouble(0),
      hourly.getJSONArray("relative_humidity_2m").getInt(0),
      hourly.getJSONArray("weather_code").getInt(0)
    );
  }

  private void printWeather(LocalTime localTime, WeatherDetails details) {
    int hour = localTime.getHour();
    int minutes = localTime.getMinute();
    int seconds = localTime.getSecond();


    System.out.println("==============================");
    System.out.printf("Time: %02d:%02d:%02d%n", hour, minutes, seconds);
    System.out.println("Temperature(°C): " + details.temperature());
    System.out.println("Humidity: " + details.humidity() + "%");
    System.out.println("Weather code: " + getWeatherDescription(details.weatherCode()));
    System.out.println("==============================");
  }

  private static String getWeatherDescription(int weatherCode) {
    return switch (weatherCode) {
      case 0 -> "Clear sky";
      case 1 -> "Mainly clear";
      case 2 -> "Partly cloudy";
      case 3 -> "Overcast";

      case 45 -> "Fog";
      case 48 -> "Depositing rime fog";

      case 51 -> "Light drizzle";
      case 53 -> "Moderate drizzle";
      case 55 -> "Dense drizzle";

      case 61 -> "Slight rain";
      case 63 -> "Moderate rain";
      case 65 -> "Heavy rain";

      case 71 -> "Slight snow";
      case 73 -> "Moderate snow";
      case 75 -> "Heavy snow";

      case 80 -> "Slight rain showers";
      case 81 -> "Moderate rain showers";
      case 82 -> "Violent rain showers";

      case 95 -> "Thunderstorm";
      case 96 -> "Thunderstorm with slight hail";
      case 99 -> "Thunderstorm with heavy hail";

      default -> "Unknown weather code";
    };
  }
}