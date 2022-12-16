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

package io.github.davemeier82.homeautomation.shelly.device;

import io.github.davemeier82.homeautomation.core.device.mqtt.DefaultMqttSubscriber;
import io.github.davemeier82.homeautomation.core.device.property.DeviceProperty;
import io.github.davemeier82.homeautomation.core.device.property.defaults.DefaultBatteryStateSensor;
import io.github.davemeier82.homeautomation.core.device.property.defaults.DefaultWindowSensor;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Shelly door/window sensor (version 1).
 *
 * @author David Meier
 * @since 0.1.0
 */
public class ShellyDoorWindow extends DefaultMqttSubscriber {
  private static final Logger log = LoggerFactory.getLogger(ShellyDoorWindow.class);
  public static final String PREFIX = "shellydw-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shelly-door-window";

  private final String id;
  private final DefaultBatteryStateSensor batteryStateSensor;
  private final DefaultWindowSensor windowSensor;
  protected String baseTopic;

  /**
   * Constructor.
   *
   * @param id                the device id (MAC address)
   * @param displayName       the display name
   * @param eventPublisher    the event publisher
   * @param eventFactory      the event factory
   * @param customIdentifiers optional custom identifiers
   */
  public ShellyDoorWindow(String id,
                          String displayName,
                          EventPublisher eventPublisher,
                          EventFactory eventFactory,
                          Map<String, String> customIdentifiers
  ) {
    super(displayName, customIdentifiers);
    this.id = id;
    baseTopic = MQTT_TOPIC + id + "/sensor/";
    batteryStateSensor = new DefaultBatteryStateSensor(0, this, eventPublisher, eventFactory);
    windowSensor = new DefaultWindowSensor(1, this, true, eventPublisher, eventFactory);
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
          windowSensor.setIsOpen(true);
        } else if (message.equals("close")) {
          windowSensor.setIsOpen(false);
        }
      } else if (topic.endsWith("/battery")) {
        batteryStateSensor.setBatteryLevel(Integer.parseInt(message));
      }
      if (topic.endsWith("/tilt")) {
        windowSensor.setTiltAngleInDegree(parseInt(message));
      }
    });
  }

  @Override
  public List<? extends DeviceProperty> getDeviceProperties() {
    return List.of(batteryStateSensor, windowSensor);
  }
}
