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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.davemeier82.homeautomation.core.updater.BatteryLevelUpdateService;
import io.github.davemeier82.homeautomation.core.updater.DimmingLevelValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.HumidityValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.PowerValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.RelayStateValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.RollerPositionValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.RollerStateValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.TemperatureValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.WindowStateValueUpdateService;
import io.github.davemeier82.homeautomation.core.updater.WindowTiltAngleValueUpdateService;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.Shelly1MessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.Shelly1MiniGen3MessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.Shelly25MessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.Shelly2MessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.ShellyDimmerMessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.ShellyDoorWindow2MessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.ShellyDoorWindowMessageProcessor;
import io.github.davemeier82.homeautomation.shelly.device.messageprocessor.ShellyHtMessageProcessor;
import io.github.davemeier82.homeautomation.spring.core.HomeAutomationCoreValueUpdateServiceAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AutoConfigureAfter({HomeAutomationCoreValueUpdateServiceAutoConfiguration.class, JacksonAutoConfiguration.class})
public class HomeAutomationShellyDeviceMessageProcessorAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(RelayStateValueUpdateService.class)
  Shelly1MessageProcessor shelly1MessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService) {
    return new Shelly1MessageProcessor(relayStateValueUpdateService);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean(RelayStateValueUpdateService.class)
  Shelly1MiniGen3MessageProcessor shelly1MiniGen3MessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService, ObjectMapper objectMapper) {
    return new Shelly1MiniGen3MessageProcessor(relayStateValueUpdateService, objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean({RelayStateValueUpdateService.class, RollerStateValueUpdateService.class, RollerPositionValueUpdateService.class, PowerValueUpdateService.class})
  Shelly2MessageProcessor shelly2MessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService,
                                                  RollerStateValueUpdateService rollerStateValueUpdateService,
                                                  RollerPositionValueUpdateService rollerPositionValueUpdateService,
                                                  PowerValueUpdateService powerValueUpdateService
  ) {
    return new Shelly2MessageProcessor(relayStateValueUpdateService, rollerStateValueUpdateService, rollerPositionValueUpdateService, powerValueUpdateService);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean({RelayStateValueUpdateService.class, RollerStateValueUpdateService.class, RollerPositionValueUpdateService.class, PowerValueUpdateService.class})
  Shelly25MessageProcessor shelly25MessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService,
                                                    RollerStateValueUpdateService rollerStateValueUpdateService,
                                                    RollerPositionValueUpdateService rollerPositionValueUpdateService,
                                                    PowerValueUpdateService powerValueUpdateService
  ) {
    return new Shelly25MessageProcessor(relayStateValueUpdateService, rollerStateValueUpdateService, rollerPositionValueUpdateService, powerValueUpdateService);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean({RelayStateValueUpdateService.class, DimmingLevelValueUpdateService.class, ObjectMapper.class})
  ShellyDimmerMessageProcessor shellyDimmerMessageProcessor(RelayStateValueUpdateService relayStateValueUpdateService,
                                                            DimmingLevelValueUpdateService dimmingLevelValueUpdateService,
                                                            ObjectMapper objectMapper
  ) {
    return new ShellyDimmerMessageProcessor(relayStateValueUpdateService, dimmingLevelValueUpdateService, objectMapper);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean({WindowStateValueUpdateService.class, WindowTiltAngleValueUpdateService.class, BatteryLevelUpdateService.class})
  ShellyDoorWindowMessageProcessor shellyDoorWindowMessageProcessor(WindowStateValueUpdateService windowStateValueUpdateService,
                                                                    WindowTiltAngleValueUpdateService windowTiltAngleValueUpdateService,
                                                                    BatteryLevelUpdateService batteryLevelUpdateService
  ) {
    return new ShellyDoorWindowMessageProcessor(windowStateValueUpdateService, windowTiltAngleValueUpdateService, batteryLevelUpdateService);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean({WindowStateValueUpdateService.class, WindowTiltAngleValueUpdateService.class, BatteryLevelUpdateService.class})
  ShellyDoorWindow2MessageProcessor shellyDoorWindow2MessageProcessor(WindowStateValueUpdateService windowStateValueUpdateService,
                                                                      WindowTiltAngleValueUpdateService windowTiltAngleValueUpdateService,
                                                                      BatteryLevelUpdateService batteryLevelUpdateService
  ) {
    return new ShellyDoorWindow2MessageProcessor(windowStateValueUpdateService, windowTiltAngleValueUpdateService, batteryLevelUpdateService);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnBean({TemperatureValueUpdateService.class, HumidityValueUpdateService.class, BatteryLevelUpdateService.class})
  ShellyHtMessageProcessor shellyHtMessageProcessor(TemperatureValueUpdateService temperatureValueUpdateService,
                                                    HumidityValueUpdateService humidityValueUpdateService,
                                                    BatteryLevelUpdateService batteryLevelUpdateService
  ) {
    return new ShellyHtMessageProcessor(temperatureValueUpdateService, humidityValueUpdateService, batteryLevelUpdateService);
  }
}
