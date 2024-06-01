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
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.*;
import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ShellyTopicFactoryTest {

  @Test
  public void extractDevicePropertyId() {
    DevicePropertyId devicePropertyId = devicePropertyIdFromTopic("shellies/shellyht-E01234/sensor/temperature").orElseThrow();
    assertEquals(devicePropertyId, new DevicePropertyId(new DeviceId("E01234", SHELLY_HT), "temperature"));
    devicePropertyId = devicePropertyIdFromTopic("shellies/shellyht-E01234/relay/0/temperature").orElseThrow();
    assertEquals(devicePropertyId, new DevicePropertyId(new DeviceId("E01234", SHELLY_HT), "0"));
    devicePropertyId = devicePropertyIdFromTopic("shellies/shelly1-E01234/relay/0").orElseThrow();
    assertEquals(devicePropertyId, new DevicePropertyId(new DeviceId("E01234", SHELLY_1), "0"));
    devicePropertyId = devicePropertyIdFromTopic("shellies/shellyswitch25-E01234/roller/0/pos").orElseThrow();
    assertEquals(devicePropertyId, new DevicePropertyId(new DeviceId("E01234", SHELLY_25), "0"));
    devicePropertyId = devicePropertyIdFromTopic("shellies/shellyswitch-E01234/roller/0/pos").orElseThrow();
    assertEquals(devicePropertyId, new DevicePropertyId(new DeviceId("E01234", SHELLY_2), "0"));
  }

  @Test
  public void extractDeviceId() {
    DeviceId deviceId = deviceIdFromTopic("shellies/shellyht-E01234/sensor/temperature").orElseThrow();
    assertEquals(deviceId, new DeviceId("E01234", SHELLY_HT));
  }

  @Test
  public void extractEmptySubTopic() {
    Optional<String> subtopic = subTopicOf("shellies/shellyht-E01234/sensor/temperature");
    assertTrue(subtopic.isEmpty());
  }

  @Test
  public void extractSubTopic() {
    Optional<String> subtopic = subTopicOf("shellies/shellyht-E01234/relay/0/temperature");
    assertEquals(Optional.of("temperature"), subtopic);
  }
}