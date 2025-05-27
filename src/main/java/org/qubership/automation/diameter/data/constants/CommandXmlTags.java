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

    /**
     * Constant for ASR command prefix.
     */
    public static final String ASR = "<ASR";

    /**
     * Constant for ASA command prefix.
     */
    public static final String ASA = "<ASA";

    /**
     * Constant for SNR command prefix.
     */
    public static final String SNR = "<SNR";

    /**
     * Constant for SNA command prefix.
     */
    public static final String SNA = "<SNA";

    /**
     * Constant for CER command prefix.
     */
    public static final String CER = "<CER";

    /**
     * Constant for CEA command prefix.
     */
    public static final String CEA = "<CEA";

    /**
     * Constant for RAR command prefix.
     */
    public static final String RAR = "<RAR";

    /**
     * Constant for RAA command prefix.
     */
    public static final String RAA = "<RAA";

    /**
     * Constant for CCR command prefix.
     */
    public static final String CCR = "<CCR";

    /**
     * Constant for CCA command prefix.
     */
    public static final String CCA = "<CCA";

    /**
     * Constant for DWR command prefix.
     */
    public static final String DWR = "<DWR";

    /**
     * Constant for DWA command prefix.
     */
    public static final String DWA = "<DWA";

    /**
     * Constant for DPR command prefix.
     */
    public static final String DPR = "<DPR";

    /**
     * Constant for DPA command prefix.
     */
    public static final String DPA = "<DPA";

    /**
     * Constant for STR command prefix.
     */
    public static final String STR = "<STR";

    /**
     * Constant for STA command prefix.
     */
    public static final String STA = "<STA";

    /**
     * Constant for SLR command prefix.
     */
    public static final String SLR = "<SLR";

    /**
     * Constant for SLA command prefix.
     */
    public static final String SLA = "<SLA";

    /**
     * List of String command prefixes.
     */
    public static final List<String> COMMAND_XML_TAGS = new ArrayList<>(
            Arrays.asList(ASR.toLowerCase(), ASA.toLowerCase(), SNR.toLowerCase(), SNA.toLowerCase(), CER.toLowerCase(),
                    CEA.toLowerCase(), RAR.toLowerCase(), RAA.toLowerCase(), CCR.toLowerCase(), CCA.toLowerCase(),
                    DWR.toLowerCase(), DWA.toLowerCase(), DPR.toLowerCase(), DPA.toLowerCase(), STR.toLowerCase(),
                    STA.toLowerCase(), SLR.toLowerCase(), SLA.toLowerCase()));
}
