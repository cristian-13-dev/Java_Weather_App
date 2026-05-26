package com.weatherapp;

public record WeatherDetails(
  double temperature,
  int humidity,
  int weatherCode,
  double apparentTemperature,
  int precipitationProbability,
  double windSpeed,
  double windGusts,
  double uvIndex,
  int europeanAqi

) {}