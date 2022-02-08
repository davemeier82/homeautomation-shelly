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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Shelly1 extends DefaultMqttSubscriber {
  private static final Logger log = LoggerFactory.getLogger(Shelly1.class);
  public static final String PREFIX = "shelly1-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shelly1";

  private final String id;
  private final String baseTopic;
  private final ShellyRelay relay;

  public Shelly1(String id,
                 String displayName,
                 MqttClient mqttClient,
                 EventPublisher eventPublisher,
                 EventFactory eventFactory,
                 Map<String, String> customIdentifiers
  ) {
    super(displayName, customIdentifiers);
    this.id = id;
    baseTopic = MQTT_TOPIC + id + "/";
    relay = new ShellyRelay(0, this, getCommandTopic(), eventPublisher, eventFactory, mqttClient);
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
          relay.setRelayStateTo(false);
        } else if ("on".equalsIgnoreCase(message)) {
          relay.setRelayStateTo(true);
        }
      }
    });
  }

  @Override
  public List<DeviceProperty> getDeviceProperties() {
    return List.of(relay);
  }
}
