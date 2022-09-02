package news.busra.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.publisher.UnicastProcessor;

import java.io.IOException;
import java.util.*;

@Component
public class SocketHandler extends AbstractHandler {
    private static final Logger log = LoggerFactory.getLogger(SocketHandler.class);
    Map<String, WebSocketSession> sessions = new HashMap<>();
    Map<String, String> userSessions = new HashMap<>();
    Map<String, List<String>> rooms = new HashMap<>();
    ObjectMapper parser = new ObjectMapper();
    private final UnicastProcessor<String> eventPublisher;
    private final Flux<String> outputEvents;
    private WebSocketSession mySession = null;

    public SocketHandler(UnicastProcessor<String> eventPublisher, Flux<String> events) {
        this.eventPublisher = eventPublisher;
        this.outputEvents = Flux.from(events);
        this.authorizedRoles.addAll(Collections.singletonList("ROLE_ADMIN"));
    }

    public Mono<Void> doHandle(WebSocketSession session) {
        SocketMessageComponent subscriber = new SocketMessageComponent(this.eventPublisher);
        sessions.put(session.getId(), session);
        this.mySession = session;

        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .map(this::checkEvent)
                .doOnNext(subscriber::sendMessage)
                .doOnError(subscriber::onError)
                .doOnComplete(subscriber::onComplete)
                .doFinally(sig -> {
                    String user = userSessions.get(session.getId());
                    log.info("Terminating WebSocket Session (client side) sig: [{}], [{}]", sig.name(), user);
                    session.close();
                    rooms.forEach((s, strings) -> strings.remove(user));
                    sessions.remove(session.getId());  // remove the stored session id
                    userSessions.remove(session.getId());
                })
                .zipWith(this.mySession.send(outputEvents.filter(s -> applicable(Objects.requireNonNull(getJson(s)), session))
                        .map(this.mySession::textMessage)))
                .then();
    }

    private boolean applicable(JsonNode jsonNode, WebSocketSession session) {
        if (jsonNode.get("target")!=null) {
            JsonNode targetUser = jsonNode.get("target");
            JsonNode user = jsonNode.get("name");
            String userName = userSessions.get(session.getId());
            if (user != null) log.info("Call (client side) from --> to : [{}], [{}]", user.asText(), userName);
            return userName.equals(targetUser.asText());
        } else if (jsonNode.get("room")!=null) {
            JsonNode room = jsonNode.get("room");
            String userName = userSessions.get(session.getId());
            return rooms.get(room.asText()).contains(userName);
        } else {
            return true;
        }
    }

    private String checkEvent(String payload) {
        JsonNode jn = getJson(payload);
        if (jn != null && "id".equals(jn.get("event").asText())) {
            String user = jn.get("data").asText();
            String room = jn.get("room").asText();
            if (userSessions.containsValue(user)) user += 1;
            if (userSessions.containsValue(user)) user += 2;
            if (userSessions.containsValue(user)) user += 3;
            if (userSessions.containsValue(user)) user += 4;
            if (userSessions.containsValue(user)) user += 5;
            if (userSessions.containsValue(user)) user += 6;
            userSessions.put(mySession.getId(), user);
            if (rooms.containsKey(room) && !rooms.get(room).contains(user)) {
                List<String> list = rooms.get(room);
                list.add(user);
                rooms.put(room, list);
            } else if (!rooms.containsKey(room)) {
                List<String> list = new ArrayList<>();
                list.add(user);
                rooms.put(room, list);
            }
            return getIdEventMessage(user, room, rooms.get(room));
        }
        return payload;
    }

    private JsonNode getJson(String payload) {
        try {
           return parser.readTree(payload);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String getIdEventMessage(String user, String room, List<String> rookies) {
        try {
            return parser.writeValueAsString(new IdEvent("id", user, room, rookies));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "";
        }
    }

    private static class SocketMessageComponent {

        private final UnicastProcessor<String> emitterProcessor;
        private final FluxSink<String> fluxSink;
        private Optional<String> lastReceivedEvent = Optional.empty();

        public SocketMessageComponent(UnicastProcessor<String> emitterProcessor) {
            this.emitterProcessor = emitterProcessor;
            this.fluxSink = emitterProcessor.sink(FluxSink.OverflowStrategy.LATEST);
        }

        public void sendMessage(String message) {
            lastReceivedEvent = Optional.of(message);
            this.fluxSink.next(message);
        }

        public void onError(Throwable error) {
            error.printStackTrace();
        }

        public void onComplete() {

            lastReceivedEvent.ifPresent(event -> {
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode node = mapper.getNodeFactory().objectNode();
                    node.put("event", "quit");
                    emitterProcessor.onNext(mapper.writeValueAsString(node));
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
