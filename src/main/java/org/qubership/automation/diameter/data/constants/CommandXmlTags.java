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

package org.qubership.automation.diameter.data.constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandXmlTags {

    public static final String ASR = "<ASR";
    public static final String ASA = "<ASA";
    public static final String SNR = "<SNR";
    public static final String SNA = "<SNA";
    public static final String CER = "<CER";
    public static final String CEA = "<CEA";
    public static final String RAR = "<RAR";
    public static final String RAA = "<RAA";
    public static final String CCR = "<CCR";
    public static final String CCA = "<CCA";
    public static final String DWR = "<DWR";
    public static final String DWA = "<DWA";
    public static final String DPR = "<DPR";
    public static final String DPA = "<DPA";
    public static final String STR = "<STR";
    public static final String STA = "<STA";
    public static final String SLR = "<SLR";
    public static final String SLA = "<SLA";

    public static final List<String> COMMAND_XML_TAGS = new ArrayList<>(
            Arrays.asList(ASR.toLowerCase(), ASA.toLowerCase(), SNR.toLowerCase(), SNA.toLowerCase(), CER.toLowerCase(),
                    CEA.toLowerCase(), RAR.toLowerCase(), RAA.toLowerCase(), CCR.toLowerCase(), CCA.toLowerCase(),
                    DWR.toLowerCase(), DWA.toLowerCase(), DPR.toLowerCase(), DPA.toLowerCase(), STR.toLowerCase(),
                    STA.toLowerCase(), SLR.toLowerCase(), SLA.toLowerCase()));
}
