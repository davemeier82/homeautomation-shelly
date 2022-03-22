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
import io.github.davemeier82.homeautomation.core.device.property.AbstractRoller;
import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;
import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;

/**
 * Device property of a Shelly device that supports a roller mode.
 */
public class ShellyRoller extends AbstractRoller {

  private final String topic;
  private final MqttClient mqttClient;

  /**
   * Constructor.
   *
   * @param id             the device property id
   * @param device         the device
   * @param topic          the topic to which this device property should publish messages
   * @param eventPublisher the event publisher
   * @param eventFactory   the event factory
   * @param mqttClient     the MQTT client
   */
  public ShellyRoller(long id,
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

}
