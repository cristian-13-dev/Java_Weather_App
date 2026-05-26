package com.weatherapp;

public class Text {
  private static final String RESET = "\u001B[0m";

  public static String printGreen(String text) {
    return "\u001B[32m" + text + RESET;
  }

  public static String printYellow(String text) {
    return "\u001B[33m" + text + RESET;
  }

  public static String printOrange(String text) {
    return "\u001B[38;5;208m" + text + RESET;
  }

  public static String printRed(String text) {
    return "\u001B[31m" + text + RESET;
  }

  public static String printPurple(String text) {
    return "\u001B[35m" + text + RESET;
  }

  public static String printGray(String text) {
    return "\u001B[90m" + text + RESET;
  }
}