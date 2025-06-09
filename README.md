# Qubership Testing Platform Diameter Transport Library

## Purpose
Diameter Transport Library is designed to send and receive messages via Diameter protocol.
It is used by Qubership Testing Platform ITF-Executor Service.

## Functionality description

- The library provides Diameter Outbound Synchronous Transport implementation only.
- So, it establishes connection according configuration settings, then sends messages of special format, waits a response, receives it and parses.
- TCP and SCTP transport layers (behind Diameter protocol) are supported.
    - SCTP layer support is available on Unix only, and depends on 3rd party installed on OS.
- Messages can be configured 2 formats
    - XML format. XML message is encoded by the library into binary (byte[]) before sending,
    - and text format like Wireshark text representation of SS7 protocol messages. In that case, text message is encoded by the library into binary (byte[]) before sending
- Responses are decoded back from binary format into XML format
- Encoders and Decoders handle the following Diameter message types:
    - CER (Capabilities-Exchange-Request) and the corresponding answer CEA (Capabilities-Exchange-Answer)
    - CCR (Credit-Control-Request) and the corresponding answer CCA (Credit-Control-Answer)
    - DWR (Device-Watchdog-Request) and the corresponding answer DWA (Device-Watchdog-Answer)
    - DPR (Disconnect-Peer-Request) and the corresponding answer DPA (Disconnect-Peer-Answer)
    - RAR (Re-Auth-Request) and the corresponding answer RAA (Re-Auth-Answer)
    - SLR (Spending-Limit-Request) and the corresponding answer SLA (Spending-Limit-Answer)
    - STR (Session-Termination-Request) and the corresponding answer STA (Session-Termination-Answer)
    - SNR (Status-Notification-Request) and the corresponding answer SNA (Status-Notification-Answer)
    - ACR (Accounting-C-Request) and the corresponding answer ACA (Accounting-C-Answer)
    - ASR (Abort-Session-Request) and the corresponding answer ASA (Abort-Session-Answer)
    - AAR (Auth-Application-Request) and the corresponding answer AAA (Auth-Application-Answer)
- The Library has Connections Holder - cache of connections with expiration settings configured. 
  - So, a connection is established at the 1st use, but isn't closed just after a testcase is completed. Instead, it remains alive to handle further requests/responses.

## Diameter Transport configuration properties

- Remote Server Host name or IP-address
- Remote Server Port number
- Diameter Dictionary Path
- Diameter Dictionary Type (Qubership Diameter or Marben)
- Wait response or not? (true/false) 
- Wait Response Timeout (millis)
- Connection type (TCP or SCTP)
- [For specific requests] Type of expected response: DWA, CCA, CEA, ASA, RAA, SLA, STA
- Messages format (XML or Wireshark)
- Diameter Session Id
- Send DPR or not? (true/false)
- Service Templates Links
  - Link to CER message Template
  - Link to DWA message Template
  - Link to DPR message Template
  - Link to DPA message Template
- Extra properties

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

In Qubership Testing Platform ITF-Executor Service, there is special transport module - **mockingbird-transport-diameter** - which uses this artifact.
In this module, there is a class DiameterOutboundTransport, which contains:
- Diameter Transport Configuration Parameters Descriptions (see above for transport configuration properties list),
- sendReceiveSync() method as a main entry point, performing:
  - request sending (connection is established if necessary),
  - and waiting a response,
- extra service methods.

