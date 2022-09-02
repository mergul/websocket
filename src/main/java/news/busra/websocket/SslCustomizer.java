package news.busra.websocket;

import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.embedded.netty.SslServerCustomizer;
import org.springframework.boot.web.server.Http2;
import org.springframework.boot.web.server.Ssl;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

@Component
public class SslCustomizer implements WebServerFactoryCustomizer<NettyReactiveWebServerFactory> {

   @Override
   public void customize(NettyReactiveWebServerFactory serverFactory) {
       Ssl ssl = new Ssl();
       ssl.setEnabled(true);
       ssl.setKeyStore("classpath:myidentity.jks");
       ssl.setKeyAlias("1");
       ssl.setKeyPassword("Merina75");
       ssl.setKeyStorePassword("Merina75");
       ssl.setTrustStore("classpath:mytrust.jks");
       ssl.setTrustStorePassword("Merina75");
       Http2 http2 = new Http2();
       http2.setEnabled(false);
       serverFactory.addServerCustomizers(new SslServerCustomizer(ssl, http2, null));
       serverFactory.setPort(65080);
   }
}
