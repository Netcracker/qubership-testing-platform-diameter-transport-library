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

import java.util.concurrent.ConcurrentHashMap;

import org.qubership.automation.diameter.config.MarbenParser;
import org.qubership.automation.diameter.config.StandardParser;

public class DiameterDictionaryHolder {

    private static final DiameterDictionaryHolder INSTANCE = new DiameterDictionaryHolder();
    private final ConcurrentHashMap<String, DiameterDictionary> holder = new ConcurrentHashMap<>();

    /**
     * Create new or return existing DiameterDictionaryHolder instance.
     *
     * @return DiameterDictionaryHolder instance.
     */
    public static DiameterDictionaryHolder getInstance() {
        return INSTANCE;
    }

    /**
     * Get DiameterDictionary by dictionaryPath+parserType key if it exists in holder.
     *
     * @param dictionaryConfig {@link DictionaryConfig} that contains absolute string path to diameter dictionary +
     *                         {@link MarbenParser} or
     *                         {@link StandardParser} class
     *                         simple name + UUID
     * @return DiameterDictionary if exists in holder, or null if it not exists.
     */
    public DiameterDictionary getDictionary(DictionaryConfig dictionaryConfig) {
        return holder.get(dictionaryConfig.getKey());
    }

    /**
     * Create new dictionary and put it to this holder with path and parser type as key.
     *
     * @param dictionaryConfig {@link DictionaryConfig} that contains absolute string path to diameter dictionary +
     *                         {@link MarbenParser} or
     *                         {@link StandardParser} class
     *                         simple name + UUID
     */
    public void createDictionary(DictionaryConfig dictionaryConfig) {
        DiameterDictionary dictionary = new DiameterDictionary(dictionaryConfig);
        holder.putIfAbsent(dictionaryConfig.getKey(), dictionary);
    }

    /**
     * Remove dictionary by key (dictionaryPath+parseType) from holder.
     *
     * @param dictionaryConfig key that will use to remove unnecessary dictionary.
     */
    public void removeDictionary(DictionaryConfig dictionaryConfig) {
        holder.remove(dictionaryConfig.getKey());
    }
}
