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

package org.qubership.automation.diameter.config;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.diameter.avp.AVPDictionary;
import org.qubership.automation.diameter.avp.AVPEntity;
import org.qubership.automation.diameter.avp.AVPProvider;
import org.qubership.automation.diameter.avp.AVPRule;
import org.qubership.automation.diameter.command.Command;
import org.qubership.automation.diameter.data.encoder.Utils;
import org.qubership.automation.diameter.dictionary.DictionaryConfig;
import org.qubership.automation.diameter.dictionary.DictionaryService;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@SuppressWarnings("checkstyle:JavadocVariable")
public abstract class DiameterParser extends DefaultHandler {

    protected static final String FLAG_RULE = "flag-rule";
    protected static final String RULE = "rule";
    protected static final String COMMAND = "command";
    protected static final String REQUEST = "request";
    protected static final String APPLICATION = "application";
    protected static final Pattern SHORT_NAME_PATTER = Pattern.compile("(^[A-z]).*?-([A-z]).*?-([A-z])");
    protected static final String ANSWER = "answer";
    protected static final String HEADER_BIT = "header-bit";
    protected static final String AVP = "avp";
    protected static final String SIMPLE_TYPE = "simple-type";
    protected static final String ENUMERATOR = "enumerator";
    protected static final String AVP_REF = "avp-ref";
    protected static final String REQUIRED = "required";
    protected static final String NAME = "name";
    protected static final String VALUE = "value";
    protected static final String LAYOUT = "layout";
    protected boolean isAvp = false;

    protected Command command;
    protected HashMap<String, String> application;
    protected AVPEntity avp;
    protected boolean isRequired;

    protected DictionaryConfig dictionaryConfig;

    @Override
    public void startElement(String uri, String localName, String tag, Attributes attributes) {
        try {
            startElementProcessing(uri, localName, tag, attributes);
        } catch (Exception e) {
            throw new RuntimeException("Failed parsing of tag:" + tag + '\n' + getAttributes(attributes), e);
        }
    }

    protected abstract void startElementProcessing(final String uri,
                                                   final String localName,
                                                   final String tag,
                                                   final Attributes attributes) throws SAXException;

    @Override
    public void endElement(final String uri, final String localName, final String tag) {
        switch (tag) {
            case APPLICATION:
                this.application = null;
                break;
            case COMMAND:
                DictionaryService.getInstance().getCommandDictionary(dictionaryConfig).add(command);
                break;
            case AVP:
                if (this.avp == null) {
                    break;
                }
                isAvp = false;
                addAvp(avp);
                break;
            case REQUIRED:
                isRequired = false;
                break;
            default:
        }
    }

    protected void addAvp(final AVPEntity avp) {
        AVPDictionary avpDictionary = DictionaryService.getInstance().getAvpDictionary(dictionaryConfig);
        if (avp.getVendorId() > 0) {
            avpDictionary.addVendor(this.avp.getVendorId());
            AVPProvider vendor = avpDictionary.getVendor(this.avp.getVendorId());
            vendor.add(avp);
        } else {
            avpDictionary.add(avp);
        }
    }

    protected String getAttributes(final Attributes attributes) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < attributes.getLength(); index++) {
            builder.append(attributes.getQName(index)).append('=').append(attributes.getValue(index)).append('\n');
        }
        return builder.toString();
    }

    protected void setRules(final Attributes attributes) {
        String name = attributes.getValue(NAME);
        AVPRule rule = AVPRule.of(attributes.getValue(RULE));
        if ("mandatory".equalsIgnoreCase(name)) {
            this.avp.setMandatory(rule);
        } else if ("protected".equalsIgnoreCase(name)) {
            this.avp.setProtect(rule);
        }
    }

    protected void addEnumerated(final AVPEntity avp, final Attributes attributes) {
        String id = attributes.getValue(VALUE);
        String name = attributes.getValue(NAME);
        if (!StringUtils.isNumeric(id)) {
            throw new IllegalStateException(avp.toString() + " is configured wrong. Enumerated hasn't numeric value");
        }
        this.avp.addEnumerated(Utils.parseInt(id), name);
    }

    protected void initAvpAttributes(final Attributes attributes) {
        String id = attributes.getValue("id");
        String vendor = attributes.getValue("vendor");
        String name = attributes.getValue(NAME);
        this.avp.setId(Utils.parseInt(id));
        this.avp.setName(name);
        this.avp.setVendorId(vendor == null ? 0 : Utils.parseInt(vendor));
    }

    protected void initHeaderBit(final Attributes attributes) {
        String name = attributes.getValue(NAME);
        int value = Utils.parseInt(attributes.getValue(VALUE));
        switch (name.toLowerCase()) {
            case "request":
                command.setRequest(value > 0);
                break;
            case "proxiable":
                command.setProxiable(value > 0);
                break;
            case "error":
                command.setError(value > 0);
                break;
            default:
        }
    }

    protected String getShortName(final Attributes attributes) {
        String name = attributes.getValue(NAME);
        if (StringUtils.isBlank(name)) {
            throw new IllegalStateException("Name is not specified");
        }
        Matcher matcher = SHORT_NAME_PATTER.matcher(name);
        StringBuilder builder = new StringBuilder(3);
        if (matcher.find()) {
            builder.append(matcher.group(1)).append(matcher.group(2)).append(matcher.group(3));
        }
        if (builder.length() == 0) {
            throw new IllegalStateException(
                    "Wrong name of diameter command. Name doesn't match '(^[A-z]).*?-([A-z]).*?-([A-z])': " + name);
        }
        return builder.toString();
    }
}
