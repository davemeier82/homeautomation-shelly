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
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.*;
import static io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType.SHELLY_HT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ShellyTopicFactoryTest {

  @Test
  public void extractDevicePropertyId() {
    String devicePropertyId = devicePropertyIdFromSubTopic(subTopicOf("shellies/shellyht-E01234/sensor/temperature").get()).orElseThrow();
    assertEquals("temperature", devicePropertyId);
    devicePropertyId = devicePropertyIdFromSubTopic(subTopicOf("shellies/shellyht-E01234/relay/0/temperature").get()).orElseThrow();
    assertEquals("0", devicePropertyId);
    devicePropertyId = devicePropertyIdFromSubTopic(subTopicOf("shellies/shelly1-E01234/relay/0").get()).orElseThrow();
    assertEquals("0", devicePropertyId);
    devicePropertyId = devicePropertyIdFromSubTopic(subTopicOf("shellies/shellyswitch25-E01234/roller/0/pos").get()).orElseThrow();
    assertEquals("0", devicePropertyId);
    devicePropertyId = devicePropertyIdFromSubTopic(subTopicOf("shellies/shellyswitch-E01234/roller/0/pos").get()).orElseThrow();
    assertEquals("0", devicePropertyId);
    assertFalse(devicePropertyIdFromSubTopic(subTopicOf("shellies/shelly1minig3-1234567abcde/events/rpc").get()).isPresent());
  }

  @Test
  public void extractDeviceId() {
    DeviceId deviceId = deviceIdFromTopic("shellies/shellyht-E01234/sensor/temperature").orElseThrow();
    assertEquals(new DeviceId("E01234", SHELLY_HT), deviceId);
  }

  @Test
  public void extractSubTopic() {
    Optional<String> subtopic = subTopicOf("shellies/shellyht-E01234/relay/0/temperature");
    assertEquals(Optional.of("0/temperature"), subtopic);
    subtopic = subTopicOf("shellies/shellyht-E01234/sensor/temperature");
    assertEquals(Optional.of("temperature"), subtopic);
    subtopic = subTopicOf("shellies/shelly1minig3-1234567abcde/events/rpc");
    assertEquals(Optional.of(""), subtopic);
    subtopic = subTopicOf("shellies/shelly1minig3-1234567abcde/events/rpc/response");
    assertEquals(Optional.of("response"), subtopic);
  }
}