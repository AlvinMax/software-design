import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import org.json.JSONArray;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;
import ru.melekhovets.akka.model.actor.ChildActor;
import ru.melekhovets.akka.model.actor.MasterActor;
import ru.melekhovets.akka.search.SearchEngine;
import ru.melekhovets.akka.search.impl.SearchServerStub;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class ActorTest {
    private ActorSystem system;
    private SearchServerStub serverStub;

    @Before
    public void initSystem() {
        system = ActorSystem.create("TestSystem");
        serverStub = SearchServerStub.getInstance();
    }

    @After
    public void stopSystem() {
        system.terminate();
        system = null;
        serverStub = null;
    }

    private final static String TEST_QUERY = "hello";
    private final static JSONArray CHILD_RESPONSE = new JSONArray("[{\"title\":\"title0\",\"body\":\"body0\"}," +
            "{\"title\":\"title1\",\"body\":\"body1\"}," +
            "{\"title\":\"title2\",\"body\":\"body2\"}," +
            "{\"title\":\"title3\",\"body\":\"body3\"}]");

    @Test
    public void testNormal() {
        CompletableFuture<MasterActor.MasterResponse> future = new CompletableFuture<>();
        ActorRef master = system.actorOf(Props.create(MasterActor.class, future), "master");
        master.tell(new MasterActor.MasterRequest("hello"), ActorRef.noSender());

        MasterActor.MasterResponse response = null;
        try {
            response = future.get(10, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }

        Assert.assertNotNull(response);
        Assert.assertEquals(SearchEngine.values().length, response.getResponses().size());
        for (ChildActor.ChildResponse childResponse : response.getResponses()) {
            Assert.assertEquals(TEST_QUERY, childResponse.getQuery());
            switch (childResponse.getSearchEngine()) {
                case BING:
                case GOOGLE:
                case YANDEX:
                    JSONAssert.assertEquals(CHILD_RESPONSE, childResponse.getResults(), false);
                    break;
                default:
                    Assert.fail();
            }
        }
    }

    @Test
    public void testTimeout() {
        serverStub.setSleepTime(10000);
        CompletableFuture<MasterActor.MasterResponse> future = new CompletableFuture<>();
        ActorRef master = system.actorOf(Props.create(MasterActor.class, future), "master");
        master.tell(new MasterActor.MasterRequest("hello"), ActorRef.noSender());

        MasterActor.MasterResponse response = null;
        try {
            response = future.get(10, TimeUnit.SECONDS);
        } catch (Exception ignored) {
            System.out.println("wtf");
        }

        Assert.assertNotNull(response);
        Assert.assertTrue(response.getResponses().isEmpty());
        serverStub.setSleepTime(0);
    }
}
