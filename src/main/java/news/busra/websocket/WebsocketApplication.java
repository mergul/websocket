package news.busra.websocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.UnicastProcessor;

@SpringBootApplication
public class WebsocketApplication {

	public static void main(String[] args) {
		SpringApplication.run(WebsocketApplication.class, args);
	}
	@Bean
	public UnicastProcessor<String> eventPublisher(){
		return UnicastProcessor.create();
	}
	@Bean
	public Flux<String> events(UnicastProcessor<String> eventPublisher) {
		return eventPublisher.replay(1).autoConnect();
	}
}
