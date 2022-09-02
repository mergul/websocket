package news.busra.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.springframework.web.reactive.socket.server.upgrade.ReactorNettyRequestUpgradeStrategy;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableWebFlux
public class SocketRouter {

    public SocketRouter() { }

    @Primary
    @Bean
    public HandlerMapping webSocketMapping(UnicastProcessor<String> eventPublisher, Flux<String> events) {
        Map<String, Object> map = new HashMap<>();
        map.put("/ws", new SocketHandler(eventPublisher, events));
        SimpleUrlHandlerMapping simpleUrlHandlerMapping = new SimpleUrlHandlerMapping();
        simpleUrlHandlerMapping.setUrlMap(map);

        simpleUrlHandlerMapping.setOrder(10);
        return simpleUrlHandlerMapping;
    }
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService(new ReactorNettyRequestUpgradeStrategy());
    }

}
