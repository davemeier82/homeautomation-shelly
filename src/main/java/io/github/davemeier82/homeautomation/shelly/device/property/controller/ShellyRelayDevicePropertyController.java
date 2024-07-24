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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.device.DeviceType;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.device.property.RelayDevicePropertyController;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import io.github.davemeier82.homeautomation.shelly.ShellyRpc;

import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.createCommandTopic;
import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.createRpcTopic;
import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.*;

public class ShellyRelayDevicePropertyController implements RelayDevicePropertyController {
  private final MqttClient mqttClient;
  private final ObjectMapper objectMapper;


  public ShellyRelayDevicePropertyController(MqttClient mqttClient, ObjectMapper objectMapper) {
    this.mqttClient = mqttClient;
    this.objectMapper = objectMapper;
  }

  private static String createTopic(DevicePropertyId devicePropertyId) {
    return createCommandTopic(devicePropertyId, devicePropertyId.deviceId().type() == SHELLY_DIMMER ? "light" : "relay");
  }

  @Override
  public Set<? extends DeviceType> getSupportedDeviceTypes() {
    return Set.of(SHELLY_1, SHELLY_2, SHELLY_25, SHELLY_DIMMER, SHELLY_1_MINI_GEN3);
  }

  @Override
  public void turnOn(DevicePropertyId devicePropertyId) {
    if (devicePropertyId.deviceId().type().equals(SHELLY_1_MINI_GEN3)) {
      String rpcTopic = createRpcTopic(devicePropertyId);
      mqttClient.publish(rpcTopic, createRpcMessage(rpcTopic, true));
    } else {
      mqttClient.publish(createTopic(devicePropertyId), "on");
    }
  }

  @Override
  public void turnOff(DevicePropertyId devicePropertyId) {
    if (devicePropertyId.deviceId().type().equals(SHELLY_1_MINI_GEN3)) {
      String rpcTopic = createRpcTopic(devicePropertyId);
      mqttClient.publish(rpcTopic, createRpcMessage(rpcTopic, false));
    } else {
      mqttClient.publish(createTopic(devicePropertyId), "off");
    }
  }

  private String createRpcMessage(String rpcTopic, boolean on) {
    try {
      return objectMapper.writeValueAsString(new ShellyRpc(UUID.randomUUID().toString(), rpcTopic + "/response", "Switch.Set", Map.of("id", 0, "on", on)));
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
