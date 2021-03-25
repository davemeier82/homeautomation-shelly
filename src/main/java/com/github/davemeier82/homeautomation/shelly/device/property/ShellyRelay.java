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

package com.github.davemeier82.homeautomation.shelly.device.property;

import com.github.davemeier82.homeautomation.core.device.Device;
import com.github.davemeier82.homeautomation.core.device.property.Relay;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ShellyRelay implements Relay {
  private final long id;
  private final Device device;
  private final String topic;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final MqttClient mqttClient;
  private final AtomicReference<DataWithTimestamp<Boolean>> isOn = new AtomicReference<>();

  public ShellyRelay(long id,
                     Device device,
                     String topic, EventPublisher eventPublisher,
                     EventFactory eventFactory,
                     MqttClient mqttClient
  ) {
    this.id = id;
    this.device = device;
    this.topic = topic;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
    this.mqttClient = mqttClient;
  }

  public void setRelayStateTo(boolean on) {
    if (isOn.get() == null || isOn.get().getValue() != on) {
      DataWithTimestamp<Boolean> newValue = new DataWithTimestamp<>(on);
      isOn.set(newValue);
      eventPublisher.publishEvent(eventFactory.createRelayStateChangedEvent(this, newValue));
    }
  }

  @Override
  public void turnOn() {
    mqttClient.publish(topic, "on");
  }

  @Override
  public void turnOff() {
    mqttClient.publish(topic, "off");
  }

  @Override
  public Optional<DataWithTimestamp<Boolean>> isOn() {
    return Optional.ofNullable(isOn.get());
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public Device getDevice() {
    return device;
  }

  EventPublisher getEventPublisher() {
    return eventPublisher;
  }

  EventFactory getEventFactory() {
    return eventFactory;
  }

  MqttClient getMqttClient() {
    return mqttClient;
  }

}
