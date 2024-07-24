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
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.getByTypeTopicPrefix;

public final class ShellyTopicFactory {

  public static final String ROOT_TOPIC = "shellies/";
  private static final Pattern DEVICE_ID_PATTERN = Pattern.compile("^" + ROOT_TOPIC + "(\\w*)-(\\w*)/");
  private static final Pattern DEVICE_TYPE_PATTERN = Pattern.compile(DEVICE_ID_PATTERN + "(\\w*)/");
  private static final Pattern SUBTOPIC_PATTERN_WITHOUT_INDEX = Pattern.compile(DEVICE_TYPE_PATTERN + "(\\S*)");
  private static final Pattern RPC_EVENT_PATTERN = Pattern.compile(DEVICE_ID_PATTERN + "events/rpc/?(\\w*)");

  private ShellyTopicFactory() {
  }

  public static Optional<DeviceId> deviceIdFromTopic(String topic) {
    Matcher matcher = DEVICE_TYPE_PATTERN.matcher(topic);
    if (matcher.find()) {
      return getByTypeTopicPrefix(matcher.group(1)).map(type -> new DeviceId(matcher.group(2), type));
    }
    return Optional.empty();
  }

  public static Optional<String> devicePropertyIdFromSubTopic(String subTopic) {
    if (StringUtils.hasLength(subTopic)) {
      int index = subTopic.indexOf("/");
      if (index < 0) {
        return Optional.of(subTopic);
      }
      if (index > 0) {
        return Optional.of(subTopic.substring(0, index));
      }
    }
    return Optional.empty();
  }

  public static Optional<String> subTopicOf(String topic) {
    Matcher rpcMatcher = RPC_EVENT_PATTERN.matcher(topic);
    if (rpcMatcher.find()) {
      return Optional.of(rpcMatcher.group(3));
    }
    Matcher matcher = SUBTOPIC_PATTERN_WITHOUT_INDEX.matcher(topic);
    if (matcher.find()) {
      return Optional.of(matcher.group(4));
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

  public static String createSetTopic(DevicePropertyId devicePropertyId, String propertyTyp) {
    return createTopic(devicePropertyId, propertyTyp) + "/set";
  }

  public static String createRpcTopic(DevicePropertyId devicePropertyId) {
    DeviceId deviceId = devicePropertyId.deviceId();
    ShellyDeviceType type = (ShellyDeviceType) deviceId.type();
    return ROOT_TOPIC + type.getTypeTopicPrefix() + deviceId.id() + "/rpc";
  }

}
