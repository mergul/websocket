package news.busra.websocket;


import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.util.annotation.NonNull;

import java.security.Principal;
import java.util.ArrayList;

abstract class AbstractHandler implements WebSocketHandler {

    protected ArrayList<String> authorizedRoles = new ArrayList<String>();

    public AbstractHandler(){
    }
    @NonNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.getHandshakeInfo().getPrincipal().filter(this::isAuthorized).then(doHandle(session));
    }

    private boolean isAuthorized(Principal principal) {
        return true;
    }

    private boolean hasRoles() {
        return true;
    }

    abstract Mono<Void> doHandle(WebSocketSession session);
}
