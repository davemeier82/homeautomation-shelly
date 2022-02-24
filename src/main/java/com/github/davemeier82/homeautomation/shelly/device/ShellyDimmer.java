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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davemeier82.homeautomation.core.device.mqtt.DefaultMqttSubscriber;
import com.github.davemeier82.homeautomation.core.device.property.DeviceProperty;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import com.github.davemeier82.homeautomation.shelly.device.property.ShellyDimmerRelay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellyDimmer extends DefaultMqttSubscriber {
  private static final Logger log = LoggerFactory.getLogger(ShellyDimmer.class);
  public static final String PREFIX = "shellydimmer-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellydimmer";

  private final String id;
  private final String baseTopic;
  private final ObjectMapper objectMapper;
  private final ShellyDimmerRelay dimmer;

  public ShellyDimmer(String id,
                      String displayName,
                      MqttClient mqttClient,
                      EventPublisher eventPublisher,
                      EventFactory eventFactory,
                      ObjectMapper objectMapper,
                      Map<String, String> customIdentifiers
  ) {
    super(displayName, customIdentifiers);
    this.id = id;
    baseTopic = MQTT_TOPIC + id + "/";
    this.objectMapper = objectMapper;
    dimmer = new ShellyDimmerRelay(0, this, getCommandTopic(), getSetTopic(), eventPublisher, eventFactory, mqttClient);
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
    return getLightTopic() + "/command";
  }

  private String getSetTopic() {
    return getLightTopic() + "/set";
  }

  private String getLightTopic() {
    return baseTopic + "light/0";
  }

  @Override
  public void processMessage(String topic, Optional<ByteBuffer> payload) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", topic, message);
      if (topic.equals(getLightTopic())) {
        processLightMessage(message);
      } else if (topic.equals(getLightTopic() + "/status")) {
        processStatusMessage(message);
      }
    });
  }

  private void processStatusMessage(String message) {
    try {
      StatusMessage statusMessage = objectMapper.readValue(message, StatusMessage.class);
      boolean newOnState = statusMessage.ison;
      dimmer.setRelayStateTo(newOnState);
      dimmer.setDimmingLevelInPercent(statusMessage.brightness);
    } catch (JsonProcessingException e) {
      log.error("failed to unmarshall status message: {}", message, e);
    }
  }

  private void processLightMessage(String message) {
    if ("off".equalsIgnoreCase(message)) {
      dimmer.setRelayStateTo(false);
    } else if ("on".equalsIgnoreCase(message)) {
      dimmer.setRelayStateTo(true);
    }
  }

  @Override
  public List<DeviceProperty> getDeviceProperties() {
    return List.of(dimmer);
  }

  private static class StatusMessage {
    public boolean ison;
    public int brightness;
  }
}
