package ru.melekhovets.akka.model.actor;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import lombok.*;
import org.json.JSONArray;
import ru.melekhovets.akka.search.SearchEngine;
import ru.melekhovets.akka.search.SearchService;
import ru.melekhovets.akka.search.impl.BingSearchService;
import ru.melekhovets.akka.search.impl.GoogleSearchService;
import ru.melekhovets.akka.search.impl.YandexSearchService;

public class ChildActor extends AbstractActor {
  @Value
  static class ChildRequest {
    SearchEngine searchEngine;
    String query;
  }

  @AllArgsConstructor
  @Data
  public static class ChildResponse {
    SearchEngine searchEngine;
    String query;
    JSONArray results;
  }

  @Override
  public Receive createReceive() {
    return new ReceiveBuilder()
        .match(ChildActor.ChildRequest.class, this::onChildRequest)
        .build();
  }

  private void onChildRequest(ChildRequest request) {
    SearchEngine searchEngine = request.getSearchEngine();
    String query = request.getQuery();
    SearchService searchService;
    switch (searchEngine) {
      case BING:
        searchService = new BingSearchService();
        break;
      case GOOGLE:
        searchService = new GoogleSearchService();
        break;
      case YANDEX:
        searchService = new YandexSearchService();
        break;
      default:
        throw new IllegalArgumentException("Unexpected search engine: " + searchEngine);
    }
    JSONArray results = searchService.search(query);
    ChildResponse response = new ChildResponse(searchEngine, query, results);
    sender().tell(response, self());
    getContext().stop(self());
  }
}
