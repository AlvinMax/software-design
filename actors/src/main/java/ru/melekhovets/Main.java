package ru.melekhovets;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import ru.melekhovets.akka.model.actor.MasterActor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.CompletableFuture;

public class Main {
  public static void main(String[] args) throws IOException {
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    ActorSystem system = ActorSystem.create();

    String in;
    while ((in = bufferedReader.readLine()) != null) {
      if (in.equals("quit")) {
        system.terminate();
        System.exit(0);
      }
      CompletableFuture<MasterActor.MasterResponse> future = new CompletableFuture<>();
      ActorRef master = system.actorOf(
          Props.create(MasterActor.class, future),
          "master");
      master.tell(new MasterActor.MasterRequest(in), ActorRef.noSender());
      try {
        MasterActor.MasterResponse response = future.get();
        System.out.println(response);
      } catch (Exception e) {
        System.out.println("Error during searching results");
      }
    }
  }
}
