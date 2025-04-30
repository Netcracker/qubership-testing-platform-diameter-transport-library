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

import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPType;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.xml.sax.Attributes;

public class StandardParser extends DiameterParser {

    /**
     * Create new StandardParser.
     *
     * @param dictionaryConfig {@link DictionaryConfig}
     */
    public StandardParser(final DictionaryConfig dictionaryConfig) {
        this.dictionaryConfig = dictionaryConfig;
    }

    @Override
    protected void startElementProcessing(final String uri,
                                          final String localName,
                                          final String tag,
                                          final Attributes attributes) {
        switch (tag.toLowerCase()) {
            case APPLICATION:
                this.application = new HashMap<>();
                this.application.put("id", attributes.getValue("id"));
                this.application.put("vendor", attributes.getValue("vendor"));
                break;
            case COMMAND:
                this.command = new Command();
                this.command.setId(Integer.parseInt(attributes.getValue("id")));
                String commandApplication = attributes.getValue("application");
                this.command.setApplicationId(Integer.parseInt(commandApplication == null
                        ? this.application.get("id")
                        : commandApplication));
                break;
            case REQUEST:
                command.setRequestTag(true);
                command.setShortName(getShortName(attributes));
                break;
            case ANSWER:
                command.setRequestTag(false);
                command.setShortName(getShortName(attributes));
                break;
            case HEADER_BIT:
                initHeaderBit(attributes);
                break;
            case AVP:
                isAvp = true;
                this.avp = new AVPEntity();
                initAvpAttributes(attributes);
                break;
            case SIMPLE_TYPE:
                this.avp.setType(AVPType.fromString(attributes.getValue("name")));
                break;
            case ENUMERATOR:
                if (this.avp.getType() == null) {
                    this.avp.setType(AVPType.ENUMERATE);
                }
                addEnumerated(avp, attributes);
                break;
            case LAYOUT:
                if (this.avp == null) {
                    //we don't handle it for command. so, it should be skipped.
                    break;
                }
                if (this.avp.getType() == null) {
                    this.avp.setType(AVPType.GROUPED);
                }
                break;
            case REQUIRED:
                this.isRequired = true;
                /* fallthrough */
            case AVP_REF:
                if (isRequired && command != null) {
                    command.addRequired(attributes.getValue(NAME));
                    break;
                }
                if (!isAvp) {
                    break;
                }
                avp.setType(AVPType.GROUPED);
                break;
            case FLAG_RULE:
                setRules(attributes);
                break;
            default:
        }
    }
}
