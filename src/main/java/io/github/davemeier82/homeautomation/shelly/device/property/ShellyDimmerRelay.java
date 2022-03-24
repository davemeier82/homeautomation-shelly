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
import io.github.davemeier82.homeautomation.core.device.property.AbstractDimmerRelay;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;

/**
 * Device property of a Shelly dimmer.
 *
 * @author David Meier
 * @since 0.1.0
 */
public class ShellyDimmerRelay extends AbstractDimmerRelay {

  private final String dimmerTopic;
  private final MqttClient mqttClient;

  /**
   * Constructor.
   *
   * @param id             the device property id
   * @param device         the device
   * @param relayTopic     the topic to which this device property should publish relay messages
   * @param dimmerTopic    the topic to which this device property should publish dimmer messages
   * @param eventPublisher the event publisher
   * @param eventFactory   the event factory
   * @param mqttClient     the MQTT client
   */
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
    mqttClient.publish(dimmerTopic, "{\"brightness\": " + percent + ", \"turn\": \"" + (on ? "on" : "off") + "\"}");
  }

}
