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
import com.github.davemeier82.homeautomation.core.device.property.Roller;
import com.github.davemeier82.homeautomation.core.device.property.RollerState;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ShellyRoller implements Roller {

  private final long id;
  private final Device device;
  private final String topic;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final MqttClient mqttClient;
  private final AtomicReference<DataWithTimestamp<RollerState>> state = new AtomicReference<>();
  private final AtomicReference<DataWithTimestamp<Integer>> position = new AtomicReference<>();

  public ShellyRoller(long id,
                      Device device,
                      String topic,
                      EventPublisher eventPublisher,
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

  public void setRelayStateTo(RollerState rollerState) {
    if (state.get() == null || state.get().getValue() != rollerState) {
      DataWithTimestamp<RollerState> newValue = new DataWithTimestamp<>(rollerState);
      state.set(newValue);
      eventPublisher.publishEvent(eventFactory.createRollerStateChangedEvent(this, newValue));
    }
  }

  public void setPositionInPercent(int positionInPercent) {
    if (position.get() == null || position.get().getValue() != positionInPercent) {
      DataWithTimestamp<Integer> newValue = new DataWithTimestamp<>(positionInPercent);
      position.set(newValue);
      eventPublisher.publishEvent(eventFactory.createRollerPositionChangedEvent(this, newValue));
    }
  }

  @Override
  public void open() {
    mqttClient.publish(topic, "open");
  }

  @Override
  public void close() {
    mqttClient.publish(topic, "close");
  }

  @Override
  public void stop() {
    mqttClient.publish(topic, "stop");
  }

  @Override
  public void setPosition(int percent) {
    mqttClient.publish(topic + "/pos", String.valueOf(percent));
  }

  @Override
  public Optional<DataWithTimestamp<Integer>> getPositionInPercent() {
    return Optional.ofNullable(position.get());
  }

  @Override
  public Optional<DataWithTimestamp<RollerState>> getState() {
    return Optional.ofNullable(state.get());
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public Device getDevice() {
    return device;
  }
}
