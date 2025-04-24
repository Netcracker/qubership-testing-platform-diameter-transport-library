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

import java.util.Objects;

import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.command.CommandDictionary;

public class DictionaryService {

    private static final DictionaryService INSTANCE = new DictionaryService();

    public static DictionaryService getInstance() {
        return INSTANCE;
    }

    /**
     * Get command dictionary by dictionaryConfig key (uuid + dictionaryPath + parser class simple name).
     *
     * @param dictionaryConfig {@link DictionaryConfig} that contains key to provide dictionary from holder.
     * @return command dictionary if exist by key in holder
     */
    public CommandDictionary getCommandDictionary(DictionaryConfig dictionaryConfig) {
        if (Objects.isNull(dictionaryConfig)) {
            throw new IllegalArgumentException("Can't get command dictionary.. dictionaryKey is null");
        }
        return DiameterDictionaryHolder.getInstance().getDictionary(dictionaryConfig).getCommandDictionary();
    }

    /**
     * Get avp dictionary by dictionary key.
     *
     * @param dictionaryConfig {@link DictionaryConfig}
     * @return If input parameter "dictionaryKey" is null then will return avp dictionary from DictionaryHolder(not
     *         multi-dictionary) else will return
     */
    public AVPDictionary getAvpDictionary(DictionaryConfig dictionaryConfig) {
        if (Objects.isNull(dictionaryConfig)) {
            throw new IllegalArgumentException("Can't get avp dictionary.. dictionaryKey is null");
        }
        return DiameterDictionaryHolder.getInstance().getDictionary(dictionaryConfig).getAvpDictionary();
    }
}
