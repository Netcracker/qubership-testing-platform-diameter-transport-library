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

import lombok.Setter;

@SuppressWarnings("AbbreviationAsWordInName")
public class AVPProvider {

    /**
     * Map of AVPs.
     */
    private final Map<Object, AVPEntity> avpMap = new HashMap<>();

    /**
     * Vendor ID.
     */
    @Setter
    private int vendorId;

    /**
     * Search AVPEntity by id.
     *
     * @param id - Integer id of AVPEntity to search for
     * @return AVPEntity found in the avpMap.
     */
    public AVPEntity getById(final Integer id) {
        return get(id);
    }

    /**
     * Search AVPEntity by name.
     *
     * @param name - String name of AVPEntity to search for
     * @return AVPEntity found in the avpMap.
     */
    public AVPEntity getByName(final String name) {
        return get(name);
    }

    /**
     * Add AVPEntity to avpMap, as 2 entries: by id and by name keys.
     *
     * @param entity - AVPEntity to be added to avpMap.
     */
    public void add(final AVPEntity entity) {
        avpMap.put(entity.getName(), entity);
        avpMap.put(entity.getId(), entity);
    }

    private AVPEntity get(final Object identifier) {
        AVPEntity avpEntity = avpMap.get(identifier);
        if (avpEntity == null) {
            throw new IllegalArgumentException("AVP not found by identifier " + identifier + ". Vendor Id: "
                    + this.vendorId);
        }
        return avpEntity;
    }

    /**
     * Search AVPEntity by name.
     *
     * @param name - String name of AVPEntity to search for
     * @return AVPEntity found in the avpMap.
     */
    public AVPEntity getByNameOrNull(final String name) {
        return avpMap.get(name);
    }

    /**
     * Clear avpMap of the provider.
     */
    public void clear() {
        avpMap.clear();
    }
}
