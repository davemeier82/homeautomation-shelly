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

package io.github.davemeier82.homeautomation.shelly;

import io.github.davemeier82.homeautomation.core.device.DeviceId;
import io.github.davemeier82.homeautomation.core.device.property.DevicePropertyId;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.getByTypeTopicPrefix;

public final class ShellyTopicFactory {

  public static final String ROOT_TOPIC = "shellies/";
  private static final Pattern DEVICE_TYPE_PATTERN = Pattern.compile("^" + ROOT_TOPIC + "(\\w*)-(\\w*)/(\\w*)/");
  private static final Pattern DEVICE_TYPE_PATTERN_WITH_INDEX = Pattern.compile(DEVICE_TYPE_PATTERN + "(\\d*)/(.*)$");
  private static final Pattern DEVICE_TYPE_PATTERN_WITHOUT_INDEX = Pattern.compile(DEVICE_TYPE_PATTERN + "(\\w*)");

  private ShellyTopicFactory() {
  }

  public static Optional<DeviceId> deviceIdFromTopic(String topic) {
    Matcher matcher = DEVICE_TYPE_PATTERN.matcher(topic);
    if (matcher.find()) {
      return getByTypeTopicPrefix(matcher.group(1)).map(type -> new DeviceId(matcher.group(2), type));
    }
    return Optional.empty();
  }

  public static Optional<DevicePropertyId> devicePropertyIdFromTopic(String topic) {
    Matcher matcher = DEVICE_TYPE_PATTERN_WITHOUT_INDEX.matcher(topic);
    if (matcher.find()) {
      return getByTypeTopicPrefix(matcher.group(1)).map(type -> new DeviceId(matcher.group(2), type)).map(deviceId -> new DevicePropertyId(deviceId, matcher.group(4)));
    }
    return Optional.empty();
  }

  public static Optional<String> subTopicOf(String topic) {
    Matcher matcher = DEVICE_TYPE_PATTERN_WITH_INDEX.matcher(topic);
    if (matcher.find()) {
      return Optional.of(matcher.group(5));
    }
    return Optional.empty();
  }

  public static Optional<String> devicePropertyType(String topic) {
    Matcher matcher = DEVICE_TYPE_PATTERN.matcher(topic);
    if (matcher.find()) {
      return Optional.of(matcher.group(3));
    }
    return Optional.empty();
  }

  public static String createTopic(DevicePropertyId devicePropertyId, String propertyTyp) {
    DeviceId deviceId = devicePropertyId.deviceId();
    ShellyDeviceType type = (ShellyDeviceType) deviceId.type();
    return ROOT_TOPIC + type.getTypeTopicPrefix() + deviceId.id() + "/" + propertyTyp + "/" + devicePropertyId.id();
  }

  public static String createCommandTopic(DevicePropertyId devicePropertyId, String propertyTyp) {
    return createTopic(devicePropertyId, propertyTyp) + "/command";
  }

}
