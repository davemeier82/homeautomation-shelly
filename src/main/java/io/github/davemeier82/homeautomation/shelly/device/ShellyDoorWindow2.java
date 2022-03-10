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

package io.github.davemeier82.homeautomation.shelly.device;

import io.github.davemeier82.homeautomation.core.event.EventPublisher;
import io.github.davemeier82.homeautomation.core.event.factory.EventFactory;

import java.util.Map;

public class ShellyDoorWindow2 extends ShellyDoorWindow {
  public static final String PREFIX = "shellydw2-";
  private static final String MQTT_TOPIC = "shellies/" + PREFIX;
  public static final String TYPE = "shelly-door-window-2";

  public ShellyDoorWindow2(String id,
                           String displayName,
                           EventPublisher eventPublisher,
                           EventFactory eventFactory,
                           Map<String, String> customIdentifiers
  ) {
    super(id, displayName, eventPublisher, eventFactory, customIdentifiers);
    baseTopic = MQTT_TOPIC + id + "/sensor/";
  }

  @Override
  public String getType() {
    return TYPE;
  }
}
