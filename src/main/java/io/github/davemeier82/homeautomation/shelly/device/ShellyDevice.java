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

package io.github.davemeier82.homeautomation.shelly.device;

import io.github.davemeier82.homeautomation.core.device.DeviceType;
import io.github.davemeier82.homeautomation.core.device.mqtt.AbstractDevice;

import java.util.Map;

public class ShellyDevice extends AbstractDevice {

  private final DeviceType deviceType;
  private final String id;

  public ShellyDevice(String id, DeviceType deviceType, String displayName, Map<String, String> customIdentifiers) {
    super(displayName, customIdentifiers);
    this.deviceType = deviceType;
    this.id = id;
  }

  @Override
  public DeviceType getType() {
    return deviceType;
  }

  @Override
  public String getId() {
    return id;
  }
}
