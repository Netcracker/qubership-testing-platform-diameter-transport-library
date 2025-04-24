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

package org.qubership.automation.diameter;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.junit.BeforeClass;
import org.qubership.automation.diameter.config.ConfigReader;
import org.qubership.automation.diameter.config.MarbenParser;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.xml.sax.SAXException;

public abstract class MarbenConfigProvider {

    private static final String DICTIONARY_PATH = "./src/test/resources/marben";

    public static final DictionaryConfig DICTIONARY_CONFIG = new DictionaryConfig(DICTIONARY_PATH, MarbenParser.class,
            null);

    @BeforeClass
    public static void prepare() throws ParserConfigurationException, SAXException, IOException {
        ConfigReader.read(DICTIONARY_CONFIG, false);
    }
}
