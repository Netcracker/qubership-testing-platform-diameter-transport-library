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

package org.qubership.automation.diameter.command;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.qubership.automation.diameter.data.Converter;

import lombok.Getter;
import lombok.Setter;

public class Command {

    /**
     * Application ID.
     */
    @Setter
    @Getter
    private int applicationId = 0;

    /**
     * Flag if it's request tag or not.
     */
    @Setter
    @Getter
    private boolean requestTag = false;

    /**
     * Flag if it's request or not.
     */
    @Setter
    @Getter
    private boolean request = false;

    /**
     * Flag if it's proxiable or not.
     */
    @Setter
    @Getter
    private boolean proxiable = false;

    /**
     * Flag if it's error command or not.
     */
    @Setter
    @Getter
    private boolean error = false;

    /**
     * Command ID.
     */
    @Setter
    @Getter
    private int id;

    /**
     * Short Name.
     */
    @Setter
    @Getter
    private String shortName = "";

    /**
     * Set of required tags.
     */
    private final Set<String> required = new HashSet<>();

    /**
     * Constructor.
     */
    public Command() {
    }

    /**
     * Constructor.
     *
     * @param id Command ID.
     * @param applicationId Application ID.
     */
    public Command(final int id, final int applicationId) {
        this.id = id;
        this.applicationId = applicationId;
    }

    /**
     * Check if the object equals to obj parameter.
     *
     * @param obj Object to compare.
     * @return true if objects equal each other; otherwise false.
     */
    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof Command)) {
            return false;
        }
        Command target = (Command) obj;
        return Objects.equals(target.getId(), this.id)
                && Objects.equals(target.getShortName(), this.shortName)
                && Objects.equals(target.getApplicationId(), this.applicationId);
    }

    /**
     * Make String representation of the command.
     *
     * @return String representation of the command.
     */
    @Override
    public String toString() {
        return "Command{applicationId=" + applicationId + ", id=" + id + ", shortName='" + shortName + "'}";
    }

    /**
     * Calculate HashCode of the object.
     *
     * @return HashCode of the object.
     */
    @Override
    public int hashCode() {
        int result = applicationId;
        result = 31 * result + id;
        result = 31 * result + shortName.hashCode();
        return result;
    }

    /**
     * Convert id to Bytes and set Flags.
     *
     * @return byte array containing converted id with flags set.
     */
    public byte[] convertToBytesAndSetFlags() {
        byte[] code = Converter.intToBytes(id);
        setFlags(code);
        return code;
    }

    /**
     * This method receives as input an array of bytes (quarter) that contains the flags byte
     * (the first (0) byte of this quarter).
     * The flags byte is calculated and modified.
     *
     * @param bytes - the bytes array (quarter) to be modified.
     */
    private void setFlags(final byte[] bytes) {
        String flagAsString = isRequest()
                ? "1"
                : "0"; // 'Request' flag
        flagAsString += isProxiable() ? "1" : "0";
        flagAsString += isError() ? "1" : "0";
        flagAsString += "00000";
        bytes[0] = (byte) Integer.parseInt(flagAsString, 2);
    }

    public void addRequired(final String name) {
        this.required.add(name);
    }

    public boolean isRequired(final String name) {
        return required.contains(name);

    }
}
