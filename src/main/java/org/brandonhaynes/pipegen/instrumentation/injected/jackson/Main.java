package org.brandonhaynes.pipegen.instrumentation.injected.jackson;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import java.io.CharArrayWriter;
import java.io.IOException;
import java.io.Writer;

public class Main {
    public static void main(String[] args) throws IOException {
        Writer writer = new CharArrayWriter();
        //JsonGenerator generator = new JsonFactory().setOutputDecorator(new RemappingOutputDecorator()).createGenerator(writer);
        JsonGenerator generator = new RemappingJsonGeneratorDecorator(new JsonFactory().createGenerator(writer = new RemappingJsonWriter(writer)));

        generator.writeStartObject();
        generator.writeFieldName("foo");
        generator.writeObject(555);
        generator.writeFieldName("bar");
        generator.writeObject("asdf");
        generator.writeEndObject();

        generator.writeStartObject();
        generator.writeFieldName("bar");
        generator.writeObject("asdf");
        generator.writeEndObject();
        generator.flush();

        System.out.println(writer.toString());
    }
}