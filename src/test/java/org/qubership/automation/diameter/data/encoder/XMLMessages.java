/*
 * # Copyright 2024-2025 NetCracker Technology Corporation
 * #
 * # Licensed under the Apache License, Version 2.0 (the "License");
 * # you may not use this file except in compliance with the License.
 * # You may obtain a copy of the License at
 * #
 * #      http://www.apache.org/licenses/LICENSE-2.0
 * #
 * # Unless required by applicable law or agreed to in writing, software
 * # distributed under the License is distributed on an "AS IS" BASIS,
 * # WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * # See the License for the specific language governing permissions and
 * # limitations under the License.
 *
 */

package org.qubership.automation.diameter.data.encoder;

public class XMLMessages {

    public static final String CER = "<CER>\n" +
            "    <Origin-Host>some-address.our-company.com</Origin-Host>\n" +
            "    <Origin-Realm>our-company.com</Origin-Realm>\n" +
            "    <Host-IP-Address>10.235.35.38</Host-IP-Address>\n" +
            "    <Vendor-Id>0</Vendor-Id>\n" +
            "    <Product-Name>OurCompanySimulator</Product-Name>\n" +
            "    <Origin-State-Id>0</Origin-State-Id>\n" +
            "    <Auth-Application-Id>4</Auth-Application-Id>\n" +
            "    <Vendor-Specific-Application-Id>\n" +
            "        <Vendor-Id>10415</Vendor-Id>\n" +
            "        <Auth-Application-Id>4</Auth-Application-Id>\n" +
            "    </Vendor-Specific-Application-Id>\n" +
            "    <Supported-Vendor-Id>10415</Supported-Vendor-Id>\n" +
            "</CER>";

    public static final String RAA = "<RAA>\n" +
            "\t<Session-Id>363741392755</Session-Id>\n" +
            "\t<Result-Code>2002</Result-Code>\n" +
            "\t<Origin-Host>799988370452.our-company.com</Origin-Host>\n" +
            "\t<Origin-Realm>our-company.com</Origin-Realm>\n" +
            "\t<Origin-State-Id>10</Origin-State-Id>\n" +
            "</RAA>";

    public static final String CCR =
            "<CCR>\n" +
                "<Session-Id>MMS73789299846</Session-Id>\n" +
                "<Origin-Host>some-address.our-company.com</Origin-Host>\n" +
                "<Origin-Realm>our-company.com</Origin-Realm>\n" +
                "<Destination-Realm>our-company.com</Destination-Realm>\n" +
                "<NC-Service-Context-Id>32251@tgpp.org</NC-Service-Context-Id>\n" +
                "<NC-Roaming-Indicator>T</NC-Roaming-Indicator>\n" +
                "<NC-Originating-Country-Code>RUS</NC-Originating-Country-Code>\n" +
                "<Destination-Host>10.109.2.41</Destination-Host>\n" +
                "<Auth-Application-Id>4</Auth-Application-Id>\n" +
                "<Service-Context-Id>32251@3gpp.org</Service-Context-Id>\n" +
                "<Event-Timestamp>2021-11-30 00:38:28 +0000 UTC</Event-Timestamp>\n" +
                "<CC-Request-Type>1</CC-Request-Type>\n" +
                "<CC-Request-Number>0</CC-Request-Number>\n" +
                "<Subscription-Id>\n" +
                "<Subscription-Id-Type>0</Subscription-Id-Type>\n" +
                "<Subscription-Id-Data>3248073788</Subscription-Id-Data>\n" +
                "</Subscription-Id>\n" +
                "<Multiple-Services-Indicator>1</Multiple-Services-Indicator>\n" +
                "<Multiple-Services-Credit-Control>\n" +
                "<Rating-Group>1</Rating-Group>\n" +
                "<Requested-Service-Unit></Requested-Service-Unit>\n" +
                "</Multiple-Services-Credit-Control>\n" +
                "<Service-Information>\n" +
                "<PS-Information>\n" +
                "<TGPP-Charging-Id>10FD00F9</TGPP-Charging-Id>\n" +
                "<TGPP-PDP-Type>0</TGPP-PDP-Type>\n" +
                "<PDP-Address>10.255.227.53</PDP-Address>\n" +
                "<QoS-Information>\n" +
                "<Allocation-Retention-Priority>\n" +
                "<Priority-Level>6</Priority-Level>\n" +
                "<Pre-emption-Capability>1</Pre-emption-Capability>\n" +
                "<Pre-emption-Vulnerability>0</Pre-emption-Vulnerability>\n" +
                "</Allocation-Retention-Priority>\n" +
                "<QoS-Class-Identifier>8</QoS-Class-Identifier>\n" +
                "<APN-Aggregate-Max-Bitrate-UL>50000</APN-Aggregate-Max-Bitrate-UL>\n" +
                "<APN-Aggregate-Max-Bitrate-DL>100000</APN-Aggregate-Max-Bitrate-DL>\n" +
                "</QoS-Information>\n" +
                "<SGSN-Address>0x0001D5572800</SGSN-Address>\n" +
                "<GGSN-Address>0x0001D948E78A</GGSN-Address>\n" +
                "<CG-Address>10.106.250.84</CG-Address>\n" +
                "<TGPP-GGSN-MCC-MNC>20620</TGPP-GGSN-MCC-MNC>\n" +
                "<TGPP-SGSN-MCC-MNC>20620</TGPP-SGSN-MCC-MNC>\n" +
                "<TGPP-NSAPI>5</TGPP-NSAPI>\n" +
                "<Called-Station-Id>gprs.base.be</Called-Station-Id>\n" +
                "<TGPP-Selection-Mode>0</TGPP-Selection-Mode>\n" +
                "<TGPP-Charging-Characteristics>0100</TGPP-Charging-Characteristics>\n" +
                "<TGPP-MS-TimeZone>8001</TGPP-MS-TimeZone>\n" +
                "<TGPP-User-Location-Info>0102F60200FC0417</TGPP-User-Location-Info>\n" +
                "<TGPP-RAT-Type>01</TGPP-RAT-Type>\n" +
                "<PDN-Connection-ID>2850516313</PDN-Connection-ID>\n" +
                "<Dynamic-Address-Flag>1</Dynamic-Address-Flag>\n" +
                "<Charging-Characteristics-Selection-Mode>0</Charging-Characteristics-Selection-Mode>\n" +
                "<PDP-Context-Type>0</PDP-Context-Type>\n" +
                "<Serving-Node-Type>0</Serving-Node-Type>\n" +
                "</PS-Information>\n" +
                "</Service-Information>\n" +
            "</CCR>";

