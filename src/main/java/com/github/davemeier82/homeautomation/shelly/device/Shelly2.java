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

import com.github.davemeier82.homeautomation.core.device.mqtt.DefaultMqttSubscriber;
import com.github.davemeier82.homeautomation.core.device.property.DeviceProperty;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import com.github.davemeier82.homeautomation.shelly.device.property.ShellyRelay;
import com.github.davemeier82.homeautomation.shelly.device.property.ShellyRoller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.github.davemeier82.homeautomation.shelly.mapper.RollerStateMapper.rollerStateFrom;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Shelly2 extends DefaultMqttSubscriber {

  private static final Logger log = LoggerFactory.getLogger(Shelly2.class);
  public static final String PREFIX = "shellyswitch-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shelly2";

  private final String id;
  private final String baseTopic;
  private final List<ShellyRelay> relays;
  private final ShellyRoller roller;

  public Shelly2(String id,
                 String displayName,
                 MqttClient mqttClient,
                 EventPublisher eventPublisher,
                 EventFactory eventFactory,
                 Map<String, String> customIdentifiers
  ) {
    super(displayName, customIdentifiers);
    this.id = id;
    baseTopic = MQTT_TOPIC + id + "/";
    relays = List.of(
        new ShellyRelay(0, this, getRelayCommandTopic(0), eventPublisher, eventFactory, mqttClient),
        new ShellyRelay(1, this, getRelayCommandTopic(1), eventPublisher, eventFactory, mqttClient)
    );
    roller = new ShellyRoller(2, this, getRollerCommandTopic(), eventPublisher, eventFactory, mqttClient);
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
      } else if (topic.equals(getRelayTopic(1))) {
        changeStateOfRelay(1, message);
      } else if (topic.startsWith(getRollerTopic())) {
        processRollerMessage(topic, message);
      }
    });
  }

  private void processRollerMessage(String topic, String message) {
    if (topic.equals(getRollerTopic())) {
      roller.setRelayStateTo(rollerStateFrom(message));
    } else if (topic.endsWith("/pos")) {
      roller.setPositionInPercent(Integer.parseInt(message));
    }
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

  private String getRelayCommandTopic(int relayIndex) {
    return getRelayTopic(relayIndex) + "/command";
  }

  private String getRollerCommandTopic() {
    return getRollerTopic() + "/command";
  }

  private String getRelayTopic(int relayIndex) {
    return baseTopic + "relay/" + relayIndex;
  }

  private String getRollerTopic() {
    return baseTopic + "roller/0";
  }

  @Override
  public List<? extends DeviceProperty> getDeviceProperties() {
    return List.of(relays.get(0), relays.get(1), roller);
  }
}
