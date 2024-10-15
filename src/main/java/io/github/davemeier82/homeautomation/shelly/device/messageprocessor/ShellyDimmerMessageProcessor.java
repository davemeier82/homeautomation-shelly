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
import io.github.davemeier82.homeautomation.core.updater.DimmingLevelValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.RelayStateValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.devicePropertyIdFromSubTopic;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellyDimmerMessageProcessor implements ShellyDeviceMessageProcessor {
  private static final Logger log = LoggerFactory.getLogger(ShellyDimmerMessageProcessor.class);
  private final RelayStateValueUpdateService relayStateValueUpdateService;
  private final DimmingLevelValueUpdateService dimmingLevelValueUpdateService;
  private final ObjectMapper objectMapper;

  public ShellyDimmerMessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService, DimmingLevelValueUpdateService dimmingLevelValueUpdateService, ObjectMapper objectMapper) {
    this.relayStateValueUpdateService = relayStateValueUpdateService;
    this.dimmingLevelValueUpdateService = dimmingLevelValueUpdateService;
    this.objectMapper = objectMapper;
  }

  @Override
  public Set<ShellyDeviceType> getSupportedDeviceTypes() {
    return Set.of(ShellyDeviceType.SHELLY_DIMMER_2, ShellyDeviceType.SHELLY_DIMMER);
  }

  @Override
  public void processMessage(String subTopic, Optional<ByteBuffer> payload, DeviceId deviceId, String devicePropertyType) {
    payload.ifPresent(byteBuffer -> {
      DevicePropertyId devicePropertyId = new DevicePropertyId(deviceId, devicePropertyIdFromSubTopic(subTopic).orElseThrow());
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", subTopic, message);
      if (subTopic.endsWith("status")) {
        processStatusMessage(message, devicePropertyId);
      } else if ("light".equals(devicePropertyType)) {
        changeStateOfRelay(devicePropertyId, message);
      }
    });
  }

  private void changeStateOfRelay(DevicePropertyId devicePropertyId, String message) {
    if ("off".equalsIgnoreCase(message)) {
      updateRelayValue(false, devicePropertyId);
    } else if ("on".equalsIgnoreCase(message)) {
      updateRelayValue(true, devicePropertyId);
    }
  }

  private void updateRelayValue(boolean isOn, DevicePropertyId devicePropertyId) {
    relayStateValueUpdateService.setValue(isOn, OffsetDateTime.now(), devicePropertyId, devicePropertyId.deviceId().toString() + ": Relay" + devicePropertyId.id());
  }

  private void processStatusMessage(String message, DevicePropertyId devicePropertyId) {
    try {
      StatusMessage statusMessage = objectMapper.readValue(message, StatusMessage.class);
      boolean newOnState = statusMessage.ison;
      updateRelayValue(newOnState, devicePropertyId);
      DevicePropertyId dimmingLevelId = new DevicePropertyId(devicePropertyId.deviceId(), "1");
      dimmingLevelValueUpdateService.setValue(statusMessage.brightness, OffsetDateTime.now(), dimmingLevelId, devicePropertyId.deviceId().toString() + ": Brightness");
    } catch (JsonProcessingException e) {
      log.error("failed to unmarshall status message: {}", message, e);
    }
  }

  private static class StatusMessage {
    public boolean ison;
    public int brightness;
  }
}
