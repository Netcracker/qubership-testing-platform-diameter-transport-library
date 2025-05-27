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

package org.qubership.automation.diameter.data.encoder.wireshark;

public class AvpRecord {
    @lombok.Setter
    @lombok.Getter
    private String length;
    @lombok.Setter
    @lombok.Getter
    private String code;
    @lombok.Setter
    @lombok.Getter
    private String value;
    // private String[] combinedValue; // Unused, commented. Not deleted, for possible future use
    @lombok.Setter
    @lombok.Getter
    private String flags;
    @lombok.Setter
    @lombok.Getter
    private String vendor;
    @lombok.Setter
    @lombok.Getter
    private String name;
    @lombok.Setter
    @lombok.Getter
    private String[] values;
    private String hex;

    @Override
    public String toString() {
        return "AvpRecord{code='" + code
                + "',length='" + length
                + "', value='" + value
                + "', flags='" + flags
                + "', vendor='" + vendor + "'}";
    }

    public void setHexValue(final String hexValue) {
        this.hex = hexValue;
    }

    public String getHexValue() {
        return hex;
    }
}
