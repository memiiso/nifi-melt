/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cumpel.nifi.processors.melt;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.InputStreamCallback;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.processors.standard.ExecuteSQL;
import org.apache.nifi.processors.standard.ListDatabaseTables;
import org.apache.nifi.processors.standard.QueryDatabaseTable;
import org.apache.nifi.util.StopWatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.apache.nifi.processors.standard.util.JdbcCommon.DEFAULT_PRECISION;
import static org.apache.nifi.processors.standard.util.JdbcCommon.DEFAULT_SCALE;
import static org.apache.nifi.processors.standard.util.JdbcCommon.NORMALIZE_NAMES_FOR_AVRO;
import static org.apache.nifi.processors.standard.util.JdbcCommon.USE_AVRO_LOGICAL_TYPES;

@Tags({"sql", "select", "jdbc", "query", "database"})
@CapabilityDescription("Provide a description")
@SeeAlso({QueryDatabaseTable.class, ExecuteSQL.class, ListDatabaseTables.class})
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({
        @WritesAttribute(attribute="extractLoadTransform.row.count", description = "Contains the number of rows returned in the select query"),
        @WritesAttribute(attribute="extractLoadTransform.query.duration", description = "Duration of the query in milliseconds"),
        @WritesAttribute(attribute="extractLoadTransform.query", description = "Ctas Query")
})
public class ExtractLoadTransform extends AbstractProcessor {

    public static final String RESULT_ROW_COUNT = "extractLoadTransform.row.count";
    public static final String RESULT_QUERY_DURATION = "extractLoadTransform.query.duration";


    public static final PropertyDescriptor MY_PROPERTY = new PropertyDescriptor
            .Builder().name("MY_PROPERTY")
            .displayName("My property")
            .description("Example Property")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .build();

    public static final Relationship MY_RELATIONSHIP = new Relationship.Builder()
            .name("MY_RELATIONSHIP")
            .description("Example relationship")
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully created FlowFile from SQL query result set.")
            .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("SQL query execution failed. Incoming FlowFile will be penalized and routed to this relationship")
            .build();

    public static final PropertyDescriptor DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();

    public static final PropertyDescriptor DATABASE_NAME = new PropertyDescriptor.Builder()
            .name("Database Name")
            .description("The name of the database to be queried.")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor QUERY_TIMEOUT = new PropertyDescriptor.Builder()
            .name("Max Wait Time")
            .description("The maximum amount of time allowed for a running SQL select query "
                    + ", zero means there is no limit. Max time less than 1 second will be equal to zero.")
            .defaultValue("0 seconds")
            .required(true)
            .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    public static final PropertyDescriptor SQL_SELECT_QUERY = new PropertyDescriptor.Builder()
            .name("SQL select query")
            .description("The SQL select query to execute. The query can be empty, a constant value, or built from attributes "
                    + "using Expression Language. If this property is specified, it will be used regardless of the content of "
                    + "incoming flowfiles. If this property is empty, the content of the incoming flow file is expected "
                    + "to contain a valid SQL select query, to be issued by the processor to the database. Note that Expression "
                    + "Language is not evaluated for flow file contents.")
            .required(false)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(true)
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(DBCP_SERVICE);
        descriptors.add(DATABASE_NAME);
        descriptors.add(SQL_SELECT_QUERY);
        descriptors.add(QUERY_TIMEOUT);
        descriptors.add(MY_PROPERTY);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(MY_RELATIONSHIP);
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {
        /*
        // If the query is not set, then an incoming flow file is needed. Otherwise fail the initialization
        if (!context.getProperty(SQL_SELECT_QUERY).isSet() && !context.hasIncomingConnection()) {
            final String errorString = "Either the Select Query must be specified or there must be an incoming connection "
                    + "providing flowfile(s) containing a SQL select query";
            getLogger().error(errorString);
            throw new ProcessException(errorString);
        } */
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if ( flowFile == null ) {
            return;
        }

        final ComponentLog logger = getLogger();
        final DBCPService dbcpService = context.getProperty(DBCP_SERVICE).asControllerService(DBCPService.class);
        final Integer queryTimeout = context.getProperty(QUERY_TIMEOUT).asTimePeriod(TimeUnit.SECONDS).intValue();
        final boolean convertNamesForAvro = context.getProperty(NORMALIZE_NAMES_FOR_AVRO).asBoolean();
        final Boolean useAvroLogicalTypes = context.getProperty(USE_AVRO_LOGICAL_TYPES).asBoolean();
        final Integer defaultPrecision = context.getProperty(DEFAULT_PRECISION).evaluateAttributeExpressions().asInteger();
        final Integer defaultScale = context.getProperty(DEFAULT_SCALE).evaluateAttributeExpressions().asInteger();
        final StopWatch stopWatch = new StopWatch(true);
        final String selectQuery;

        if (context.getProperty(SQL_SELECT_QUERY).isSet()) {
            selectQuery = context.getProperty(SQL_SELECT_QUERY).evaluateAttributeExpressions(flowFile).getValue();
        } else {
            // If the query is not set, then an incoming flow file is required, and expected to contain a valid SQL select query.
            // If there is no incoming connection, onTrigger will not be called as the processor will fail when scheduled.
            final StringBuilder queryContents = new StringBuilder();
            session.read(flowFile, new InputStreamCallback() {
                @Override
                public void process(InputStream in) throws IOException {
                    queryContents.append(IOUtils.toString(in));
                }
            });
            selectQuery = queryContents.toString();
        }
    }
}
