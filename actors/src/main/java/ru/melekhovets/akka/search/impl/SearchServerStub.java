package ru.melekhovets.akka.search.impl;

import org.json.JSONArray;
import org.json.JSONObject;
import ru.melekhovets.akka.search.SearchResult;

public class SearchServerStub {
  private static volatile SearchServerStub instance;

  private long sleepTime;

  public SearchServerStub() {
    this.sleepTime = 0;
  }

  public static SearchServerStub getInstance() {
    SearchServerStub localInstance = instance;
    if (localInstance == null) {
      synchronized (SearchServerStub.class) {
        localInstance = instance;
        if (localInstance == null) {
          instance = localInstance = new SearchServerStub();
        }
      }
    }
    return localInstance;
  }

  public JSONArray search(String query) {
    if (sleepTime > 0) {
      try {
        Thread.sleep(sleepTime);
      } catch (InterruptedException ignored) {
      }
    }

    JSONArray results = new JSONArray();
    for (int i = 0; i < 4; i++) {
      SearchResult result = new SearchResult("title" + i, "body" + i);
      results.put(new JSONObject(result));
    }

    return results;
  }

  public void setSleepTime(long sleepTime) {
    this.sleepTime = sleepTime;
  }
}
