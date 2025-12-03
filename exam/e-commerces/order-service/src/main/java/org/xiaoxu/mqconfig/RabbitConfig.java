package org.xiaoxu.mqconfig;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
  @Bean
  public TopicExchange orderEventsExchange() {
    return new TopicExchange("order_events", true, false);
  }
}
