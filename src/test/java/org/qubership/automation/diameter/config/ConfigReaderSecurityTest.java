package org.qubership.automation.diameter.config;

import java.io.File;
import java.io.FileWriter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.xml.sax.SAXParseException;

class ConfigReaderSecurityTest {

    @TempDir
    File tempDir;

    @Test
    void shouldBlockDoctypeInDictionaryFiles() throws Exception {
        // Create a malicious dictionary file with DOCTYPE
        File dictionaryFile = new File(tempDir, "malicious.xml");
        try (FileWriter writer = new FileWriter(dictionaryFile)) {
            writer.write("<?xml version=\"1.0\"?>\n");
            writer.write("<!DOCTYPE avp [\n");
            writer.write("  <!ENTITY test SYSTEM \"file:///etc/passwd\">\n");
            writer.write("]>\n");
            writer.write("<avp name=\"Test\" code=\"999\" vendor=\"0\"/>\n");
        }

        DictionaryConfig config = new DictionaryConfig(
                tempDir.getAbsolutePath(),
                StandardParser.class,
                null
        );

        // Should throw exception due to DOCTYPE being disallowed
        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> ConfigReader.read(config, false));
        Throwable cause = exception.getCause();
        Assertions.assertNotNull(cause);
        Assertions.assertInstanceOf(SAXParseException.class, cause);
        Assertions.assertEquals("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.",
                cause.getMessage());
    }

}