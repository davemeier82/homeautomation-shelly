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

import java.util.Arrays;
import java.util.Optional;

public enum ShellyDeviceType implements DeviceType {
  SHELLY_1("shelly1", "shelly1"),
  SHELLY_2("shelly2", "shellyswitch"),
  SHELLY_25("shelly25", "shellyswitch25"),
  SHELLY_DIMMER("shellydimmer", "shellydimmer"),
  SHELLY_DIMMER_2("shellydimmer2", "shellydimmer2"),
  SHELLY_HT("shellyht", "shellyht"),
  SHELLY_DOOR_WINDOW("shelly-door-window", "shellydw"),
  SHELLY_DOOR_WINDOW_2("shelly-door-window-2", "shellydw2"),
  SHELLY_1_MINI_GEN3("shelly1-mini-gen3", "shelly1minig3");

  private final String typeName;
  private final String typeTopicPrefix;

  ShellyDeviceType(String typeName, String typeTopicPrefix) {
    this.typeName = typeName;
    this.typeTopicPrefix = typeTopicPrefix;
  }

  public static Optional<ShellyDeviceType> getByTypeTopicPrefix(String typeTopicPrefix) {
    return Arrays.stream(values()).filter(t -> t.typeTopicPrefix.equals(typeTopicPrefix)).findAny();
  }

  public static Optional<ShellyDeviceType> getByTypeName(String typeName) {
    return Arrays.stream(values()).filter(t -> t.typeName.equals(typeName)).findAny();
  }

  @Override
  public String getTypeName() {
    return typeName;
  }

  public String getTypeTopicPrefix() {
    return typeTopicPrefix + "-";
  }
}
