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
import com.github.davemeier82.homeautomation.core.device.property.WindowSensor;
import com.github.davemeier82.homeautomation.core.event.DataWithTimestamp;
import com.github.davemeier82.homeautomation.core.event.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public class ShellyWindowSensor implements WindowSensor {
  private final long id;
  private final Device device;
  private final EventPublisher eventPublisher;
  private final EventFactory eventFactory;
  private final AtomicReference<DataWithTimestamp<Boolean>> isOpen = new AtomicReference<>();
  private final AtomicReference<DataWithTimestamp<Integer>> tiltAngleInDegree = new AtomicReference<>();


  public ShellyWindowSensor(long id,
                            Device device,
                            EventPublisher eventPublisher,
                            EventFactory eventFactory
  ) {
    this.id = id;
    this.device = device;
    this.eventPublisher = eventPublisher;
    this.eventFactory = eventFactory;
  }

  @Override
  public long getId() {
    return id;
  }

  @Override
  public Device getDevice() {
    return device;
  }

  public void setIsOpen(boolean open) {
    DataWithTimestamp<Boolean> newValue = new DataWithTimestamp<>(open);
    DataWithTimestamp<Boolean> previousValue = isOpen.getAndSet(newValue);
    if (previousValue == null || !previousValue.getValue().equals(open)) {
      if (open) {
        eventPublisher.publishEvent(eventFactory.createWindowOpenedEvent(this, ZonedDateTime.now()));
      } else {
        eventPublisher.publishEvent(eventFactory.createWindowClosedEvent(this, ZonedDateTime.now()));
      }
    }
  }

  public void setTiltAngleInDegree(int angleInDegree) {
    if (angleInDegree != -1) {
      tiltAngleInDegree.set(new DataWithTimestamp<>(angleInDegree));
    }
  }

  @Override
  public Optional<DataWithTimestamp<Boolean>> isOpen() {
    return Optional.ofNullable(isOpen.get());
  }

  @Override
  public Optional<DataWithTimestamp<Integer>> getTiltAngleInDegree() {
    return Optional.ofNullable(tiltAngleInDegree.get());
  }

  @Override
  public boolean isTiltingSupported() {
    return true;
  }
}
