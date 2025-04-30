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

    private final Map<Object, Command> requests = new HashMap<>();
    private final Map<Object, Command> answers = new HashMap<>();

    @Override
    public Command getRequest(final int commandId) {
        return getCommandIfRequest(commandId);
    }

    @Override
    public Command getRequest(final String name) {
        return getCommandIfRequest(name);
    }

    @Override
    public boolean isRequest(final String name) {
        return Objects.nonNull(requests.get(name));
    }

    @Override
    public Command getAnswer(final int commandId) {
        return getCommandIfAnswer(commandId);
    }

    @Override
    public Command getAnswer(final String name) {
        return getCommandIfAnswer(name);
    }


    @Override
    public void add(final Command command) {
        if (command.isRequestTag()) {
            put(command, this.requests);
        } else {
            put(command, this.answers);
        }
    }

    @Override
    public boolean containsRequest(final int code) {
        return requests.containsKey(code);
    }

    @Override
    public boolean containsAnswer(final int code) {
        return answers.containsKey(code);
    }

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
