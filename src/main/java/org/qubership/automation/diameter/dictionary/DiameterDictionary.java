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

import java.math.BigInteger;

import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.command.CommandDictionary;

public class DiameterDictionary {

    private final CommandDictionary commandDictionary = new CommandDictionary();
    private final AVPDictionary avpDictionary = new AVPDictionary();
    private final String path;
    private final String type;
    private final BigInteger id;
    private boolean ready;

    /**
     * Create new instance of DiameterDictionary that contains {@link AVPDictionary} and {@link CommandDictionary}.
     *
     * @param dictionaryConfig {@link DictionaryConfig}
     */
    public DiameterDictionary(DictionaryConfig dictionaryConfig) {
        this.path = dictionaryConfig.getDictionaryPath();
        this.type = dictionaryConfig.getParserClass().getSimpleName();
        this.id = dictionaryConfig.getId();
        this.ready = false;
    }

    public CommandDictionary getCommandDictionary() {
        return commandDictionary;
    }

    public AVPDictionary getAvpDictionary() {
        return avpDictionary;
    }

    public String getPath() {
        return path;
    }

    public String getType() {
        return type;
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }

    public BigInteger getId() {
        return id;
    }
}
