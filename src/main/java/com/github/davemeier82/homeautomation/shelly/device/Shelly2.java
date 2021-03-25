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

package com.github.davemeier82.homeautomation.shelly.device;

import com.github.davemeier82.homeautomation.core.device.mqtt.MqttSubscriber;
import com.github.davemeier82.homeautomation.core.device.property.Relay;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import com.github.davemeier82.homeautomation.shelly.device.property.ShellyRelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Shelly2 implements MqttSubscriber {

  private static final Logger log = LoggerFactory.getLogger(Shelly2.class);
  public static final String PREFIX = "shellyswitch-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellies/shelly2";

  private final String id;
  private final String baseTopic;
  private final List<ShellyRelay> relays;
  private String displayName;

  public Shelly2(String id, String displayName, MqttClient mqttClient, EventPublisher eventPublisher, EventFactory eventFactory) {
    this.id = id;
    this.displayName = displayName;
    baseTopic = MQTT_TOPIC + id + "/";
    relays = List.of(
        new ShellyRelay(0, this, getCommandTopic(0), eventPublisher, eventFactory, mqttClient),
        new ShellyRelay(1, this, getCommandTopic(1), eventPublisher, eventFactory, mqttClient)
    );
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
      relays.get(relayIndex).setRelayStateTo(false);
    } else if ("on".equalsIgnoreCase(message)) {
      relays.get(relayIndex).setRelayStateTo(true);
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

  @Override
  public List<? extends Relay> getDeviceProperties() {
    return relays;
  }
}
