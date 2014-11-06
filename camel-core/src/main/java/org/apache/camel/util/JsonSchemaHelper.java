/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class for <a href="http://json-schema.org/">JSON schema</a>.
 */
public final class JsonSchemaHelper {

    private static final Pattern PATTERN = Pattern.compile("\"(.+?)\"");

    private JsonSchemaHelper() {
    }

    /**
     * Gets the JSon schema type.
     *
     * @param   type the java type
     * @return  the json schema type, is never null, but returns <tt>object</tt> as the generic type
     */
    public static String getType(Class<?> type) {
        if (type.isEnum()) {
            return "enum";
        } else if (type.isArray()) {
            return "array";
        }

        String primitive = getPrimitiveType(type);
        if (primitive != null) {
            return primitive;
        }

        return "object";
    }

    /**
     * Gets the JSon schema primitive type.
     *
     * @param   type the java type
     * @return  the json schema primitive type, or <tt>null</tt> if not a primitive
     */
    public static String getPrimitiveType(Class<?> type) {
        String name = type.getCanonicalName();

        // special for byte[] or Object[] as its common to use
        if ("java.lang.byte[]".equals(name) || "byte[]".equals(name)) {
            return "string";
        } else if ("java.lang.Byte[]".equals(name) || "Byte[]".equals(name)) {
            return "array";
        } else if ("java.lang.Object[]".equals(name) || "Object[]".equals(name)) {
            return "array";
        } else if ("java.lang.String[]".equals(name) || "String[]".equals(name)) {
            return "array";
            // and these is common as well
        } else if ("java.lang.String".equals(name) || "String".equals(name)) {
            return "string";
        } else if ("java.lang.Boolean".equals(name) || "Boolean".equals(name)) {
            return "boolean";
        } else if ("boolean".equals(name)) {
            return "boolean";
        } else if ("java.lang.Integer".equals(name) || "Integer".equals(name)) {
            return "integer";
        } else if ("int".equals(name)) {
            return "integer";
        } else if ("java.lang.Long".equals(name) || "Long".equals(name)) {
            return "integer";
        } else if ("long".equals(name)) {
            return "integer";
        } else if ("java.lang.Short".equals(name) || "Short".equals(name)) {
            return "integer";
        } else if ("short".equals(name)) {
            return "integer";
        } else if ("java.lang.Byte".equals(name) || "Byte".equals(name)) {
            return "integer";
        } else if ("byte".equals(name)) {
            return "integer";
        } else if ("java.lang.Float".equals(name) || "Float".equals(name)) {
            return "number";
        } else if ("float".equals(name)) {
            return "number";
        } else if ("java.lang.Double".equals(name) || "Double".equals(name)) {
            return "number";
        } else if ("double".equals(name)) {
            return "number";
        }

        return null;
    }

    /**
     * Extracts the description value from the blob of json with the given property name
     *
     * @param json the blob of json
     * @param name the name of the property to extract the description
     * @return the value of the description, or <tt>null</tt> if no description exists
     */
    public static String getDescription(String json, String name) {
        // we dont have a json parser, but we know the structure, so just do this simple way
        String[] lines = json.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.startsWith("\"" + name + "\":")) {
                // grab text after description
                String value = ObjectHelper.after(line, "\"description\": \"");
                if (value != null) {
                    int lastQuote = value.lastIndexOf('"');
                    value = value.substring(0, lastQuote);
                    value = StringHelper.removeLeadingAndEndingQuotes(value);
                    return value;
                }
            }
        }
        return null;
    }

    /**
     * Parses the endpoint explain json
     *
     * @param json the json
     * @return a list of all the options, where each row contains: <tt>key, value, description</tt>
     */
    public static List<String[]> parseEndpointExplainJson(String json) {
        List<String[]> answer = new ArrayList<>();
        if (json == null) {
            return answer;
        }

        // parse line by line
        // skip first 2 lines as they are leading
        String[] lines = json.split("\n");
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i];

            Matcher matcher = PATTERN.matcher(line);
            String option = null;
            String value = null;
            String description = null;
            int count = 0;
            while (matcher.find()) {
                count++;
                if (count == 1) {
                    option = matcher.group(1);
                } else if (count == 3) {
                    value = matcher.group(1);
                } else if (count == 5) {
                    description = matcher.group(1);
                }
            }

            if (option != null) {
                String[] row = new String[]{option, value, description};
                answer.add(row);
            }
        }

        return answer;
    }

}