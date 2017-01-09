package io.scalecube.gateway.rabbitmq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.HashMap;

public class RabbitPublisherTest {

  @Test
  public void test_rabbit_publisher() {

    try {
      new RabbitPublisher("localhost", 5672, 5, null);
    } catch (Exception e) {
      if (e.getMessage() != null) {
        assertEquals(e.getMessage().toString(), "connect timed out");
      } else {
        e.printStackTrace();
      }
    }

    try {
      new RabbitPublisher("localhost", -1, 3, null);
    } catch (Exception e) {
      assertEquals(e.getMessage().toString(), "connect timed out");
    }

    try {
      new RabbitPublisher("localhost", -1, 1000, new BasicCredentials("a", "b"));
    } catch (Exception e) {
      assertEquals(e.getMessage().toString(),
          "ACCESS_REFUSED - Login was refused using authentication mechanism PLAIN. For details see the broker logfile.");
    }

    try {
      Credentials cred = new Credentials() {};
      new RabbitPublisher("localhost", -1, 1000, cred);

    } catch (Exception e) {
      assertEquals(e.getMessage().toString(),
          "ACCESS_REFUSED - Login was refused using authentication mechanism PLAIN. For details see the broker logfile.");
    }

    try {
      RabbitPublisher publisher = new RabbitPublisher("localhost", -1, 1000, null);
      Exchange exchange = Exchange.builder()
      .durable(false)
      .autoDelete(false)
      .internal(false)
      .type("direct")
      .name("in")
      .properties(new HashMap<>()).build();
      assertTrue(!exchange.internal());
      
      publisher.subscribe(exchange);
      
      assertTrue(publisher.channel().isOpen());
      publisher.close();
      assertTrue(!publisher.channel().isOpen());
    } catch (Exception ex) {
      assertEquals(ex.getMessage().toString(), ".");
    }

  }

}
