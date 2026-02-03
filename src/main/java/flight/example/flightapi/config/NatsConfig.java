package flight.example.flightapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.Nats;
import io.nats.client.Options;

@Configuration
public class NatsConfig {

   @Value("${NATS_SERVER:nats://nats:4222}")
   private String natsServer;

   @Bean
   public Connection natsConnection() throws Exception {
      Options options = new Options.Builder()
            .server(natsServer)
            .build();
      return Nats.connect(options);
   }

   @Bean
   public JetStream jetStream(Connection nc) throws Exception {
      return nc.jetStream();
   }
}