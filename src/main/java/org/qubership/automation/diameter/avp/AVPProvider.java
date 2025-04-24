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
public class AVPProvider {
    private final Map<Object, AVPEntity> avpMap = new HashMap<>();
    private int vendorId;

    public AVPEntity getById(Integer id) {
        return get(id);
    }

    public AVPEntity getByName(String name) {
        return get(name);
    }

    public void add(AVPEntity entity) {
        avpMap.put(entity.getName(), entity);
        avpMap.put(entity.getId(), entity);
    }

    private AVPEntity get(Object identifier) {
        AVPEntity avpEntity = avpMap.get(identifier);
        if (avpEntity == null) {
            throw new IllegalArgumentException("AVP not found by identifier " + identifier + ". Vendor Id: "
                    + this.vendorId);
        }
        return avpEntity;
    }

    public AVPEntity getByNameOrNull(String name) {
        return avpMap.get(name);
    }

    public void setVendorId(int vendorId) {
        this.vendorId = vendorId;
    }

    public void clear() {
        avpMap.clear();
    }
}