    public static final String SNA =
        "<SNA>" +
            "<Session-Id>Sy77265396269</Session-Id>" +
            "<Origin-Host>some-address.our-company.com</Origin-Host>"+
            "<Origin-Realm>our-company.com</Origin-Realm>" +
            "<Result-Code>2001</Result-Code>" +
        "</SNA>";

    public static final String SLR =
        "<SLR> " +
            "<Session-Id>Sy53485643696</Session-Id>" +
            "<Auth-Application-Id>16777302</Auth-Application-Id>" +
            "<Origin-Host>itf-service.some-address.our-company.com</Origin-Host>" +
            "<Origin-Realm>our-company.com</Origin-Realm>" +
            "<Destination-Host>10.109.2.41</Destination-Host>" +
            "<Destination-Realm>our-company.com</Destination-Realm>" +
            "<SL-Request-Type>0</SL-Request-Type>" +
            "<Subscription-Id>" +
                "<Subscription-Id-Type>0</Subscription-Id-Type>" +
                "<Subscription-Id-Data>3248508703</Subscription-Id-Data>" +
            "</Subscription-Id>" +
            "<Policy-Counter-Identifier>BASIC_PLAN_PCID</Policy-Counter-Identifier>  " +
        "</SLR>";

    public static final String STR =
            "<STR>\n" +
                "\t<Session-Id>Sy53485643696</Session-Id>\n" +
                "\t<Origin-Host>itf-service.some-address.our-company.com</Origin-Host>\n" +
                "\t<Origin-Realm>our-company.com</Origin-Realm>\n" +
                "\t<Destination-Realm>our-company.com</Destination-Realm>\n" +
                "\t<Auth-Application-Id>16777302</Auth-Application-Id>\t\n" +
                "\t<Termination-Cause>1</Termination-Cause>\n" +
                "\t<Destination-Host>10.109.2.41</Destination-Host>\n" +
            "</STR>";

    public static final String DWA =
            "<DWA>\n" +
                "<Origin-Host>$tc.Params.OriginHost</Origin-Host>\n" +
                "<Origin-Realm>our-company.com</Origin-Realm>\n" +
                "<Result-Code>2001</Result-Code>\n" +
            "</DWA>";

