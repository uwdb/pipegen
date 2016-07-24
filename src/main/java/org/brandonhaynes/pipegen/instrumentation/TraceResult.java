package org.brandonhaynes.pipegen.instrumentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.brandonhaynes.pipegen.utilities.AutoCloseableAbstractIterator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class TraceResult {
    private final ArrayNode root;
    private final Path traceFile;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String[] stringArray = new String[0];
    private static final String HEADER = "Entry:";

    public TraceResult(String traceData) throws IOException {
        this.root = (ArrayNode) new ObjectMapper().readTree(parseEntries(traceData));
        this.traceFile = null;
    }

    public TraceResult(Path traceFile) throws IOException {
        this.root = null;
        this.traceFile = traceFile;
    }

    private static String parseEntries(String data) {
        StringBuilder builder = new StringBuilder();
        String[] entries = data.split(HEADER + "\n");

        builder.append("[\n");
        for (int i = 0; i < entries.length; i++)
            if (!entries[i].equals(""))
                builder.append(parseEntry(entries[i]))
                       .append(i < entries.length - 1 ? ",\n" : "");
        builder.append("\n]");

        return builder.toString();
    }

    private static StringBuilder parseEntry(String entry) {
        return parseEntry(entry.split("\n"));
    }
    private static StringBuilder parseEntry(String[] lines) {
        StringBuilder builder = new StringBuilder();

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

    public Iterable<JsonNode> getNodes() {
        return () -> {
            try {
                return new JsonNodeIterator(traceFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public ArrayNode getRoot() { return root; }

    private class JsonNodeIterator extends AutoCloseableAbstractIterator<JsonNode> {
        private final BufferedReader reader;

        JsonNodeIterator(Path traceFile) throws IOException {
            reader = new BufferedReader(new FileReader(traceFile.toFile()));
            if(!reader.readLine().equals(HEADER))
                throw new IOException(String.format("Expected trace file to begin with header '%s'", HEADER));
        }

        @Override
        protected JsonNode computeNext() {
            List<String> lines = Lists.newArrayList();
            String line;

            try {
                while ((line = reader.readLine()) != null && !line.equals(""))
                    if(!line.equals(HEADER))
                        lines.add(line);

                return line != null || !lines.isEmpty()
                        ? objectMapper.readTree(parseEntry(lines.toArray(stringArray)).toString())
                        : closeAndSignalEndOfData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private JsonNode closeAndSignalEndOfData() throws IOException {
            close();
            return endOfData();
        }

        public void close() throws IOException {
            reader.close();
        }
    }
}