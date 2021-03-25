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
import com.github.davemeier82.homeautomation.core.device.property.Dimmer;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ShellyDimmerRelay implements Dimmer {

  private final ShellyRelay relay;
  private final String dimmerTopic;
  private final AtomicReference<DataWithTimestamp<Integer>> brightness = new AtomicReference<>();

  public ShellyDimmerRelay(long id,
                           Device device,
                           String relayTopic,
                           String dimmerTopic, EventPublisher eventPublisher,
                           EventFactory eventFactory,
                           MqttClient mqttClient
  ) {
    this.dimmerTopic = dimmerTopic;
    relay = new ShellyRelay(id, device, relayTopic, eventPublisher, eventFactory, mqttClient);
  }

  @Override
  public long getId() {
    return relay.getId();
  }

  @Override
  public Device getDevice() {
    return relay.getDevice();
  }

  @Override
  public void setDimmingLevel(int percent) {
    boolean on = percent > 0;
    relay.getMqttClient().publish(dimmerTopic, "{\"brightness\": " + percent + ", \"turn\": \"" + on + "\"}");
  }

  public void setRelayStateTo(boolean on) {
    relay.setRelayStateTo(on);
  }

  public void setDimmingLevelInPercent(int levelInPercent) {
    if (brightness.get() == null || brightness.get().getValue() != levelInPercent) {
      DataWithTimestamp<Integer> newValue = new DataWithTimestamp<>(levelInPercent);
      brightness.set(newValue);
      relay.getEventPublisher().publishEvent(relay.getEventFactory().createDimmingLevelChangedEvent(this, newValue));
    }
  }

  @Override
  public Optional<DataWithTimestamp<Integer>> getDimmingLevelInPercent() {
    return Optional.ofNullable(brightness.get());
  }

  @Override
  public void turnOn() {
    relay.turnOn();
  }

  @Override
  public void turnOff() {
    relay.turnOff();
  }

  @Override
  public Optional<DataWithTimestamp<Boolean>> isOn() {
    return relay.isOn();
  }
}
