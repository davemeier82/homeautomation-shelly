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
import io.github.davemeier82.homeautomation.core.updater.PowerValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.RelayStateValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.RollerPositionValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.RollerStateValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.Set;

import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.devicePropertyIdFromSubTopic;
import static io.github.davemeier82.homeautomation.shelly.mapper.RollerStateMapper.rollerStateFrom;
import static java.lang.Double.parseDouble;
import static java.nio.charset.StandardCharsets.UTF_8;

public class Shelly25MessageProcessor implements ShellyDeviceMessageProcessor {

  private static final Logger log = LoggerFactory.getLogger(Shelly25MessageProcessor.class);
  private final RelayStateValueUpdateService relayStateValueUpdateService;
  private final RollerStateValueUpdateService rollerStateValueUpdateService;

  private final RollerPositionValueUpdateService rollerPositionValueUpdateService;

  private final PowerValueUpdateService powerValueUpdateService;

  public Shelly25MessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService,
                                  RollerStateValueUpdateService rollerStateValueUpdateService,
                                  RollerPositionValueUpdateService rollerPositionValueUpdateService,
                                  PowerValueUpdateService powerValueUpdateService
  ) {
    this.relayStateValueUpdateService = relayStateValueUpdateService;
    this.rollerStateValueUpdateService = rollerStateValueUpdateService;
    this.rollerPositionValueUpdateService = rollerPositionValueUpdateService;
    this.powerValueUpdateService = powerValueUpdateService;
  }

  @Override
  public Set<ShellyDeviceType> getSupportedDeviceTypes() {
    return Set.of(ShellyDeviceType.SHELLY_25);
  }

  @Override
  public void processMessage(String subTopic, Optional<ByteBuffer> payload, DeviceId deviceId, String devicePropertyType) {
    payload.ifPresent(byteBuffer -> {
      DevicePropertyId devicePropertyId = new DevicePropertyId(deviceId, devicePropertyIdFromSubTopic(subTopic).orElseThrow());
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", subTopic, message);
      if ("relay".equals(devicePropertyType) && subTopic == null) {
        changeStateOfRelay(devicePropertyId, message);
      } else if ("roller".equals(devicePropertyType)) {
        processRollerMessage(subTopic, message, devicePropertyId);
      } else if ("power".equals(devicePropertyType)) {
        processRelayPowerMessage(message, devicePropertyId);
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

  private void processRollerMessage(String subTopic, String message, DevicePropertyId devicePropertyId) {
    if ("0".equals(subTopic)) {
      rollerStateValueUpdateService.setValue(rollerStateFrom(message), OffsetDateTime.now(), devicePropertyId, devicePropertyId.deviceId().toString() + ": Roller State");
    } else if ("0/pos".equals(subTopic)) {
      rollerPositionValueUpdateService.setValue(Integer.parseInt(message), OffsetDateTime.now(), devicePropertyId, devicePropertyId.deviceId().toString() + ": Roller Position");
    }
  }

  private void processRelayPowerMessage(String message, DevicePropertyId devicePropertyId) {
    powerValueUpdateService.setValue(parseDouble(message), OffsetDateTime.now(), devicePropertyId, devicePropertyId.deviceId().toString() + ": Power");
  }
}
