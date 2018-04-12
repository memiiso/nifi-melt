package com.memiiso.nifi.melt.processors;

import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.flowfile.attributes.CoreAttributes;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.io.StreamCallback;
import org.apache.nifi.processors.standard.AbstractRecordProcessor;
import org.apache.nifi.schema.access.SchemaNotFoundException;
import org.apache.nifi.serialization.*;
import org.apache.nifi.serialization.record.Record;
import org.apache.nifi.serialization.record.RecordSchema;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@CapabilityDescription("Join")
public class JOIN extends AbstractRecordProcessor {

    /*
        static final PropertyDescriptor RECORD_READER = new PropertyDescriptor.Builder()
                .name("record-reader")
                .displayName("Record Reader")
                .description("Specifies the Controller Service to use for reading incoming data")
                .identifiesControllerService(RecordReaderFactory.class)
                .required(true)
                .build();
        static final PropertyDescriptor RECORD_WRITER = new PropertyDescriptor.Builder()
                .name("record-writer")
                .displayName("Record Writer")
                .description("Specifies the Controller Service to use for writing out the records")
                .identifiesControllerService(RecordSetWriterFactory.class)
                .required(true)
                .build();
*/
        static final Relationship REL_SUCCESS = new Relationship.Builder()
                .name("success")
                .description("FlowFiles that are successfully transformed will be routed to this relationship")
                .build();
        static final Relationship REL_FAILURE = new Relationship.Builder()
                .name("failure")
                .description("If a FlowFile cannot be transformed from the configured input format to the configured output format, "
                        + "the unchanged FlowFile will be routed to this relationship")
                .build();

        @Override
        public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
            FlowFile flowFile = session.get();
            if (flowFile == null) {
                return;
            }
/*
            final RecordReaderFactory readerFactory = context.getProperty(RECORD_READER).asControllerService(RecordReaderFactory.class);
            final RecordSetWriterFactory writerFactory = context.getProperty(RECORD_WRITER).asControllerService(RecordSetWriterFactory.class);

            final Map<String, String> attributes = new HashMap<>();
            final AtomicInteger recordCount = new AtomicInteger();

            final FlowFile original = flowFile;
            final Map<String, String> originalAttributes = flowFile.getAttributes();
            try {
                flowFile = session.write(flowFile, new StreamCallback() {
                    @Override
                    public void process(final InputStream in, final OutputStream out) throws IOException {

                        try (final RecordReader reader = readerFactory.createRecordReader(originalAttributes, in, getLogger())) {

                            final RecordSchema writeSchema = writerFactory.getSchema(originalAttributes, reader.getSchema());
                            try (final RecordSetWriter writer = writerFactory.createWriter(getLogger(), writeSchema, out)) {
                                writer.beginRecordSet();

                                Record record;
                                while ((record = reader.nextRecord()) != null) {
                                    final Record processed = JOIN.this.process(record, writeSchema, original, context);
                                    writer.write(processed);
                                }

                                final WriteResult writeResult = writer.finishRecordSet();
                                attributes.put("record.count", String.valueOf(writeResult.getRecordCount()));
                                attributes.put(CoreAttributes.MIME_TYPE.key(), writer.getMimeType());
                                attributes.putAll(writeResult.getAttributes());
                                recordCount.set(writeResult.getRecordCount());
                            }
                        } catch (final SchemaNotFoundException e) {
                            throw new ProcessException(e.getLocalizedMessage(), e);
                        } catch (final MalformedRecordException e) {
                            throw new ProcessException("Could not parse incoming data", e);
                        }
                    }
                });
            } catch (final Exception e) {
                getLogger().error("Failed to process {}; will route to failure", new Object[] {flowFile, e});
                session.transfer(flowFile, REL_FAILURE);
                return;
            }

            flowFile = session.putAllAttributes(flowFile, attributes);
            session.transfer(flowFile, REL_SUCCESS);

            final int count = recordCount.get();
            session.adjustCounter("Records Processed", count, false);
            getLogger().info("Successfully converted {} records for {}", new Object[] {count, flowFile});
        */
        }

    @Override
    protected Record process(final Record record, final RecordSchema writeSchema, final FlowFile flowFile, final ProcessContext context) {
        return record;
    }

}
