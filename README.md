# homeautomation-shelly

This is an extension of the [homeautomation-spring-core](https://github.com/davemeier82/homeautomation-spring-core/blob/main/README.md) to add [Shelly devices](https://www.shelly.com/) support.

## Usage

Checkout the detailed usage in the Demo: [homeautomation-demo](https://github.com/davemeier82/homeautomation-demo/blob/main/README.md)

```xml

<project>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.github.davemeier82.homeautomation</groupId>
                <artifactId>homeautomation-bom</artifactId>
                <version>${homeautomation-bom.version}</version>
                <scope>import</scope>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>io.github.davemeier82.homeautomation</groupId>
            <artifactId>homeautomation-spring-core</artifactId>
        </dependency>
        <dependency>
            <groupId>io.github.davemeier82.homeautomation</groupId>
            <artifactId>homeautomation-shelly</artifactId>
        </dependency>
    </dependencies>
</project>
```

## Supported devices

| Device                         | Events                                     | Controller                                                    |
|--------------------------------|--------------------------------------------|---------------------------------------------------------------|
| Shelly 1                       | RelayState                                 | RelayDevicePropertyController (on / off)                      |
| Shelly 1 mini Gen3             | RelayState                                 | RelayDevicePropertyController (on / off)                      |
| Shelly 2                       | RelayState                                 | RelayDevicePropertyController (on / off)                      |
| Shelly 2.5                     | RelayState, RollerState, Power             | RelayDevicePropertyController, RollerDevicePropertyController |
| Shelly Dimmer (V1 and V2)      | RelayState, RollerState, Power             | RelayDevicePropertyController, RollerDevicePropertyController |
| Shelly Door Window (V1 and V2) | RelayState, DimmingLevel                   | RelayDevicePropertyController, DimmerDevicePropertyController |
| Shelly H&T (V1)                | WindowState, WindowTiltAngle, BatteryLevel | -                                                             |
