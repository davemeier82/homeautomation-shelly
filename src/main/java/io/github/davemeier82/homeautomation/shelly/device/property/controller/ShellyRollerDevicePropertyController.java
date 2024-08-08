/*
 * Copyright 2021-2024 the original author or authors.
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

package io.github.davemeier82.homeautomation.shelly.device.property.controller;

import io.github.davemeier82.homeautomation.core.device.DeviceType;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.device.property.RollerDevicePropertyController;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory;

import java.util.Set;

import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.SHELLY_2;
import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.SHELLY_25;

public class ShellyRollerDevicePropertyController implements RollerDevicePropertyController {
  private final MqttClient mqttClient;


  public ShellyRollerDevicePropertyController(MqttClient mqttClient) {
    this.mqttClient = mqttClient;
  }

  private static String createTopic(DevicePropertyId devicePropertyId) {
    return ShellyTopicFactory.createCommandTopic(devicePropertyId, "roller");
  }

  @Override
  public Set<? extends DeviceType> getSupportedDeviceTypes() {
    return Set.of(SHELLY_25, SHELLY_2);
  }

  @Override
  public void open(DevicePropertyId devicePropertyId) {
    mqttClient.publish(createTopic(devicePropertyId), "open");
  }

  @Override
  public void close(DevicePropertyId devicePropertyId) {
    mqttClient.publish(createTopic(devicePropertyId), "close");
  }

  @Override
  public void stop(DevicePropertyId devicePropertyId) {
    mqttClient.publish(createTopic(devicePropertyId), "stop");
  }

  @Override
  public void setPosition(DevicePropertyId devicePropertyId, int percentage) {
    mqttClient.publish(createTopic(devicePropertyId) + "/pos", String.valueOf(percentage));
  }
}
