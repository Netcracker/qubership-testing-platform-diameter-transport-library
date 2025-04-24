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

@SuppressWarnings("AbbreviationAsWordInName")
public enum AVPRule {
    MUST("must") {
        @Override
        public byte flag() {
            return 0x40;
        }
    },
    MAY("may") {
        @Override
        public byte flag() {
            return 0x20;
        }
    },
    MUSTNOT("mustnot") {
        @Override
        public byte flag() {
            return 0x0;
        }
    },
    SHOULDNOT("shouldnot") {
        @Override
        public byte flag() {
            return 0x0;
        }
    },
    SHOULD("should") {
        @Override
        public byte flag() {
            return 0x0;
        }
    };

    private final String rule;

    AVPRule(String rule) {
        this.rule = rule;
    }

    public abstract byte flag();

    /**
     * Find AVPRule by rule type.
     *
     * @param value rule type to search.
     * @return AVPRule if found; otherwise IllegalArgumentException is thrown.
     */
    public static AVPRule of(String value) {
        for (AVPRule avpRule : AVPRule.class.getEnumConstants()) {
            if (avpRule.toString().equalsIgnoreCase(value)) {
                return avpRule;
            }
        }
        throw new IllegalArgumentException("Undefined rule type: " + value);
    }

    @Override
    public String toString() {
        return rule;
    }
}
