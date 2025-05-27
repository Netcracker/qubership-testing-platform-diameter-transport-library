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

package org.qubership.automation.diameter.dictionary;

import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.Objects;

import javax.annotation.Nonnull;

import org.qubership.automation.diameter.config.DiameterParser;

public class DictionaryConfig {

    @lombok.Getter
    private final BigInteger id;
    @lombok.Getter
    private final String dictionaryPath;
    private final Class<? extends DiameterParser> parser;

    /**
     * Create dictionary config that will use to get dictionary from dictionary holder.
     * All the input params will use as key to get dictionary from holder
     * If id is null will create new BigInteger("0")
     *
     * @param dictionaryPath absolute path to dictionary.
     * @param parser         object of Class extending DiameterParser
     * @param id             unique id; If id is null will create new BigInteger.ZERO.
     */
    public DictionaryConfig(
            @Nonnull String dictionaryPath, @Nonnull Class<? extends DiameterParser> parser, BigInteger id) {
        this.dictionaryPath = dictionaryPath;
        this.parser = parser;
        this.id = Objects.isNull(id)
                ? ZERO
                : id;
    }

    public String getKey() {
        return String.format("%s_%s_%s", id.toString(), parser.getSimpleName(), dictionaryPath);
    }

    public Class<? extends DiameterParser> getParserClass() {
        return parser;
    }

}
