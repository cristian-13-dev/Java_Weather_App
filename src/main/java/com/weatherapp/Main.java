package com.weatherapp;

import java.io.IOException;
import java.util.Scanner;

public class Main {
  String city;

  void main() {
    Scanner scanner = new Scanner(System.in);

    do {
      System.out.print("Enter the city name: ");
      city = scanner.nextLine();
    } while (city.isBlank());

    scanner.close();

    try {
      Weather weather = new Weather(city);
      weather.getWeatherDetails();
    } catch (IOException | InterruptedException e) {
      System.out.println("Could not fetch weather data. Please try again later.");
    }
  }
}