package com.memiiso.nifi.melt.processors;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.DatumWriter;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.stream.io.ByteArrayOutputStream;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class JOINTest extends AbstractMeltProcessorDerbyTest {

    @Before
    public void setUp() throws Exception {
        testRunner  = TestRunners.newTestRunner(JOIN.class);
        super.setUp();
    }
    @Test
    public void testSingleAvroMessage() throws IOException {
        final Schema schema = new Schema.Parser().parse(new File("src/test/resources/user.avsc"));

        final GenericRecord user1 = new GenericData.Record(schema);
        user1.put("name", "Alyssa");
        user1.put("favorite_number", 256);

        final DatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        final ByteArrayOutputStream out1 = AvroTestUtil.serializeAvroRecord(schema, datumWriter, user1);
        testRunner.enqueue(out1.toByteArray());

        testRunner.run();

        testRunner.assertAllFlowFilesTransferred(JOIN.REL_SUCCESS, 1);
        final MockFlowFile out = testRunner.getFlowFilesForRelationship(JOIN.REL_SUCCESS).get(0);
        out.assertContentEquals("{\"name\": \"Alyssa\", \"favorite_number\": 256, \"favorite_color\": null}");
    }

     @Test
    public void testMeltJoin() throws ClassNotFoundException, SQLException, InitializationException, IOException {

    }
}