package com.d3x.morpheus.avro;

import java.util.Collections;

import org.apache.avro.Schema;
import org.apache.avro.SchemaBuilder;

public class AvroTest {

    public static void main(String[] args) {
        Schema schema = SchemaBuilder.record("AvroHttpRequest")
            .namespace("com.baeldung.avro")
            .fields().requiredLong("requestTime")
            .name("clientIdentifier")
            .type("string")
            .noDefault()
            .name("employeeNames")
            .type()
            .array()
            .items()
            .nullable()
            .stringType()
            .arrayDefault(Collections.emptyList())
            .name("active")
            .type()
            .enumeration("Active")
            .symbols("YES","NO")
            .noDefault()
            .endRecord();

        System.out.println(schema.toString());
    }
}
