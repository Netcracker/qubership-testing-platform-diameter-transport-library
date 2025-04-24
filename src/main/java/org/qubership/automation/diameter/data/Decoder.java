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

package org.qubership.automation.diameter.data;

import java.nio.ByteBuffer;

import org.qubership.automation.diameter.dictionary.DictionaryConfig;

public abstract class Decoder {

    protected static final int QUARTER = 4;
    protected DictionaryConfig dictionaryConfig;

    public Decoder(DictionaryConfig dictionaryConfig) {
        this.dictionaryConfig = dictionaryConfig;
    }

    public abstract String decode(ByteBuffer data);

    public abstract int getMessageLength(byte[] data);

    public abstract void setAppendAvpcode(boolean b);

    public abstract void setAppendAvpvendor(boolean b);
}
