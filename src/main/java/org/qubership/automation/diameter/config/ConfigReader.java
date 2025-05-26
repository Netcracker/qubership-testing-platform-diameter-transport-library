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

package org.qubership.automation.diameter.config;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.qubership.automation.diameter.dictionary.DiameterDictionary;
import org.qubership.automation.diameter.dictionary.DiameterDictionaryHolder;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.xml.sax.SAXException;

public class ConfigReader {

    /**
     * Instance object link.
     */
    private static final ConfigReader INSTANCE = new ConfigReader();

    /**
     * XML files filter by file extension.
     */
    private static final FileFilter XML_FILTER = pathname -> pathname.getName().endsWith(".xml");

    /**
     * Marben parser class simple name.
     */
    private static final String MARBEN_PARSER = MarbenParser.class.getSimpleName();

    /**
     * Constructor.
     */
    private ConfigReader() {
    }

    /**
     * Read diameter dictionary (xml files) which will put to {@link DiameterDictionaryHolder}  (this holder keeps
     * multi-dictionary with {@link DiameterDictionary} type).
     * After dictionaries will read and put to {@link DiameterDictionaryHolder} any of them will use by
     * {@link DictionaryConfig} key in diameter parsers, encoders\decoders and avp formatter.
     * Force mode: if you want to re-read existing dictionary you need to set input parameter "force" to true.
     *
     * @param dictionaryConfig {@link DictionaryConfig}
     * @param force            set input parameter "force" as true to re-read (reload) existing dictionary.
     * @throws ParserConfigurationException if a parser cannot be created which satisfies the requested configuration.
     * @throws IOException                  dictionary files IO exceptions.
     * @throws SAXException                 if a parser cannot be created which satisfies the requested configuration.
     */
    public static void read(@Nonnull final DictionaryConfig dictionaryConfig, final boolean force)
            throws ParserConfigurationException, SAXException, IOException {
        DiameterDictionaryHolder dictionaryHolder = DiameterDictionaryHolder.getInstance();
        DiameterDictionary dictionary = dictionaryHolder.getDictionary(dictionaryConfig);
        if (Objects.nonNull(dictionary) && force) {
            dictionaryHolder.removeDictionary(dictionaryConfig);
        }
        if (Objects.isNull(dictionary) || force) {
            String parserType = dictionaryConfig.getParserClass().getSimpleName();
            INSTANCE.readConfig(dictionaryConfig, MARBEN_PARSER.equals(parserType)
                    ? new MarbenParser(dictionaryConfig)
                    : new StandardParser(dictionaryConfig));
        }
    }

    private void readConfig(final DictionaryConfig dictionaryConfig, final DiameterParser diameterParser)
            throws SAXException, ParserConfigurationException {
        String path = dictionaryConfig.getDictionaryPath();
        File directory = new File(path);
        if (!directory.exists()) {
            throw new IllegalArgumentException("Directory with configuration doesn't exist: " + path);
        }
        if (directory.isFile()) {
            throw new IllegalArgumentException(
                    "Path to directory with config xmls should be specified, instead of path to single file:" + path);
        }
        readFiles(directory, diameterParser, dictionaryConfig);
    }

    private synchronized void readFiles(final File directory,
                                        final DiameterParser diameterParser,
                                        final DictionaryConfig dictionaryConfig)
            throws ParserConfigurationException, SAXException {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser saxParser = factory.newSAXParser();
        File[] files = directory.listFiles(XML_FILTER);
        String dictionaryPath = dictionaryConfig.getDictionaryPath();
        if (files == null) {
            throw new IllegalArgumentException(
                    "Directory '" + dictionaryPath + "' doesn't contain xml config files for diameter.");
        }
        initDictionary(dictionaryConfig);
        for (File file : files) {
            try {
                saxParser.parse(file, diameterParser);
            } catch (Exception e) {
                DiameterDictionaryHolder.getInstance().removeDictionary(dictionaryConfig);
                throw new IllegalStateException("Can't read configuration file: " + file, e);
            } finally {
                saxParser.reset();
            }
        }
        setDictionaryAsReady(dictionaryConfig);
    }

    private void setDictionaryAsReady(final DictionaryConfig dictionaryConfig) {
        DiameterDictionaryHolder.getInstance().getDictionary(dictionaryConfig).setReady(true);
    }

    private void initDictionary(final DictionaryConfig dictionaryConfig) {
        DiameterDictionaryHolder.getInstance().createDictionary(dictionaryConfig);
    }
}
