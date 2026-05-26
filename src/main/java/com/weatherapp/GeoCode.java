package com.weatherapp;

import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class GeoCode {
  private final HttpClient client = HttpClient.newHttpClient();

  Coordinates getGeoLocation(String city) throws IOException, InterruptedException {
    String cityUrl = buildUrl(city);
    String cityJson = fetchWeatherJson(cityUrl);

    return parseCityCoordinates(cityJson);
  }

  private static String buildUrl(String city) {
    return "https://geocoding-api.open-meteo.com/v1/search?name=%s&count=1&language=en&format=json"
      .formatted(city);
  }

  private String fetchWeatherJson(String cityUrl) throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
      .uri(URI.create(cityUrl))
      .build();

    return client.send(request, HttpResponse.BodyHandlers.ofString()).body();
  }

  private Coordinates parseCityCoordinates(String json) {
    JSONObject jsonObject = new JSONObject(json);

    if (!jsonObject.has("results")) {
      return null;
    }

    var results = jsonObject.getJSONArray("results");

    if (results.isEmpty()) {
      return null;
    }

    JSONObject coordinates = results.getJSONObject(0);

    double latitude = coordinates.getDouble("latitude");
    double longitude = coordinates.getDouble("longitude");

    return new Coordinates(latitude, longitude);
  }
}