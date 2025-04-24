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
    private String length;
    private String code;
    private String value;
    // private String[] combinedValue; // Unused, commented. Not deleted, for possible future use
    private String flags;
    private String vendor;
    private String name;
    private String[] values;
    private String hex;

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public String getFlags() {
        return flags;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public String toString() {
        return "AvpRecord{code='" + code
                + "',length='" + length
                + "', value='" + value
                + "', flags='" + flags
                + "', vendor='" + vendor + "'}";
    }

    public void setName(String nameValue) {
        this.name = nameValue;
    }

    public String getName() {
        return this.name;
    }

    public void setValues(String[] valuesArray) {
        this.values = valuesArray;
    }

    public String[] getValues() {
        return this.values;
    }

    public void setHexValue(String hexValue) {
        this.hex = hexValue;
    }

    public String getHexValue() {
        return hex;
    }
}
