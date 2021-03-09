/*
 * Copyright 2021-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.davemeier82.homeautomation.shelly;

import com.github.davemeier82.homeautomation.core.device.mqtt.MqttRelay;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Shelly1 implements MqttRelay {
  private static final Logger log = LoggerFactory.getLogger(Shelly1.class);
  public static final String PREFIX = "shelly1-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellies/shelly1";

  private final String id;
  private final MqttClient mqttClient;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final String baseTopic;
  private final AtomicReference<DataWithTimestamp<Boolean>> isOn = new AtomicReference<>();
  private String displayName;

  public Shelly1(String id, String displayName, MqttClient mqttClient, EventPublisher eventPublisher, EventFactory eventFactory) {
    this.id = id;
    this.displayName = displayName;
    this.mqttClient = mqttClient;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
    baseTopic = MQTT_TOPIC + id + "/";
  }

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public String getTopic() {
    return baseTopic + "#";
  }

  @Override
  public void turnOn() {
    mqttClient.publish(getCommandTopic(), "on");
  }

  @Override
  public void turnOff() {
    mqttClient.publish(getCommandTopic(), "off");
  }

  @Override
  public Optional<DataWithTimestamp<Boolean>> isOn() {
    return Optional.ofNullable(isOn.get());
  }

  private String getCommandTopic() {
    return getRelayTopic() + "/command";
  }

  private String getRelayTopic() {
    return baseTopic + "relay/0";
  }

  @Override
  public void processMessage(String topic, Optional<ByteBuffer> payload) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", topic, message);
      if (topic.equals(getRelayTopic())) {
        if ("off".equalsIgnoreCase(message)) {
          setRelayStateTo(false);
        } else if ("on".equalsIgnoreCase(message)) {
          setRelayStateTo(true);
        }
      }
    });
  }

  private void setRelayStateTo(boolean on) {
    if (isOn.get() == null || isOn.get().getValue() != on) {
      DataWithTimestamp<Boolean> newValue = new DataWithTimestamp<>(on);
      isOn.set(newValue);
      eventPublisher.publishEvent(eventFactory.createRelayStateChangedEvent(this, newValue));
    }
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

  @Override
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }
}
