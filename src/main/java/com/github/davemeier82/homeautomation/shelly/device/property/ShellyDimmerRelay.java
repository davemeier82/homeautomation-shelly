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
import com.github.davemeier82.homeautomation.core.device.property.AbstractDimmerRelay;
import com.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import com.github.davemeier82.homeautomation.core.event.EventPublisher;
import com.github.davemeier82.homeautomation.core.mqtt.MqttClient;

public class ShellyDimmerRelay extends AbstractDimmerRelay {

  private final String dimmerTopic;
  private final MqttClient mqttClient;

  public ShellyDimmerRelay(long id,
                           Device device,
                           String relayTopic,
                           String dimmerTopic,
                           EventPublisher eventPublisher,
                           EventFactory eventFactory,
                           MqttClient mqttClient
  ) {
    super(new ShellyRelay(id, device, relayTopic, eventPublisher, eventFactory, mqttClient));
    this.dimmerTopic = dimmerTopic;
    this.mqttClient = mqttClient;
  }

  @Override
  public void setDimmingLevel(int percent) {
    boolean on = percent > 0;
    mqttClient.publish(dimmerTopic, "{\"brightness\": " + percent + ", \"turn\": \"" + on + "\"}");
  }

}
