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

package io.github.davemeier82.homeautomation.shelly.device.messageprocessor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.device.DeviceId;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.updater.RelayStateValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.ShellyRpc;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.ZoneOffset.UTC;

public class Shelly1MiniGen3MessageProcessor implements ShellyDeviceMessageProcessor {
  private static final Logger log = LoggerFactory.getLogger(Shelly1MiniGen3MessageProcessor.class);
  private static final String DISPLAY_NAME = "Relay";
  private final RelayStateValueUpdateService relayStateValueUpdateService;
  private final ObjectMapper objectMapper;


  public Shelly1MiniGen3MessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService, ObjectMapper objectMapper) {
    this.relayStateValueUpdateService = relayStateValueUpdateService;
    this.objectMapper = objectMapper;
  }

  @Override
  public ShellyDeviceType getSupportedDeviceType() {
    return ShellyDeviceType.SHELLY_1_MINI_GEN3;
  }

  @Override
  public void processMessage(String subTopic, Optional<ByteBuffer> payload, DeviceId deviceId, String devicePropertyType) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", deviceId, message);
      try {
        ShellyRpc rpcMessage = objectMapper.readValue(message, ShellyRpc.class);
        if ("NotifyStatus".equals(rpcMessage.method())) {
          DevicePropertyId devicePropertyId = new DevicePropertyId(deviceId, "0");
          Map<String, Object> params = (Map<String, Object>) rpcMessage.params().get("switch:0");
          Boolean isOn = (Boolean) params.get("output");
          if (isOn != null) {
            double timestamp = (double) rpcMessage.params().get("ts");
            OffsetDateTime offsetDateTime = OffsetDateTime.ofInstant(Instant.ofEpochMilli(Math.round(timestamp * 1000d)), UTC);
            updateValue(isOn, offsetDateTime, devicePropertyId);
          }
        }
      } catch (JsonProcessingException e) {
        throw new UncheckedIOException(e);
      }
    });
  }

  private void updateValue(boolean isOn, OffsetDateTime time, DevicePropertyId devicePropertyId) {
    relayStateValueUpdateService.setValue(isOn, time, devicePropertyId, DISPLAY_NAME);
  }
}
