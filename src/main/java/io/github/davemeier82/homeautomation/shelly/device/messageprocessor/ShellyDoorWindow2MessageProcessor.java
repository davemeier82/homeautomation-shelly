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

import io.github.davemeier82.homeautomation.core.updater.BatteryLevelUpdateService;
import io.github.davemeier82.homeautomation.core.updater.WindowStateValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.WindowTiltAngleValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;

public class ShellyDoorWindow2MessageProcessor extends ShellyDoorWindowMessageProcessor {
  public ShellyDoorWindow2MessageProcessor(WindowStateValueUpdateService windowStateValueUpdateService,
                                           WindowTiltAngleValueUpdateService windowTiltAngleValueUpdateService,
                                           BatteryLevelUpdateService batteryLevelUpdateService
  ) {
    super(windowStateValueUpdateService, windowTiltAngleValueUpdateService, batteryLevelUpdateService);
  }

  @Override
  public ShellyDeviceType getSupportedDeviceType() {
    return ShellyDeviceType.SHELLY_DOOR_WINDOW_2;
  }
}
