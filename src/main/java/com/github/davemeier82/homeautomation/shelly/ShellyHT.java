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

import com.github.davemeier82.homeautomation.core.device.BatteryStateSensor;
import com.github.davemeier82.homeautomation.core.device.mqtt.MqttHumiditySensor;
import com.github.davemeier82.homeautomation.core.device.mqtt.MqttTemperatureSensor;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellyHT implements MqttTemperatureSensor, MqttHumiditySensor, BatteryStateSensor {
  private static final Logger log = LoggerFactory.getLogger(ShellyHT.class);
  public static final String PREFIX = "shellyht-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellies/shellyht";

  private final String id;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final String baseTopic;
  private final AtomicReference<DataWithTimestamp<Float>> temperature = new AtomicReference<>();
  private final AtomicReference<DataWithTimestamp<Float>> humidity = new AtomicReference<>();
  private final AtomicReference<DataWithTimestamp<Integer>> batteryLevel = new AtomicReference<>();
  private String displayName;

  public ShellyHT(String id, String displayName, EventPublisher eventPublisher, EventFactory eventFactory) {
    this.id = id;
    this.displayName = displayName;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
    baseTopic = MQTT_TOPIC + id + "/sensor/";
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
      if (topic.endsWith("/temperature")) {
        DataWithTimestamp<Float> newValue = new DataWithTimestamp<>(Float.valueOf(message));
        temperature.set(newValue);
        eventPublisher.publishEvent(eventFactory.createTemperatureChangedEvent(this, newValue));
      } else if (topic.endsWith("/humidity")) {
        DataWithTimestamp<Float> newValue = new DataWithTimestamp<>(Float.valueOf(message));
        humidity.set(newValue);
        eventPublisher.publishEvent(eventFactory.createHumidityChangedEvent(this, newValue));
      } else if (topic.endsWith("/battery")) {
        DataWithTimestamp<Integer> newValue = new DataWithTimestamp<>(Integer.valueOf(message));
        batteryLevel.set(newValue);
        eventPublisher.publishEvent(eventFactory.createBatteryLevelChangedEvent(this, newValue));
      }
    });
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
  public Optional<DataWithTimestamp<Float>> getRelativeHumidityInPercent() {
    return Optional.ofNullable(humidity.get());
  }

  @Override
  public Optional<DataWithTimestamp<Float>> getTemperatureInDegree() {
    return Optional.ofNullable(temperature.get());
  }

  @Override
  public Optional<DataWithTimestamp<Integer>> batteryLevelInPercent() {
    return Optional.ofNullable(batteryLevel.get());
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
