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

import io.github.davemeier82.homeautomation.core.device.DeviceId;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.updater.RelayStateValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Optional;

import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.devicePropertyIdFromSubTopic;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Shelly1MessageProcessor implements ShellyDeviceMessageProcessor {
  private static final Logger log = LoggerFactory.getLogger(Shelly1MessageProcessor.class);
  private static final String DISPLAY_NAME = "Relay";
  private final RelayStateValueUpdateService relayStateValueUpdateService;


  public Shelly1MessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService) {
    this.relayStateValueUpdateService = relayStateValueUpdateService;
  }

  @Override
  public ShellyDeviceType getSupportedDeviceType() {
    return ShellyDeviceType.SHELLY_1;
  }

  @Override
  public void processMessage(String subTopic, Optional<ByteBuffer> payload, DeviceId deviceId, String devicePropertyType) {
    payload.ifPresent(byteBuffer -> {
      DevicePropertyId devicePropertyId = new DevicePropertyId(deviceId, devicePropertyIdFromSubTopic(subTopic).orElseThrow());
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", devicePropertyId, message);
      if (!subTopic.contains("command")) {
        if ("off".equalsIgnoreCase(message)) {
          updateValue(false, devicePropertyId);
        } else if ("on".equalsIgnoreCase(message)) {
          updateValue(true, devicePropertyId);
        }
      }
    });
  }

  private void updateValue(boolean isOn, DevicePropertyId devicePropertyId) {
    relayStateValueUpdateService.setValue(isOn, OffsetDateTime.now(), devicePropertyId, devicePropertyId.deviceId().toString() + ": " + DISPLAY_NAME);
  }
}
