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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CommandDictionary implements CommandProvider {

    /**
     * Map of request commands.
     */
    private final Map<Object, Command> requests = new HashMap<>();

    /**
     * Map of answer commands.
     */
    private final Map<Object, Command> answers = new HashMap<>();

    /**
     * Get Request Command by ID.
     *
     * @param commandId Command ID to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    @Override
    public Command getRequest(final int commandId) {
        return getCommandIfRequest(commandId);
    }

    /**
     * Get Request Command by name.
     *
     * @param name Command name to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    @Override
    public Command getRequest(final String name) {
        return getCommandIfRequest(name);
    }

    /**
     * Check if requests map contains element by the name.
     *
     * @param name Command name to search
     * @return true if requests map contains element by the name, otherwise false.
     */
    @Override
    public boolean isRequest(final String name) {
        return Objects.nonNull(requests.get(name));
    }

    /**
     * Get Answer Command by id.
     *
     * @param commandId Command ID to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    @Override
    public Command getAnswer(final int commandId) {
        return getCommandIfAnswer(commandId);
    }

    /**
     * Get Answer Command by name.
     *
     * @param name Command Name to search
     * @return Command if found, otherwise IllegalArgumentException is thrown.
     */
    @Override
    public Command getAnswer(final String name) {
        return getCommandIfAnswer(name);
    }

    /**
     * Add the command into requests or answers map, depending on .isRequestTag() value.
     *
     * @param command Command to add.
     */
    @Override
    public void add(final Command command) {
        if (command.isRequestTag()) {
            put(command, this.requests);
        } else {
            put(command, this.answers);
        }
    }

    /**
     * Check if requests map contains this code as key.
     *
     * @param code Command ID to check
     * @return true if requests map contains this code as key, otherwise false.
     */
    @Override
    public boolean containsRequest(final int code) {
        return requests.containsKey(code);
    }

    /**
     * Check if answers map contains this code as key.
     *
     * @param code Command ID to check
     * @return true if answers map contains this code as key, otherwise false.
     */
    @Override
    public boolean containsAnswer(final int code) {
        return answers.containsKey(code);
    }

    /**
     * Clear Requests Map and Answers Map.
     */
    @Override
    public void clear() {
        this.requests.clear();
        this.answers.clear();
    }

    private void put(final Command command, final Map<Object, Command> table) {
        table.put(command.getId(), command);
        table.put(command.getShortName(), command);
    }

    private Command getCommandIfRequest(final Object identifier) {
        return getCommandOfTypeOrThrow(identifier, this.requests, "requests");
    }

    private Command getCommandIfAnswer(final Object identifier) {
        return getCommandOfTypeOrThrow(identifier, this.answers, "answers");
    }

    private Command getCommandOfTypeOrThrow(final Object identifier,
                                            final Map<Object, Command> commandsMap,
                                            final String commandType) {
        Command command = commandsMap.get(identifier);
        if (command == null) {
            throw new IllegalArgumentException(
                    String.format("Command '%s' is not found in '%s' dictionary", identifier, commandType));
        }
        return command;
    }
}
