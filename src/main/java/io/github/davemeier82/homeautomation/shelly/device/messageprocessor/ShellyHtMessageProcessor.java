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
import io.github.davemeier82.homeautomation.core.updater.HumidityValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.TemperatureValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.time.OffsetDateTime;
import java.util.Optional;

import static java.lang.Float.parseFloat;
import static java.lang.Integer.parseInt;
import static java.nio.charset.StandardCharsets.UTF_8;

public class ShellyHtMessageProcessor implements ShellyDeviceMessageProcessor {
  private static final Logger log = LoggerFactory.getLogger(ShellyHtMessageProcessor.class);
  private final TemperatureValueUpdateService temperatureValueUpdateService;
  private final HumidityValueUpdateService humidityValueUpdateService;
  private final BatteryLevelUpdateService batteryLevelUpdateService;

  public ShellyHtMessageProcessor(TemperatureValueUpdateService temperatureValueUpdateService, HumidityValueUpdateService humidityValueUpdateService,
                                  BatteryLevelUpdateService batteryLevelUpdateService
  ) {
    this.temperatureValueUpdateService = temperatureValueUpdateService;
    this.humidityValueUpdateService = humidityValueUpdateService;
    this.batteryLevelUpdateService = batteryLevelUpdateService;
  }


  @Override
  public ShellyDeviceType getSupportedDeviceType() {
    return ShellyDeviceType.SHELLY_HT;
  }

  @Override
  public void processMessage(String subTopic, Optional<ByteBuffer> payload, DevicePropertyId devicePropertyId, String devicePropertyType) {
    payload.ifPresent(byteBuffer -> {
      String message = UTF_8.decode(byteBuffer).toString();
      log.debug("{}: {}", devicePropertyId, message);
      if ("temperature".equals(devicePropertyId.id())) {
        temperatureValueUpdateService.setValue(parseFloat(message), OffsetDateTime.now(), devicePropertyId, "Temperature");
      } else if ("humidity".equals(devicePropertyId.id())) {
        humidityValueUpdateService.setValue(parseFloat(message), OffsetDateTime.now(), devicePropertyId, "Humidity");
      } else if ("battery".equals(devicePropertyId.id())) {
        batteryLevelUpdateService.setValue(parseInt(message), OffsetDateTime.now(), devicePropertyId, "Battery Level");
      }
    });
  }

}
