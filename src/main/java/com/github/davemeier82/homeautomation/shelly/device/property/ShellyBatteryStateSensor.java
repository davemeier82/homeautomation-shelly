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
import com.github.davemeier82.homeautomation.core.device.property.BatteryStateSensor;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ShellyBatteryStateSensor implements BatteryStateSensor {
  private final long id;
  private final Device device;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final AtomicReference<DataWithTimestamp<Integer>> batteryLevel = new AtomicReference<>();

  public ShellyBatteryStateSensor(long id,
                                  Device device,
                                  EventPublisher eventPublisher,
                                  EventFactory eventFactory
  ) {
    this.id = id;
    this.device = device;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
  }

  public void setBatteryLevel(int batteryLevel) {
    DataWithTimestamp<Integer> newValue = new DataWithTimestamp<>(batteryLevel);
    DataWithTimestamp<Integer> previousValue = this.batteryLevel.getAndSet(newValue);
    if (previousValue == null || !previousValue.getValue().equals(batteryLevel)) {
      eventPublisher.publishEvent(eventFactory.createBatteryLevelChangedEvent(this, newValue));
    }
  }

  @Override
  public Optional<DataWithTimestamp<Integer>> batteryLevelInPercent() {
    return Optional.ofNullable(batteryLevel.get());
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
