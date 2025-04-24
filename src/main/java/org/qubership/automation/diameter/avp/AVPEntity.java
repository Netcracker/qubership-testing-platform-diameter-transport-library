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

@SuppressWarnings("AbbreviationAsWordInName")
public class AVPEntity {
    private AVPType type;
    private String name;
    private AVPRule protect;
    private AVPRule mandatory;
    private int vendorId;
    private int id;
    private Map<Integer, String> enumeratedValues;

    public AVPEntity() {
    }

    public AVPEntity(AVPType type, String name) {
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
    public AVPEntity(AVPType type, String name, AVPRule protect, AVPRule mandatory, int vendorId) {
        this.type = type;
        this.name = name;
        this.protect = protect;
        this.mandatory = mandatory;
        this.vendorId = vendorId;
    }

    public AVPType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public AVPRule getProtect() {
        return protect;
    }

    public AVPRule getMandatory() {
        return mandatory;
    }

    public int getVendorId() {
        return vendorId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setType(AVPType type) {
        this.type = type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProtect(AVPRule protect) {
        this.protect = protect;
    }

    public void setMandatory(AVPRule mandatory) {
        this.mandatory = mandatory;
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    /**
     * Add {id, value} entry into enumeratedValues map; initialize new map if needed.
     *
     * @param id - key of entry to add,
     * @param value - value of entry to add.
     */
    public void addEnumerated(int id, String value) {
        if (enumeratedValues == null) {
            enumeratedValues = Maps.newHashMapWithExpectedSize(10);
        }
        enumeratedValues.put(id, value);
    }

    /**
     * Find by key in enumeratedValues.
     *
     * @param id - key to search in enumeratedValues.
     * @return value of found entry.
     */
    public String getEnumerated(int id) {
        return enumeratedValues.get(id);
    }

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
    public int enumerated(String value) {
        for (Map.Entry<Integer, String> entry : enumeratedValues.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(value)) {
                return entry.getKey();
            }
        }
        return 0;
    }
}
