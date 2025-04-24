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

package org.qubership.automation.diameter;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class HexDumpReader {
    public static ByteBuffer read(String resourcePath, String fileName) throws IOException, URISyntaxException {
        List<String> list = Files.readAllLines(
                Paths.get(
                        Objects.requireNonNull(
                                HexDumpReader.class.getResource(resourcePath + sanitizePathTraversal(fileName))
                        ).toURI()
                )
        );
        ByteBuffer data = ByteBuffer.allocate(1024);
        list.forEach(line -> {
            for (String input : line.trim().split("\\s+")) {
                data.put((byte) Integer.parseInt(input.trim(), 16));
            }
        });
        data.flip();
        return data.slice();
    }

    private static String sanitizePathTraversal(String filename) {
        Path p = Paths.get(filename);
        return p.getFileName().toString();
    }
}
