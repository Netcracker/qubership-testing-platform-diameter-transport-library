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
    public Command getRequest(int commandId) {
        return getCommandIfRequest(commandId);
    }

    @Override
    public Command getRequest(String name) {
        return getCommandIfRequest(name);
    }

    @Override
    public boolean isRequest(String name) {
        return Objects.nonNull(requests.get(name));
    }

    @Override
    public Command getAnswer(int commandId) {
        return getCommandIfAnswer(commandId);
    }

    @Override
    public Command getAnswer(String name) {
        return getCommandIfAnswer(name);
    }


    @Override
    public void add(Command command) {
        if (command.isRequestTag()) {
            put(command, this.requests);
        } else {
            put(command, this.answers);
        }
    }

    @Override
    public boolean containsRequest(int code) {
        return requests.containsKey(code);
    }

    @Override
    public boolean containsAnswer(int code) {
        return answers.containsKey(code);
    }

    @Override
    public void clear() {
        this.requests.clear();
        this.answers.clear();
    }

    private void put(Command command, Map<Object, Command> table) {
        table.put(command.getId(), command);
        table.put(command.getShortName(), command);
    }

    private Command getCommandIfRequest(Object identifier) {
        return getCommandOfTypeOrThrow(identifier, this.requests, "requests");
    }

    private Command getCommandIfAnswer(Object identifier) {
        return getCommandOfTypeOrThrow(identifier, this.answers, "answers");
    }

    private Command getCommandOfTypeOrThrow(Object identifier, Map<Object, Command> commandsMap, String commandType) {
        Command command = commandsMap.get(identifier);
        if (command == null) {
            throw new IllegalArgumentException(
                    String.format("Command '%s' is not found in '%s' dictionary", identifier, commandType));
        }
        return command;
    }
}
