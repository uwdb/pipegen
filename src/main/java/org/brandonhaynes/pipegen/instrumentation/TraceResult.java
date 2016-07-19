package org.brandonhaynes.pipegen.instrumentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;

import java.io.IOException;

public class TraceResult {
    private final ArrayNode root;

    public TraceResult(String traceData) throws IOException {
        this.root = (ArrayNode) new ObjectMapper().readTree(parseEntries(traceData));
    }

    private static String parseEntries(String data) {
        StringBuilder builder = new StringBuilder();
        String[] entries = data.split("Entry:\n");

        builder.append("[\n");
        for (int i = 0; i < entries.length; i++)
            if (!entries[i].equals(""))
                builder.append(parseEntry(entries[i]))
                       .append(i < entries.length - 1 ? ",\n" : "");
        builder.append("\n]");

        return builder.toString();
    }

    private static StringBuilder parseEntry(String entry) {
        StringBuilder builder = new StringBuilder();
        String[] lines = entry.split("\n");

        builder.append("{");
        if (lines.length > 0)
            createAttribute(builder, "class", lines[0].replace("class ", ""), null, true);
        if (lines.length > 1)
            createAttribute(builder, "line", coalesce(lines[1], "-1"), ",", false);
        if (lines.length > 2)
            createAttribute(builder, "arguments", parseArguments(lines[2]), ",", false);
        if (lines.length > 3)
            createAttribute(builder, "state", parseState(lines[3]), ",", false);
        if (lines.length > 4)
            createAttribute(builder, "stack", parseStack(lines, 4), ",", false);
        builder.append("}");

        return builder;
    }

    private static void createAttribute(StringBuilder builder,
                                       String name, Object value,
                                       String prefix, Boolean quoteValue) {
        builder.append(coalesce(prefix, ""))
                .append("\"").append(name).append("\": ")
                .append(quoteValue ? quote(value) : value)
                .append("\n");
    }

    private static StringBuilder parseArguments(String argumentLine) {
        StringBuilder builder = new StringBuilder();
        String[] arguments = argumentLine.replaceAll("^\\[|\\]$", "").split(",");

        builder.append("[");
        for (int i = 0; i < arguments.length - 2; i++)
            builder.append("\"")
                   .append(escape(arguments[i]))
                   .append("\"")
                   .append(trailingComma(i + 2, arguments));
        builder.append("]");

        return builder;
    }

    private static StringBuilder parseState(String stateLine) {
        StringBuilder builder = new StringBuilder();
        String[] pairs = stateLine.replaceAll("^\\{|\\}$", "").split(",");

        builder.append("{");
        for (int i = 0; i < pairs.length - 1; i++) {
            String[] keyValue = pairs[i].split("=", 2);
            builder.append("\"")
                    .append(escape(keyValue[0].trim()))
                    .append("\": ")
                    .append(quote(keyValue[1]))
                    .append(trailingComma(i + 1, pairs));
        }
        builder.append("}");

        return builder;
    }

    private static StringBuilder parseStack(String[] stack, int startIndex) {
        StringBuilder builder = new StringBuilder();

        builder.append("[");
        for (int i = startIndex; i < stack.length; i++)
            builder.append("\"")
                   .append(escape(stack[i]))
                   .append("\"")
                   .append(trailingComma(i, stack));
        builder.append("]");

        return builder;
    }

    private static String escape(String value) {
        return value.replace("\"", "\\\"");
    }

    private static String trailingComma(int i, String[] values) {
        return i < values.length - 1 ? ", " : "";
    }

    private static String quote(Object value) {
        return !value.equals("null")
                ? ("\"" + value + "\"")
                : value.toString();
    }

    private static String coalesce(String... values) {
        for(String v: values)
            if(!Strings.isNullOrEmpty(v))
                return v;
        return "";
    }

    public ArrayNode getRoot() {
        return root;
    }
}