/*
 * Copyright 2021-2021 the original author or authors.
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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.device.DeviceId;
import io.github.davemeier82.homeautomation.core.device.mqtt.MqttDeviceFactory;
import io.github.davemeier82.homeautomation.core.device.mqtt.MqttSubscriber;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import io.github.davemeier82.homeautomation.shelly.device.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ShellyMqttDeviceFactory implements MqttDeviceFactory {
  private static final Logger log = LoggerFactory.getLogger(ShellyMqttDeviceFactory.class);
  private static final String ROOT_TOPIC = "shellies/";
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final MqttClient mqttClient;
  private final ObjectMapper objectMapper;
  private static final Set<String> supportedDeviceTypes = Set.of(
      Shelly1.TYPE,
      Shelly2.TYPE,
      Shelly25.TYPE,
      ShellyDimmer.TYPE,
      ShellyHT.TYPE,
      ShellyDoorWindow.TYPE,
      ShellyDoorWindow2.TYPE
  );

  public ShellyMqttDeviceFactory(EventPublisher eventPublisher, EventFactory eventFactory, MqttClient mqttClient, ObjectMapper objectMapper) {
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
    this.mqttClient = mqttClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public String getRootTopic() {
    return ROOT_TOPIC;
  }

  @Override
  public Optional<DeviceId> getDeviceId(String topic) {
    Optional<String> id = getIdFromTopic(topic);
    Optional<String> type = getTypeFromTopic(topic);
    if (id.isEmpty() || type.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(new DeviceId(id.get(), type.get()));
  }

  @Override
  public Optional<MqttSubscriber> createMqttSubscriber(DeviceId deviceId) {
    try {
      return Optional.of(createDevice(deviceId.type(), deviceId.id(), deviceId.toString(), Map.of(), Map.of()));
    } catch (IllegalArgumentException e) {
      log.debug("unknown device with id: {}", deviceId);
      return Optional.empty();
    }
  }

  @Override
  public boolean supportsDeviceType(String type) {
    return supportedDeviceTypes.contains(type);
  }

  @Override
  public Set<String> getSupportedDeviceTypes() {
    return supportedDeviceTypes;
  }

  @Override
  public MqttSubscriber createDevice(String type,
                                     String id,
                                     String displayName,
                                     Map<String, String> parameters,
                                     Map<String, String> customIdentifiers
  ) {
    MqttSubscriber device;
    if (Shelly1.TYPE.equalsIgnoreCase(type)) {
      device = createShelly1(id, displayName, customIdentifiers);
    } else if (Shelly2.TYPE.equalsIgnoreCase(type)) {
      device = createShelly2(id, displayName, customIdentifiers);
    } else if (Shelly25.TYPE.equalsIgnoreCase(type)) {
      device = createShelly25(id, displayName, customIdentifiers);
    } else if (ShellyHT.TYPE.equalsIgnoreCase(type)) {
      device = createShellyHT(id, displayName, customIdentifiers);
    } else if (ShellyDoorWindow.TYPE.equalsIgnoreCase(type)) {
      device = createShellyDoorWindow(id, displayName, customIdentifiers);
    } else if (ShellyDoorWindow2.TYPE.equalsIgnoreCase(type)) {
      device = createShellyDoorWindow2(id, displayName, customIdentifiers);
    } else if (ShellyDimmer.TYPE.equalsIgnoreCase(type)) {
      device = createShellyDimmer(id, displayName, customIdentifiers);
    } else {
      throw new IllegalArgumentException("device type '" + type + "' not supported");
    }
    mqttClient.subscribe(device.getTopic(), device::processMessage);
    eventPublisher.publishEvent(eventFactory.createNewDeviceCreatedEvent(device));
    return device;
  }

  private Optional<String> getIdFromTopic(String topic) {
    int endIndex = topic.indexOf('/', ROOT_TOPIC.length());
    if (endIndex < 0) {
      return Optional.empty();
    }
    String[] typeAndId = topic.substring(0, endIndex).split("-");
    if (typeAndId.length != 2) {
      return Optional.empty();
    }
    return Optional.of(typeAndId[1]);
  }

  private Optional<String> getTypeFromTopic(String topic) {
    String subtopic = topic.substring(ROOT_TOPIC.length());
    String type = null;
    if (subtopic.startsWith(ShellyDoorWindow.PREFIX)) {
      type = ShellyDoorWindow.TYPE;
    } else if (subtopic.startsWith(ShellyDoorWindow2.PREFIX)) {
      type = ShellyDoorWindow2.TYPE;
    } else if (subtopic.startsWith(Shelly1.PREFIX)) {
      type = Shelly1.TYPE;
    } else if (subtopic.startsWith(Shelly2.PREFIX)) {
      type = Shelly2.TYPE;
    } else if (subtopic.startsWith(Shelly25.PREFIX)) {
      type = Shelly25.TYPE;
    } else if (subtopic.startsWith(ShellyHT.PREFIX)) {
      type = ShellyHT.TYPE;
    } else if (subtopic.startsWith(ShellyDimmer.PREFIX)) {
      type = ShellyDimmer.TYPE;
    } else {
      log.debug("unknown device with topic {}", topic);
    }
    return Optional.ofNullable(type);
  }

  private ShellyDoorWindow2 createShellyDoorWindow2(String id, String displayName, Map<String, String> customIdentifiers) {
    log.debug("creating Shelly Door/Window 2 with id {} ({})", id, displayName);
    return new ShellyDoorWindow2(id, displayName, eventPublisher, eventFactory, customIdentifiers);
  }

  private ShellyDoorWindow createShellyDoorWindow(String id, String displayName, Map<String, String> customIdentifiers) {
    log.debug("creating Shelly Door/Window with id {} ({})", id, displayName);
    return new ShellyDoorWindow(id, displayName, eventPublisher, eventFactory, customIdentifiers);
  }

  private ShellyHT createShellyHT(String id, String displayName, Map<String, String> customIdentifiers) {
    log.debug("creating Shelly H&T with id {} ({})", id, displayName);
    return new ShellyHT(id, displayName, eventPublisher, eventFactory, customIdentifiers);
  }

  private Shelly25 createShelly25(String id, String displayName, Map<String, String> customIdentifiers) {
    log.debug("creating Shelly 2.5 with id {} ({})", id, displayName);
    return new Shelly25(id, displayName, mqttClient, eventPublisher, eventFactory, customIdentifiers);
  }

  private Shelly2 createShelly2(String id, String displayName, Map<String, String> customIdentifiers) {
    log.debug("creating Shelly 2 with id {} ({})", id, displayName);
    return new Shelly2(id, displayName, mqttClient, eventPublisher, eventFactory, customIdentifiers);
  }

  private Shelly1 createShelly1(String id, String displayName, Map<String, String> customIdentifiers) {
    log.debug("creating Shelly 1 with id {} ({})", id, displayName);
    return new Shelly1(id, displayName, mqttClient, eventPublisher, eventFactory, customIdentifiers);
  }

  private ShellyDimmer createShellyDimmer(String id, String displayName, Map<String, String> customIdentifiers) {
    log.debug("creating Shelly Dimmer with id {} ({})", id, displayName);
    return new ShellyDimmer(id, displayName, mqttClient, eventPublisher, eventFactory, objectMapper, customIdentifiers);
  }
}
