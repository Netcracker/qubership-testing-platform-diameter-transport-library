# Qubership Testing Platform Diameter Transport Library

## Purpose
Diameter Transport Library is designed to send and receive messages via Diameter protocol.
It is used by Qubership Testing Platform ITF-Executor Service.

## Local build

In IntelliJ IDEA, one can select 'github' Profile in Maven Settings menu on the right, then expand Lifecycle dropdown of diameter-transport module, then select 'clean' and 'install' options and click 'Run Maven Build' green arrow button on the top.

Or, one can execute the command:
```bash
mvn -P github clean install
```

## How to add dependency into a service
```xml
    <!-- Change version number if necessary -->
    <dependency>
        <groupId>org.qubership.automation</groupId>
        <artifactId>diameter-transport</artifactId>
        <version>2.2.7-SNAPSHOT</version>
    </dependency>
```

