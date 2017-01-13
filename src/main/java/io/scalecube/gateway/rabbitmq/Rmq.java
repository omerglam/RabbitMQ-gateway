package io.scalecube.gateway.rabbitmq;

import io.scalecube.gateway.rabbitmq.Rmq.Builder;
import io.scalecube.gateway.rabbitmq.serialization.proto.JsonMessageSerialization;
import io.scalecube.gateway.rabbitmq.serialization.proto.ProtoMessageSerialization;
import io.scalecube.gateway.rabbitmq.serialization.text.PlainMessageSeriazliation;

import com.rabbitmq.client.AMQP;

import rx.Observable;

public class Rmq implements AutoCloseable {

  private RabbitPublisher publisher;
  private RabbitListener listener;
  private MessageSerialization rmqSerialization;


  public static class Builder {


    private String host = "localhost";

    private RabbitListener rabbitListener;

    private int port = -1;

    private int timeout = 0;

    private Credentials credentials;

    private MessageSerialization serialization = MessageSerialization.empty();

    private boolean autoRecovery = true;

    /**
     * Set the host of the broker.
     * 
     * @param host to use when connecting to the RMQ broker.
     */
    public Builder host(String host) {
      this.host = host;
      return this;
    }

    /**
     * Set the port of the broker.
     * 
     * @param port to use when connecting to the RMQ broker
     */
    public Builder port(int port) {
      this.port = port;
      return this;
    }

    /**
     * Set the password.
     * 
     * @param credentials the password to use when connecting to the RMQ broker if null not in use.
     */
    public Builder credentials(Credentials credentials) {
      this.credentials = credentials;
      return this;
    }

    /**
     * Set the TCP connection timeout.
     * 
     * @param timeout connection TCP establishment timeout in milliseconds; zero for infinite
     */
    public Builder timeout(int timeout) {
      this.timeout = timeout;
      return this;
    }

    public Rmq build() throws Exception {
      return new Rmq(
          new RabbitListener(this),
          new RabbitPublisher(this),
          this.serialization);
    }

    public Builder plain() {
      this.serialization = new PlainMessageSeriazliation();
      return this;
    }

    public Builder proto() {
      this.serialization = new ProtoMessageSerialization();
      return this;
    }

    public Builder json() {
      this.serialization = new JsonMessageSerialization();
      return this;
    }

    public Credentials credentials() {
      return this.credentials;
    }

    public MessageSerialization serialization() {
      return this.serialization;
    }

    public String host() {
      return this.host;
    }

    public int port() {
      return this.port;
    }

    public int timeout() {
      return this.timeout;
    }

    public Builder autoRecovery(boolean autoRecovery) {
      this.autoRecovery = autoRecovery;
      return this;
    }

    public boolean autoRecovery() {
      return autoRecovery;
    }

    public Builder serialization(MessageSerialization serialization) {
      this.serialization = serialization;
      return this;
    }
  }

  private Rmq(RabbitListener rabbitListener, RabbitPublisher rabbitPublisher, MessageSerialization serialization) {
    this.listener = rabbitListener;
    this.publisher = rabbitPublisher;
    this.rmqSerialization = serialization;
  }

  public static Builder builder() {
    return new Builder();
  }

  public Rmq topic(Topic topic) throws Exception {
    listener.subscribe(topic);
    return this;
  }

  public Rmq exchange(Exchange exchange, Topic topic, String routingKey) throws Exception {
    listener.subscribe(exchange, topic, routingKey);
    return this;
  }

  public <T> Observable<T> listen(Class<T> class1) {
    return listener.listen(class1);
  }

  public Observable<byte[]> listen() {
    return listener.listen();
  }

  /**
   * publish message to queue.
   * 
   * @param topic to publish.
   * @param obj the message to publish
   * @throws Exception exception if failed.
   */

  public <T> void publish(Topic topic, Object obj) throws Exception {
    publisher.channel().basicPublish(topic.exchange(), topic.name(),
        topic.properties(),
        rmqSerialization.serialize((T) obj,
            (Class<T>) obj.getClass()));
  }

  /**
   * publish message to exchange.
   * 
   * @param exchange Exchange to publish.
   * @param routingKey routing key to be used.
   * @param obj the message to publish
   * @throws Exception exception if failed.
   */
  public <T> void publish(Exchange exchange, String routingKey, Object obj) throws Exception {
    publisher.channel().basicPublish(exchange.exchange(), routingKey,
        new AMQP.BasicProperties.Builder()
            .deliveryMode(1) // transient
            .build(),
        rmqSerialization.serialize((T) obj,
            (Class<T>) obj.getClass()));
  }


  @Override
  public void close() throws Exception {
    this.publisher.close();
    this.listener.close();
  }

}
