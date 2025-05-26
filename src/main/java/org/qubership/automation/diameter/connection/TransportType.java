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

package org.qubership.automation.diameter.connection;

public enum TransportType {

    /**
     * Constant for SCTP transport type.
     */
    SCTP("sctp"),

    /**
     * Constant for TCP transport type.
     */
    TCP("tcp");

    /**
     * Name of type.
     */
    private final String name;

    /**
     * Constructor.
     *
     * @param transportTypeName String type name.
     */
    TransportType(final String transportTypeName) {
        this.name = transportTypeName;
    }

    /**
     * Get name of the transport type.
     *
     * @return String name.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Get TransportType by transport type name (equalsIgnoreCase).
     *
     * @param name - transport type name.
     * @return TransportType if found, otherwise IllegalArgumentException is thrown.
     */
    public static TransportType getType(final String name) {
        for (TransportType type : values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown type of transport: " + name);
    }

}
