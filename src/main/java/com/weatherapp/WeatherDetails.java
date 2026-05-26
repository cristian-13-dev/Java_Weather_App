package com.weatherapp;

public record WeatherDetails(
  double temperature,
  int humidity,
  int weatherCode
) {
}