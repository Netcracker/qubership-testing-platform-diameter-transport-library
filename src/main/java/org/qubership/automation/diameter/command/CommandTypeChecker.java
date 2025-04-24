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

/**
 * The methods of this utility class check the type of command against the short names of the commands.
 * This class has the means to check by the functional type of the commands (connection commands, session commands,
 * notification commands) as well as by the type of the command (credit control commands, watchdog commands...).
 * input param: String - command short name
 * return: boolean true if the input String short name command is equal to one of the short names.
 */
public class CommandTypeChecker {

    /**
     * Returns true if command short name equals CER or CEA, DWR or DWA, DPR or DPA.
     *
     * @param command - String command short name
     * @return - boolean
     */
    public static boolean isConnectionCommand(String command) {
        return "CER".equalsIgnoreCase(command) || "CEA".equalsIgnoreCase(command) || "DWR".equalsIgnoreCase(
                command) || "DWA".equalsIgnoreCase(command) || "DPR".equalsIgnoreCase(
                command) || "DPA".equalsIgnoreCase(command);
    }

    /**
     * Returns true if command short name equals RAR or RAA, ASR or ASA, STR or STA.
     *
     * @param command - String command short name
     * @return - boolean
     */
    public static boolean isSessionCommand(String command) {
        return "RAR".equalsIgnoreCase(command) || "RAA".equalsIgnoreCase(command) || "ASR".equalsIgnoreCase(
                command) || "ASA".equalsIgnoreCase(command) || "STR".equalsIgnoreCase(
                command) || "STA".equalsIgnoreCase(command);
    }

    /**
     * Returns true if command short name equals CCR or CCA.
     *
     * @param command - String command short name
     * @return - boolean
     */
    public static boolean isCreditControlCommand(String command) {
        return "CCR".equalsIgnoreCase(command) || "CCA".equalsIgnoreCase(command);
    }

    /**
     * Returns true if command short name equals DWR or DWA.
     *
     * @param command - String command short name
     * @return - boolean
     */
    public static boolean isWatchdogCommand(String command) {
        return "DWR".equalsIgnoreCase(command) || "DWA".equalsIgnoreCase(command);
    }

    /**
     * Returns true if command short name equals SLR or SLA.
     *
     * @param command - String command short name
     * @return - boolean
     */
    public static boolean isSpendingLimitCommand(String command) {
        return "SLR".equalsIgnoreCase(command) || "SLA".equalsIgnoreCase(command);
    }

    /**
     * Returns true if command short name equals SNR or SNA.
     *
     * @param command - String command short name
     * @return - boolean
     */
    public static boolean isStatusNotificationCommand(String command) {
        return "SNR".equalsIgnoreCase(command) || "SNA".equalsIgnoreCase(command);
    }
}