    public static final String CCA_WITH_VELOCITY_AND_FORMATTED_SESSION_ID_AVP =
            "#if($tc.OriginCountry.Zone eq 'Zone EU' || $tc.OriginCountry.Zone eq 'Belgium')\n" +
                "#set($tc.saved.CCTotalOctets = '26214400')\n" +
            "#else\n" +
                "#set($tc.saved.CCTotalOctets = $CCTotalOctets)\n" +
            "#end\n" +
            "\n" +
            "<CCA>\n" +
                "<Session-Id code=\"263\" vendor=\"0\">$tc.saved.SessionId</Session-Id>\n" +
                "<Origin-Host>cube.some-address.our-company.com</Origin-Host>\n" +
                    "<Multiple-Services-Credit-Control>\t\t\t\t\t\t\t\t\t\n" +
                        "<Rating-Group>$tc.saved.Rating-Group</Rating-Group>\n" +
                        "<Granted-Service-Unit>\n" +
                            "<CC-Total-Octets code=\"421\" vendor=\"0\">$tc.saved.CCTotalOctets</CC-Total-Octets>\n" +
                        "</Granted-Service-Unit>\n" +
                        "<Trigger>\n" +
                            "<Trigger-Type>CHANGE_IN_LOCATION</Trigger-Type>\n" +
                            "<Trigger-Type code=\"871\" vendor=\"10415\">CHANGE_IN_QOS_TRAFFIC_CLASS</Trigger-Type>\n" +
                        "</Trigger>\n" +
                    "</Multiple-Services-Credit-Control>\n" +
            "</CCA>";

    public static final String CCA_WITH_VELOCITY_AFTER_FORMAT_AVP =
            "#if($tc.OriginCountry.Zone eq 'Zone EU' || $tc.OriginCountry.Zone eq 'Belgium')\n" +
                "#set($tc.saved.CCTotalOctets = '26214400')\n" +
            "#else\n" +
                "#set($tc.saved.CCTotalOctets = $CCTotalOctets)\n" +
            "#end\n" +
            "\n" +
            "<CCA>\n" +
                "<Session-Id code=\"263\" vendor=\"0\">$tc.saved.SessionId</Session-Id>\n" +
                "<Origin-Host code=\"264\" vendor=\"0\">cube.some-address.our-company.com</Origin-Host>\n" +
                "<Multiple-Services-Credit-Control code=\"456\" vendor=\"0\">\t\t\t\t\t\t\t\t\t\n" +
                    "<Rating-Group code=\"432\" vendor=\"0\">$tc.saved.Rating-Group</Rating-Group>\n" +
                    "<Granted-Service-Unit code=\"431\" vendor=\"0\">\n" +
                        "<CC-Total-Octets code=\"421\" vendor=\"0\">$tc.saved.CCTotalOctets</CC-Total-Octets>\n" +
                    "</Granted-Service-Unit>\n" +
                    "<Trigger code=\"1264\" vendor=\"10415\">\n" +
                        "<Trigger-Type code=\"870\" vendor=\"10415\">CHANGE_IN_LOCATION</Trigger-Type>\n" +
                        "<Trigger-Type code=\"871\" vendor=\"10415\">CHANGE_IN_QOS_TRAFFIC_CLASS</Trigger-Type>\n" +
                    "</Trigger>\n" +
                "</Multiple-Services-Credit-Control>\n" +
            "</CCA>\n";

    public static final String CCA_WITHOUT_AVP_ATTRIBUTES =
            "#if($tc.OriginCountry.Zone eq 'Zone EU' || $tc.OriginCountry.Zone eq 'Belgium')\n" +
                "#set($tc.saved.CCTotalOctets = '26214400')\n" +
            "#else\n" +
                "#set($tc.saved.CCTotalOctets = $CCTotalOctets)\n" +
            "#end\n" +
            "\n" +
            "<CCA>\n" +
                "<Session-Id>$tc.saved.SessionId</Session-Id>\n" +
                "\t<Origin-Host>cube.some-address.our-company.com</Origin-Host>\n" +
                "<Multiple-Services-Credit-Control>\t\t\t\t\t\t\t\t\t\n" +
                    "<Rating-Group>$tc.saved.Rating-Group</Rating-Group>\n" +
                    "<Granted-Service-Unit>\n" +
                        "<CC-Total-Octets>$tc.saved.CCTotalOctets</CC-Total-Octets>\n" +
                    "</Granted-Service-Unit>\n" +
                    "<Trigger>\n" +
                        "<Trigger-Type>CHANGE_IN_LOCATION</Trigger-Type>\n" +
                        "<Trigger-Type>CHANGE_IN_QOS_TRAFFIC_CLASS</Trigger-Type>\n" +
                    "</Trigger>\n" +
                "</Multiple-Services-Credit-Control>\n" +
            "</CCA>";

