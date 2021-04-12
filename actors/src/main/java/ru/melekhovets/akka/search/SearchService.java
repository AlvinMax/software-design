package ru.melekhovets.akka.search;

import org.json.JSONArray;

public interface SearchService {
  JSONArray search(String query);
}