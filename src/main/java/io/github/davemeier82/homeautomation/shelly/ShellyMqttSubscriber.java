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

import io.github.davemeier82.homeautomation.core.device.Device;
import io.github.davemeier82.homeautomation.core.device.mqtt.MqttSubscriber;
import io.github.davemeier82.homeautomation.core.repositories.DeviceRepository;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceFactory;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceType;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.ShellyDeviceMessageProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static io.github.davemeier82.homeautomation.shelly.ShellyTopicFactory.*;
import static java.util.stream.Collectors.toMap;

public class ShellyMqttSubscriber implements MqttSubscriber {

  private static final Logger log = LoggerFactory.getLogger(ShellyMqttSubscriber.class);
  private final ShellyDeviceFactory shellyDeviceFactory;
  private final DeviceRepository deviceRepository;

  private final Map<ShellyDeviceType, ShellyDeviceMessageProcessor> messageProcessorByType;


  public ShellyMqttSubscriber(ShellyDeviceFactory shellyDeviceFactory, DeviceRepository deviceRepository, Set<ShellyDeviceMessageProcessor> shellyDeviceMessageProcessors) {
    this.shellyDeviceFactory = shellyDeviceFactory;
    this.deviceRepository = deviceRepository;
    messageProcessorByType = shellyDeviceMessageProcessors.stream().collect(toMap(ShellyDeviceMessageProcessor::getSupportedDeviceType, Function.identity()));
  }

  @Override
  public String getTopic() {
    return ROOT_TOPIC + "#";
  }

  @Override
  public void processMessage(String topic, Optional<ByteBuffer> payload) {
    devicePropertyIdFromTopic(topic).ifPresentOrElse(dpId -> {
      deviceRepository.getByDeviceId(dpId.deviceId()).orElseGet(() -> {
        Device newDevice = shellyDeviceFactory.createDevice(dpId.deviceId().type(), dpId.deviceId().id(), dpId.deviceId().toString(), Map.of(), Map.of()).orElseThrow();
        deviceRepository.save(newDevice);
        return newDevice;
      });
      String devicePropertyType = ShellyTopicFactory.devicePropertyType(topic).orElseThrow();
      ShellyDeviceMessageProcessor shellyDeviceMessageProcessor = messageProcessorByType.get((ShellyDeviceType) dpId.deviceId().type());
      if (shellyDeviceMessageProcessor == null) {
        log.error("no processor found for devicePropertyId={} and topic={}", dpId, topic);
      } else {
        shellyDeviceMessageProcessor.processMessage(subTopicOf(topic).orElse(null), payload, dpId, devicePropertyType);
      }
    }, () -> log.info("No devicePropertyId found in topic: {}", topic));

  }
}
