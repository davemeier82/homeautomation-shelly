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
import com.github.davemeier82.homeautomation.core.device.mqtt.MqttWindowSensor;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellyDoorWindow implements MqttWindowSensor, BatteryStateSensor {
  private static final Logger log = LoggerFactory.getLogger(ShellyDoorWindow.class);
  public static final String PREFIX = "shellydw-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellies/shelly-door-window";

  private final String id;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final AtomicReference<DataWithTimestamp<Boolean>> isOpen = new AtomicReference<>();
  private final AtomicReference<DataWithTimestamp<Integer>> tiltAngleInDegree = new AtomicReference<>();
  private final AtomicReference<DataWithTimestamp<Integer>> batteryLevel = new AtomicReference<>();
  protected String baseTopic;
  private String displayName;

  public ShellyDoorWindow(String id, String displayName, EventPublisher eventPublisher, EventFactory eventFactory) {
    this.id = id;
    this.displayName = displayName;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
    baseTopic = MQTT_TOPIC + id + "/sensor/";
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
  public void processMessage(String topic, Optional<ByteBuffer> payload) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", topic, message);
      if (topic.endsWith("/state")) {
        if (message.equals("open")) {
          updateOpenState(true);
        } else if (message.equals("close")) {
          updateOpenState(false);
        }
      } else if (topic.endsWith("/battery")) {
        DataWithTimestamp<Integer> newValue = new DataWithTimestamp<>(Integer.valueOf(message));
        batteryLevel.set(newValue);
        eventPublisher.publishEvent(eventFactory.createBatteryLevelChangedEvent(this, newValue));
      }
      if (topic.endsWith("/tilt")) {
        int angle = parseInt(message);
        if (angle != -1) {
          tiltAngleInDegree.set(new DataWithTimestamp<>(angle));
        }
      }
    });
  }

  private void updateOpenState(boolean open) {
    if (isOpen.get() == null || !isOpen.get().getValue().equals(open)) {
      isOpen.set(new DataWithTimestamp<>(open));
      if (open) {
        eventPublisher.publishEvent(eventFactory.createWindowOpenedEvent(this, ZonedDateTime.now()));
      } else {
        eventPublisher.publishEvent(eventFactory.createWindowClosedEvent(this, ZonedDateTime.now()));
      }
    }
  }

  @Override
  public Optional<DataWithTimestamp<Boolean>> isOpen() {
    return Optional.ofNullable(isOpen.get());
  }

  @Override
  public Optional<DataWithTimestamp<Integer>> getTiltAngleInDegree() {
    return Optional.ofNullable(tiltAngleInDegree.get());
  }

  @Override
  public boolean isTiltingSupported() {
    return true;
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
