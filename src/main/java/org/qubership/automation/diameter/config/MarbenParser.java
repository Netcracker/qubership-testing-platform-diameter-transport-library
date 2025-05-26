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

import java.util.HashMap;
import java.util.Map;

import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPRule;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.command.CommandDictionary;
import org.qubership.automation.diameter.data.encoder.Utils;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.qubership.automation.diameter.dictionary.DictionaryService;
import org.xml.sax.Attributes;

@SuppressWarnings("checkstyle:JavadocVariable")
public class MarbenParser extends DiameterParser {

    private static final String DIAMETER_COMMAND_DICTIONARY = "diameter-command-dictionary";
    private static final String VENDOR = "vendor";
    private static final String TYPE = "type";
    private static final String ENUM = "enum";
    private static final String GROUPED = "grouped";
    private static final String APPLICATION = "application";
    private final Map<String, Integer> vendors = new HashMap<>();
    private final Map<Integer, Integer> commandsVendors = new HashMap<>();
    private boolean isCommandDictionary;
    private int vendorId = 0;
    private boolean isApplicationIdParsing;
    private int applicationId = 0;

    /**
     * Create new MarbenParser. Parser that can work with Marben diameter dictionaries.
     *
     * @param dictionaryConfig object that contains key that will use to get necessary dictionary to read and add new
     *                         dictionary components(avp, command).
     */
    public MarbenParser(final DictionaryConfig dictionaryConfig) {
        this.dictionaryConfig = dictionaryConfig;
    }

    @Override
    public void startElementProcessing(final String uri,
                                       final String localName,
                                       final String tag,
                                       final Attributes attributes) {
        switch (tag) {
            case DIAMETER_COMMAND_DICTIONARY:
                this.isCommandDictionary = true;
                break;
            case COMMAND:
                if (!isCommandDictionary && !isApplicationIdParsing) {
                    return;
                }
                if (isApplicationIdParsing) {
                    int commandCode = Utils.parseInt(attributes.getValue("code"));
                    commandsVendors.put(commandCode, this.applicationId);
                    setApplicationId(commandCode);
                    return;
                }
                this.command = new Command();
                int code = Utils.parseInt(attributes.getValue("code"));
                command.setId(code);
                command.setRequest(Utils.parseBoolean(attributes.getValue("request")));
                command.setRequestTag(Utils.parseBoolean(attributes.getValue("request")));
                command.setProxiable(Utils.parseBoolean(attributes.getValue("proxiable")));
                command.setError(Utils.parseBoolean(attributes.getValue("error")));
                command.setShortName(getShortName(attributes));
                command.setApplicationId(getApplicationId(code));
                break;
            case AVP:
                if (isCommandDictionary) {
                    return;
                }
                this.isAvp = true;
                this.avp = new AVPEntity();
                this.avp.setName(attributes.getValue("name"));
                this.avp.setId(Utils.parseInt(attributes.getValue("code")));
                this.avp.setVendorId(this.vendorId);
                this.avp.setMandatory(AVPRule.of(attributes.getValue("mandatory")));
                String protectedVal = attributes.getValue("protected");
                if (protectedVal != null) {
                    this.avp.setProtect(AVPRule.of(attributes.getValue("protected")));
                }
                break;
            case VENDOR:
                this.vendorId = Utils.parseInt(attributes.getValue("id"));
                this.vendors.put(attributes.getValue("name"), this.vendorId);
                break;
            case TYPE:
                this.avp.setType(AVPType.fromString(attributes.getValue("type-name")));
                break;
            case ENUM:
                int id = Utils.parseInt(attributes.getValue("code"));
                String name = attributes.getValue("name");
                this.avp.addEnumerated(id, name);
                break;
            case APPLICATION:
                this.isApplicationIdParsing = true;
                this.applicationId = Utils.parseInt(attributes.getValue("id"));
                break;
            default:
        }
    }

    private void setApplicationId(final int commandCode) {
        CommandDictionary commandDictionary = DictionaryService.getInstance().getCommandDictionary(dictionaryConfig);
        if (commandDictionary.containsRequest(commandCode)) {
            setAppId(commandDictionary.getRequest(commandCode));
        }
        if (commandDictionary.containsAnswer(commandCode)) {
            setAppId(commandDictionary.getAnswer(commandCode));
        }
    }

    private void setAppId(final Command command) {
        if (command != null) {
            command.setApplicationId(this.applicationId);
        }
    }

    private int getApplicationId(final int code) {
        Integer idVendor = commandsVendors.get(code);
        return (idVendor == null) ? 0 : idVendor;
    }

    @Override
    public void endElement(final String uri, final String localName, final String tag) {
        switch (tag) {
            case COMMAND:
                if (isCommandDictionary) {
                    DictionaryService.getInstance().getCommandDictionary(dictionaryConfig).add(command);
                }
                break;
            case AVP:
                if (this.avp == null || isCommandDictionary) {
                    break;
                }
                isAvp = false;
                addAvp(avp);
                break;
            case REQUIRED:
                isRequired = false;
                break;
            case VENDOR:
                this.vendorId = 0;
                break;
            case DIAMETER_COMMAND_DICTIONARY:
                this.isCommandDictionary = false;
                break;
            case GROUPED:
                this.avp.setType(AVPType.GROUPED);
                break;
            case APPLICATION:
                this.isApplicationIdParsing = false;
                this.applicationId = 0;
                break;
            default:
        }
    }
}
