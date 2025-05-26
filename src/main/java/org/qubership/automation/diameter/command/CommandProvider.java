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

public interface CommandProvider {

    /**
     * Get Request Command by ID.
     *
     * @param commandId Command ID to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    Command getRequest(int commandId);

    /**
     * Get Request Command by name.
     *
     * @param name Command name to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    Command getRequest(String name);

    /**
     * Check if requests map contains element by the name.
     *
     * @param name Command name to search
     * @return true if requests map contains element by the name, otherwise false.
     */
    boolean isRequest(String name);

    /**
     * Get Answer Command by id.
     *
     * @param commandId Command ID to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    Command getAnswer(int commandId);

    /**
     * Get Answer Command by name.
     *
     * @param name Command Name to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    Command getAnswer(String name);

    /**
     * Add the command into Commands Dictionary.
     *
     * @param command Command to add.
     */
    void add(Command command);

    /**
     * Check if requests map contains this code as key.
     *
     * @param code Command ID to check
     * @return true if requests map contains this code as key, otherwise false.
     */
    boolean containsRequest(int code);

    /**
     * Check if answers map contains this code as key.
     *
     * @param code Command ID to check
     * @return true if answers map contains this code as key, otherwise false.
     */
    boolean containsAnswer(int code);

    /**
     * Clear Requests Map and Answers Map.
     */
    void clear();

}
