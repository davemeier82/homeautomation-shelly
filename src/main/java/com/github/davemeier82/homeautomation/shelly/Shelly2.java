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

import com.github.davemeier82.homeautomation.core.device.mqtt.MqttMultiRelay;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Shelly2 implements MqttMultiRelay {

  private static final Logger log = LoggerFactory.getLogger(Shelly1.class);
  public static final String PREFIX = "shellyswitch-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellies/shelly2";

  private final String id;
  private final MqttClient mqttClient;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final String baseTopic;
  private final List<AtomicReference<DataWithTimestamp<Boolean>>> isOn = List.of(new AtomicReference<>(), new AtomicReference<>());
  private String displayName;

  public Shelly2(String id, String displayName, MqttClient mqttClient, EventPublisher eventPublisher, EventFactory eventFactory) {
    this.id = id;
    this.displayName = displayName;
    this.mqttClient = mqttClient;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
    baseTopic = MQTT_TOPIC + id + "/";
  }

  @Override
  public String getTopic() {
    return baseTopic + "#";
  }

  @Override
  public void processMessage(String topic, Optional<ByteBuffer> payload) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", topic, message);
      if (topic.equals(getRelayTopic(0))) {
        changeStateOfRelay(0, message);
      }
      if (topic.equals(getRelayTopic(1))) {
        changeStateOfRelay(1, message);
      }
    });
  }

  private void changeStateOfRelay(int relayIndex, String message) {
    if ("off".equalsIgnoreCase(message)) {
      setRelayStateTo(relayIndex, false);
    } else if ("on".equalsIgnoreCase(message)) {
      setRelayStateTo(relayIndex, true);
    }
  }

  private void setRelayStateTo(int relayIndex, boolean on) {
    AtomicReference<DataWithTimestamp<Boolean>> relayState = isOn.get(relayIndex);
    if (relayState.get() == null || relayState.get().getValue() != on) {
      DataWithTimestamp<Boolean> newValue = new DataWithTimestamp<>(on);
      relayState.set(newValue);
      eventPublisher.publishEvent(eventFactory.createMultiRelayStateChangedEvent(this, relayIndex, newValue));
    }
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
  public void turnOn(int relayIndex) {
    mqttClient.publish(getCommandTopic(relayIndex), "on");
  }

  @Override
  public void turnOff(int relayIndex) {
    mqttClient.publish(getCommandTopic(relayIndex), "off");
  }

  @Override
  public Optional<DataWithTimestamp<Boolean>> isOn(int relayIndex) {
    return Optional.ofNullable(isOn.get(relayIndex).get());
  }

  private String getCommandTopic(int relayIndex) {
    return getRelayTopic(relayIndex) + "/command";
  }

  private String getRelayTopic(int relayIndex) {
    return baseTopic + "relay/" + relayIndex;
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