    public static final String CCA_FORMATTED_TEMPLATE =
            "#if($tc.OriginCountry.Zone eq 'Zone EU' || $tc.OriginCountry.Zone eq 'Belgium')\n" +
                "#set($tc.saved.CCTotalOctets = '26214400')\n" +
            "#else\n" +
                "#set($tc.saved.CCTotalOctets = $CCTotalOctets)\n" +
            "#end\n" +
            "\n" +
            "<CCA>\n" +
                "<Session-Id code=\"263\" vendor=\"0\">$tc.saved.SessionId</Session-Id>\n" +
                "\t<Origin-Host code=\"264\" vendor=\"0\">cube.some-address.our-company.com</Origin-Host>\n" +
                "<Multiple-Services-Credit-Control code=\"456\" vendor=\"0\">\t\t\t\t\t\t\t\t\t\n" +
                    "<Rating-Group code=\"432\" vendor=\"0\">$tc.saved.Rating-Group</Rating-Group>\n" +
                    "<Granted-Service-Unit code=\"431\" vendor=\"0\">\n" +
                        "<CC-Total-Octets code=\"421\" vendor=\"0\">$tc.saved.CCTotalOctets</CC-Total-Octets>\n" +
                    "</Granted-Service-Unit>\n" +
                    "<Trigger code=\"1264\" vendor=\"10415\">\n" +
                        "<Trigger-Type code=\"870\" vendor=\"10415\">CHANGE_IN_LOCATION</Trigger-Type>\n" +
                        "<Trigger-Type code=\"870\" vendor=\"10415\">CHANGE_IN_QOS_TRAFFIC_CLASS</Trigger-Type>\n" +
                    "</Trigger>\n" +
                "</Multiple-Services-Credit-Control>\n" +
            "</CCA>";

    public static final String TEMPLATE_WITH_NOT_EXISTING_AVP =
            "<Trigger1212121212>\n" +
                "<Trigger-Type>CHANGE_IN_LOCATION</Trigger-Type>\n" +
                "<Trigger-Type code=\"871\" vendor=\"10415\">CHANGE_IN_QOS_TRAFFIC_CLASS</Trigger-Type>\n" +
            "</Trigger>";

    public static final String CCR_WITH_DESCRIPTION_ATTR_AND_OPENED_AVP_TAG =
            "<CCR Description=\"CCR-I\" Retransmit=\"true\" Service=\"To_DiameterPeer_User_dp_user2\">\t<Session-Id>$SessionId";

    public static final String CCR_WITH_DESCRIPTION_ATTR_AND_OPENED_AVP_TAG_WITH_WHITESPACE=
            "<CCR Description=\"CCR-I\" Retransmit=\"true\" Service=\"To_DiameterPeer_User_dp_user2\"> "
                    + "<Session-Id>$SessionId";

    public static final String CCR_WITH_DESCRIPTION_ATTR_AND_WITHOUT_END_BRACKET=
            "<CCR Description=\"CCR-I\" Retransmit=\"true\" Service=\"To_DiameterPeer_User_dp_user2\"\n"
                    + "><Session-Id>$SessionId</Session-Id>";

    public static final String AVP_WITH_ATTR_WITHOUT_END_BRACKET =
            "<Session-Id code=\"123\"\n"
                    + "    >$SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITHOUT_END_BRACKET_AND_NEW_LINE =
            "\t<Session-Id\n"
                    + "    >$SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITHOUT_END_BRACKET_AND_RETURN_CARET =
            "\t<Session-Id\r"
                    + "    >$SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITHOUT_END_BRACKET_AND_TAB_AT_THE_END =
            "\t<Session-Id\t"
                    + "    >$SessionId</Session-Id>";

    public static final String NOT_AVP_WITHOUT_END_BRACKET_SOME_CHARS_INTO_AND_OPENED_BRACKET_AFTER =
            "<fff<bbb\n"
                    + "    >$SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITH_SPACE_AFTER =
            "\t<Session-Id >\n"
                    + "    $SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITH_SPACE_BEFORE_AND_AFTER =
            "\t< Session-Id >\n"
                    + "    $SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITH_SPACE_AFTER_AND_WITHOUT_END_BRACKET =
            "\t<Session-Id \n"
                    + "    >$SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITH_TAB_INTO_TAG =
            "\t<Session-Id\t>"
                    + "    $SessionId</Session-Id>";

    public static final String SESSION_ID_AVP_WITH_TAB_AFTER_TAG =
            "\t<Session-Id>\t"
                    + "    $SessionId</Session-Id>";

}
