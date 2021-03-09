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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.davemeier82.homeautomation.core.device.mqtt.MqttDimmer;
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

public class ShellyDimmer implements MqttDimmer {
  private static final Logger log = LoggerFactory.getLogger(ShellyDimmer.class);
  public static final String PREFIX = "shellydimmer-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellies/shellydimmer";

  private final String id;
  private final MqttClient mqttClient;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final String baseTopic;
  private final ObjectMapper objectMapper;
  private final AtomicReference<DataWithTimestamp<Boolean>> isOn = new AtomicReference<>();
  private final AtomicReference<DataWithTimestamp<Integer>> brightness = new AtomicReference<>();
  private String displayName;

  public ShellyDimmer(String id,
                      String displayName,
                      MqttClient mqttClient,
                      EventPublisher eventPublisher,
                      EventFactory eventFactory,
                      ObjectMapper objectMapper
  ) {
    this.id = id;
    this.displayName = displayName;
    this.mqttClient = mqttClient;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
    baseTopic = MQTT_TOPIC + id + "/";
    this.objectMapper = objectMapper;
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
  public void setDimmingLevel(int percent) {
    boolean on = percent > 0;
    mqttClient.publish(getSetTopic(), "{\"brightness\": " + percent + ", \"turn\": \"" + on + "\"}");
  }

  @Override
  public Optional<DataWithTimestamp<Boolean>> isOn() {
    return Optional.ofNullable(isOn.get());
  }

  @Override
  public Optional<DataWithTimestamp<Integer>> getDimmingLevelInPercent() {
    return Optional.ofNullable(brightness.get());
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
      boolean newOnState = statusMessage.isOn;
      setLightStateTo(newOnState);
      if (brightness.get() == null || brightness.get().getValue() != statusMessage.brightness) {
        DataWithTimestamp<Integer> newValue = new DataWithTimestamp<>(statusMessage.brightness);
        brightness.set(newValue);
        eventPublisher.publishEvent(eventFactory.createDimmingLevelChangedEvent(this, newValue));
      }

    } catch (JsonProcessingException e) {
      log.error("failed to unmarshall status message: {}", message, e);
    }
    // TODO: brightness
  }

  private void processLightMessage(String message) {
    if ("off".equalsIgnoreCase(message)) {
      setLightStateTo(false);
    } else if ("on".equalsIgnoreCase(message)) {
      setLightStateTo(true);
    }
  }

  private void setLightStateTo(boolean on) {
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

  private static class StatusMessage {
    public boolean isOn;
    public int brightness;
  }
}
