package ru.melekhovets.akka.model.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.ReceiveTimeout;
import akka.japi.pf.ReceiveBuilder;
import akka.routing.RoundRobinPool;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Value;
import ru.melekhovets.akka.search.SearchEngine;
import scala.concurrent.duration.Duration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class MasterActor extends AbstractActor {
  private final ActorRef childRouter;
  private final List<ChildActor.ChildResponse> responses;
  private final CompletableFuture<MasterResponse> future;

  public MasterActor(CompletableFuture<MasterResponse> future) {
    this.future = future;
    this.childRouter = getContext().actorOf(
        new RoundRobinPool(SearchEngine.values().length).props(Props.create(ChildActor.class)),
        "childRouter"
    );
    responses = new ArrayList<>();
  }

  @Value
  public static class MasterRequest {
    String query;
  }

  @AllArgsConstructor
  @Data
  public static class MasterResponse {
    List<ChildActor.ChildResponse> responses;

    @Override
    public String toString() {
      return new Gson().toJson(this);
    }
  }

  @Override
  public Receive createReceive() {
    return new ReceiveBuilder()
        .match(MasterRequest.class, this::onMasterRequest)
        .match(ChildActor.ChildResponse.class, this::onChildResponse)
        .match(ReceiveTimeout.class, this::onReceiveTimeout)
        .build();
  }

  private void onMasterRequest(MasterRequest request) {
    Arrays.stream(SearchEngine.values()).forEach(searchEngine -> childRouter.tell(
        new ChildActor.ChildRequest(searchEngine, request.getQuery()),
        getSelf()
    ));
    getContext().setReceiveTimeout(Duration.create(5, TimeUnit.SECONDS));
  }

  private void onChildResponse(ChildActor.ChildResponse response) {
    responses.add(response);
    if (responses.size() == SearchEngine.values().length) {
      completeResponse();
      getContext().cancelReceiveTimeout();
      context().stop(self());
    }
  }

  private void onReceiveTimeout(ReceiveTimeout msg) {
    completeResponse();
    getContext().cancelReceiveTimeout();
    context().stop(self());
  }

  private void completeResponse() {
    future.complete(new MasterResponse(responses));
  }
}
