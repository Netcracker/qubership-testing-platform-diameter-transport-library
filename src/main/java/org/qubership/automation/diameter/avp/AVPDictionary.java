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

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("AbbreviationAsWordInName")
public class AVPDictionary extends AVPProvider {
    private final Map<Integer, AVPProvider> vendors = new HashMap<>();

    /**
     * Create AVPDictionary object for vendorId and put it into vendors map.
     *
     * @param vendorId - id of vendor to add.
     */
    public void addVendor(final int vendorId) {
        if (!vendors.containsKey(vendorId)) {
            AVPDictionary value = new AVPDictionary();
            value.setVendorId(vendorId);
            vendors.put(vendorId, value);
        }
    }

    /**
     * Find vendor by id.
     *
     * @param vendorId - id of vendor to search.
     * @return AVPProvider found by id, otherwise IllegalArgumentException is thrown.
     */
    public AVPProvider getVendor(final int vendorId) {
        AVPProvider avpProvider = vendors.get(vendorId);
        if (avpProvider == null) {
            throw new IllegalArgumentException("Vendor is not found by id " + vendorId);
        }
        return avpProvider;
    }

    /**
     * Check if vendor with this id is present in vendors map.
     *
     * @param vendorId - id of vendor to check.
     * @return true if vendor with this id is present in vendors map, otherwise false.
     */
    public boolean isVendorExist(final int vendorId) {
        return vendors.containsKey(vendorId);
    }

    /**
     * Find AVP by name.
     *
     * @param name - AVP name to search
     * @return AVPEntity found by name, or IllegalArgumentException is thrown.
     */
    @Override
    public AVPEntity getByName(final String name) {
        AVPEntity byName = getByNameOrNull(name);
        if (byName == null) {
            byName = getFromVendors(name);
        }
        return byName;
    }

    /**
     * Get AVPEntity by id from this AVPProvider's avpMap.
     * If not found, find it under all providers and return the 1st found entry.
     * It should be rechecked, because 'byId = getByIdFromVendors(id)' seems unachievable,
     * because IllegalArgumentException is thrown in getById() in case not found.
     *
     * @param id - id of AVP to search.
     * @return AVPEntity found by id, or IllegalArgumentException is thrown.
     */
    public AVPEntity getByIdGlobal(final Integer id) {
        AVPEntity byId = getById(id);
        if (byId == null) {
            byId = getByIdFromVendors(id);
        }
        return byId;
    }

    /**
     * Clear Dictionary and the corresponding Vendors map.
     */
    public void clear() {
        super.clear();
        vendors.clear();
    }

    private AVPEntity getFromVendors(final String name) {
        for (AVPProvider provider : vendors.values()) {
            AVPEntity entity = provider.getByNameOrNull(name);
            if (entity != null) {
                return entity;
            }
        }
        throw new IllegalArgumentException("AVP not found by name " + name);
    }

    private AVPEntity getByIdFromVendors(final Integer id) {
        for (AVPProvider provider : vendors.values()) {
            AVPEntity entity = provider.getById(id);
            if (entity != null) {
                return entity;
            }
        }
        throw new IllegalArgumentException("AVP not found by id " + id);
    }
}
