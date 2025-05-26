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

package org.qubership.automation.diameter.avp;

import java.util.Map;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@SuppressWarnings("AbbreviationAsWordInName")
public class AVPEntity {

    /**
     * Type of Entity.
     */
    private AVPType type;

    /**
     * Name of Entity.
     */
    private String name;

    /**
     * Protection rule.
     */
    private AVPRule protect;

    /**
     * Mandatory rule.
     */
    private AVPRule mandatory;

    /**
     * Vendor ID.
     */
    private int vendorId;

    /**
     * Entity ID.
     */
    private int id;

    /**
     * Enumerated Values Map.
     */
    private Map<Integer, String> enumeratedValues;

    public AVPEntity(final AVPType type, final String name) {
        this.type = type;
        this.name = name;
    }

    /**
     * Constructor of AVPEntity.
     *
     * @param type - type of AVPEntity,
     * @param name - name of AVPEntity,
     * @param protect - protection rule,
     * @param mandatory - mandatory rule,
     * @param vendorId - id of vendor.
     */
    public AVPEntity(final AVPType type,
                     final String name,
                     final AVPRule protect,
                     final AVPRule mandatory,
                     final int vendorId) {
        this.type = type;
        this.name = name;
        this.protect = protect;
        this.mandatory = mandatory;
        this.vendorId = vendorId;
    }

    /**
     * Add {key, value} entry into enumeratedValues map; initialize new map if needed.
     *
     * @param key - key of entry to add,
     * @param value - value of entry to add.
     */
    public void addEnumerated(final int key, final String value) {
        synchronized (this) {
            if (enumeratedValues == null) {
                enumeratedValues = Maps.newHashMapWithExpectedSize(10);
            }
        }
        enumeratedValues.put(key, value);
    }

    /**
     * Find by key in enumeratedValues.
     *
     * @param key - key to search in enumeratedValues.
     * @return value of found entry.
     */
    public String getEnumerated(final int key) {
        return enumeratedValues.get(key);
    }

    /**
     * Get String representation of the AVPEntity.
     *
     * @return String containing name, vendorId and id properties.
     */
    @Override
    public String toString() {
        return "AVP{name='" + name + "', vendorId=" + vendorId + ", id=" + id + '}';
    }

    /**
     * Find entry in enumeratedValues by value, and return its key.
     *
     * @param value - value to search.
     * @return key of found entry, otherwise return 0.
     */
    public int enumerated(final String value) {
        for (Map.Entry<Integer, String> entry : enumeratedValues.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(value)) {
                return entry.getKey();
            }
        }
        return 0;
    }
}
