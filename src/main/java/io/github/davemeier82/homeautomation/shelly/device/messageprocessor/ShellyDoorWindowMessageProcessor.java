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

import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.core.updater.BatteryLevelUpdateService;
import io.github.davemeier82.homeautomation.core.updater.WindowStateValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.WindowTiltAngleValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellyDoorWindowMessageProcessor implements ShellyDeviceMessageProcessor {
  private static final Logger log = LoggerFactory.getLogger(ShellyDoorWindowMessageProcessor.class);
  private final WindowStateValueUpdateService windowStateValueUpdateService;
  private final WindowTiltAngleValueUpdateService windowTiltAngleValueUpdateService;
  private final BatteryLevelUpdateService batteryLevelUpdateService;

  public ShellyDoorWindowMessageProcessor(WindowStateValueUpdateService windowStateValueUpdateService,
                                          WindowTiltAngleValueUpdateService windowTiltAngleValueUpdateService,
                                          BatteryLevelUpdateService batteryLevelUpdateService
  ) {
    this.windowStateValueUpdateService = windowStateValueUpdateService;
    this.windowTiltAngleValueUpdateService = windowTiltAngleValueUpdateService;
    this.batteryLevelUpdateService = batteryLevelUpdateService;
  }


  @Override
  public ShellyDeviceType getSupportedDeviceType() {
    return ShellyDeviceType.SHELLY_DOOR_WINDOW;
  }

  @Override
  public void processMessage(String subTopic, Optional<ByteBuffer> payload, DevicePropertyId devicePropertyId, String devicePropertyType) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", devicePropertyId, message);
      if ("state".equals(devicePropertyId.id())) {
        if (message.equals("open")) {
          windowStateValueUpdateService.setValue(true, OffsetDateTime.now(), devicePropertyId, "Window State");
        } else if (message.equals("close")) {
          windowStateValueUpdateService.setValue(false, OffsetDateTime.now(), devicePropertyId, "Window State");
        }
      } else if ("battery".equals(devicePropertyId.id())) {
        batteryLevelUpdateService.setValue(Integer.parseInt(message), OffsetDateTime.now(), devicePropertyId, "Battery Level");
      } else if ("tilt".equals(devicePropertyId.id())) {
        windowTiltAngleValueUpdateService.setValue(parseInt(message), OffsetDateTime.now(), devicePropertyId, "Tilt Angle");
      }
    });
  }

}
