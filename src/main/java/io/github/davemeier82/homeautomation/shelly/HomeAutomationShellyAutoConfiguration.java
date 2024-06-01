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

import io.github.davemeier82.homeautomation.core.mqtt.MqttClient;
import io.github.davemeier82.homeautomation.core.repositories.DeviceRepository;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceFactory;
import io.github.davemeier82.homeautomation.shelly.device.ShellyDeviceTypeFactory;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.ShellyDeviceMessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.property.controller.ShellyRelayDevicePropertyController;
import io.github.davemeier82.homeautomation.shelly.device.property.controller.ShellyRollerDevicePropertyController;
import io.github.davemeier82.homeautomation.shelly.device.property.controller.ShellyDimmerDevicePropertyController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class HomeAutomationShellyAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  ShellyDeviceFactory shellyDeviceFactory() {
    return new ShellyDeviceFactory();
  }


  @Bean
  @ConditionalOnMissingBean
  ShellyDeviceTypeFactory shellyDeviceTypeFactory() {
    return new ShellyDeviceTypeFactory();
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean({ShellyDeviceFactory.class, DeviceRepository.class})
  ShellyMqttSubscriber shellyMqttSubscriber(ShellyDeviceFactory shellyDeviceFactory, DeviceRepository deviceRepository, Set<ShellyDeviceMessageProcessor> shellyDeviceMessageProcessors) {
    return new ShellyMqttSubscriber(shellyDeviceFactory, deviceRepository, shellyDeviceMessageProcessors);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(MqttClient.class)
  ShellyRollerDevicePropertyController shellyRollerDevicePropertyController(MqttClient mqttClient) {
    return new ShellyRollerDevicePropertyController(mqttClient);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(MqttClient.class)
  ShellyDimmerDevicePropertyController shellyDimmerDevicePropertyController(MqttClient mqttClient) {
    return new ShellyDimmerDevicePropertyController(mqttClient);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(MqttClient.class)
  ShellyRelayDevicePropertyController shellyRelayDevicePropertyController(MqttClient mqttClient) {
    return new ShellyRelayDevicePropertyController(mqttClient);
  }
}
