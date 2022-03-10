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

package io.github.davemeier82.homeautomation.shelly.device.property;

import io.github.davemeier82.homeautomation.core.device.Device;
import io.github.davemeier82.homeautomation.core.device.property.AbstractRelay;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;

public class ShellyRelay extends AbstractRelay {
  private final String topic;
  private final MqttClient mqttClient;

  public ShellyRelay(long id,
                     Device device,
                     String topic,
                     EventPublisher eventPublisher,
                     EventFactory eventFactory,
                     MqttClient mqttClient
  ) {
    super(id, device, eventPublisher, eventFactory);
    this.topic = topic;
    this.mqttClient = mqttClient;
  }

  @Override
  public void turnOn() {
    mqttClient.publish(topic, "on");
  }

  @Override
  public void turnOff() {
    mqttClient.publish(topic, "off");
  }

}
