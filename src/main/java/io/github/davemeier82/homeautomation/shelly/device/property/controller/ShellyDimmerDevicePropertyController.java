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
import io.github.davemeier82.homeautomation.core.device.property.DimmerDevicePropertyController;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory;

import java.util.Set;

import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.SHELLY_DIMMER;


public class ShellyDimmerDevicePropertyController implements DimmerDevicePropertyController {
  private final MqttClient mqttClient;


  public ShellyDimmerDevicePropertyController(MqttClient mqttClient) {
    this.mqttClient = mqttClient;
  }

  private static String createTopic(DevicePropertyId devicePropertyId) {
    return ShellyTopicFactory.createCommandTopic(devicePropertyId, "light");
  }

  @Override
  public Set<? extends DeviceType> getSupportedDeviceTypes() {
    return Set.of(SHELLY_DIMMER);
  }

  @Override
  public void settDimmingLevel(DevicePropertyId devicePropertyId, Integer dimmingLevelInPercent) {
    boolean on = dimmingLevelInPercent > 0;
    mqttClient.publish(createTopic(devicePropertyId), "{\"brightness\": " + dimmingLevelInPercent + ", \"turn\": \"" + (on ? "on" : "off") + "\"}");
  }
}