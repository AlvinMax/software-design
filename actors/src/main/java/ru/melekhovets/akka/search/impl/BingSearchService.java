package ru.melekhovets.akka.search.impl;

import org.json.JSONArray;
import ru.melekhovets.akka.search.SearchService;

public class BingSearchService implements SearchService {
  private final SearchServerStub server = SearchServerStub.getInstance();

  @Override
  public JSONArray search(String query) {
    return server.search(query);
  }
}
