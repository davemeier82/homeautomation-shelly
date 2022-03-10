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
import io.github.davemeier82.homeautomation.core.device.property.defaults.DefaultHumiditySensor;
import io.github.davemeier82.homeautomation.core.device.property.defaults.DefaultTemperatureSensor;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellyHT extends DefaultMqttSubscriber {
  private static final Logger log = LoggerFactory.getLogger(ShellyHT.class);
  public static final String PREFIX = "shellyht-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shellyht";

  private final String id;
  private final String baseTopic;

  private final DefaultBatteryStateSensor batteryStateSensor;
  private final DefaultHumiditySensor humiditySensor;
  private final DefaultTemperatureSensor temperatureSensor;

  public ShellyHT(String id, String displayName, EventPublisher eventPublisher, EventFactory eventFactory, Map<String, String> customIdentifiers) {
    super(displayName, customIdentifiers);
    this.id = id;
    baseTopic = MQTT_TOPIC + id + "/sensor/";
    temperatureSensor = new DefaultTemperatureSensor(0, this, eventPublisher, eventFactory);
    humiditySensor = new DefaultHumiditySensor(1, this, eventPublisher, eventFactory);
    batteryStateSensor = new DefaultBatteryStateSensor(2, this, eventPublisher, eventFactory);
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
        temperatureSensor.setTemperatureInDegree(parseFloat(message));
      } else if (topic.endsWith("/humidity")) {
        humiditySensor.setRelativeHumidityInPercent(parseFloat(message));
      } else if (topic.endsWith("/battery")) {
        batteryStateSensor.setBatteryLevel(parseInt(message));
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
  public List<DeviceProperty> getDeviceProperties() {
    return List.of(temperatureSensor, humiditySensor, batteryStateSensor);
  }
}
