/*
 * Copyright (c) 2018. Memiiso
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.memiiso.nifi.melt.processors;

import com.memiiso.nifi.melt.db.MeltDBConnection;
import com.memiiso.nifi.melt.dbcp.MeltDBCPService;
import org.apache.nifi.annotation.behavior.*;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnStopped;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;


@EventDriven
@TriggerSerially
@TriggerWhenEmpty
@InputRequirement(Requirement.INPUT_ALLOWED)
@Tags({ "sql", "select", "jdbc", "query", "database" })
@WritesAttributes({
        @WritesAttribute(attribute = "melt.query.duration", description = "Duration of the query in milliseconds"),
        @WritesAttribute(attribute = "melt.query", description = "Melt Query") })
public abstract class AbstractMeltProcessor extends AbstractProcessor {

    // Relationships
    public static final Relationship REL_SUCCESS =
            new Relationship.Builder().name("success").description("Successfully created FlowFile from SQL query result set.").build();
    public static final Relationship REL_FAILURE = new Relationship.Builder().name("failure")
            .description("SQL query execution failed. Incoming FlowFile will be penalized and routed to this relationship").build();
    Set<Relationship> relationships;

    // Properties
    public static final PropertyDescriptor MELT_DBCP_SERVICE = new PropertyDescriptor.Builder()
            .name("melt_dbcp_service")
            .displayName("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(MeltDBCPService.class).build();
    public static final PropertyDescriptor MELT_QUERY_TIMEOUT = new PropertyDescriptor.Builder()
            .name("melt_query_timeout")
            .displayName("Query Timeout")
            .description("The maximum amount of time allowed for a running SQL select query "
                    + " , zero means there is no limit. Max time less than 1 second will be equal to zero.")
            .defaultValue("0 seconds")
            .required(true)
            .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
            .sensitive(false)
            .build();
    public static final PropertyDescriptor MELT_TARGET_TABLE = new PropertyDescriptor.Builder()
            .name("melt_target_table")
            .displayName("Target Table Name")
            .description("Table table name (including schema). ex. stg.my_table_name")
            .required(true)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR).build();
    public static final PropertyDescriptor MELT_SOURCE = new PropertyDescriptor.Builder()
            .name("melt_source")
            .displayName("Data Source")
            .description("Select Statement or a table.")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_TEMPLATE = new PropertyDescriptor.Builder()
            .name("melt_elt_template")
            .displayName("ELT Template")
            .description("${melt_target_table} is the Table Name and ${melt_source} is SQL Select StatEment.")
            .defaultValue("DROP TABLE IF EXISTS ${melt_target_table};\nCREATE TABLE ${melt_target_table} AS \n ${melt_source}")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();
    public static final PropertyDescriptor MELT_ELT_STATEMENT = new PropertyDescriptor.Builder()
            .name("melt_elt_statement")
            .displayName("Final ELT Statement.")
            .description("Final ELt Statement(* generated).")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR).required(true).build();

    List<PropertyDescriptor> propDescriptors;

    //TODO
    public void setRelationships() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);
    }

    public ConcurrentHashMap<String,PropertyDescriptor> getMeltCommonPropertyDescriptors() {
        final ConcurrentHashMap<String,PropertyDescriptor> pds = new ConcurrentHashMap<String,PropertyDescriptor>();
        pds.put(MELT_DBCP_SERVICE.getName(),MELT_DBCP_SERVICE);
        pds.put(MELT_QUERY_TIMEOUT.getName(),MELT_QUERY_TIMEOUT);
        pds.put(MELT_TARGET_TABLE.getName(),MELT_TARGET_TABLE);
        pds.put(MELT_SOURCE.getName(),MELT_SOURCE);
        pds.put(MELT_TEMPLATE.getName(),MELT_TEMPLATE);
        pds.put(MELT_ELT_STATEMENT.getName(),MELT_ELT_STATEMENT);
        return pds;
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }


    // TODO review . close connections when done!?
    @OnStopped
    public final void closeConn(ProcessContext context) {
    }

    public ConcurrentHashMap<String, String> getDynamicProperties(ProcessContext context) {
        final ConcurrentHashMap<String, String> dynamicProperties = new ConcurrentHashMap<>();
        for (final ConcurrentHashMap.Entry<PropertyDescriptor, String> entry : context.getProperties().entrySet()) {
            if (entry.getKey().isDynamic()) {
                dynamicProperties.put(entry.getKey().getName(), entry.getValue());
            }
        }
        return dynamicProperties;
    }

    @Override
    public void onPropertyModified(final PropertyDescriptor descriptor, final String oldValue, final String newValue) {
        //TODO save metadata.
        super.onPropertyModified( descriptor,  oldValue, newValue);
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        this.getLogger().info(this.getClass().getSimpleName() + " processor Triggered");
        // TODO review if
        FlowFile fileToProcess = session.get();
        if(fileToProcess == null){
            fileToProcess = session.create();
        }

        long duration = 0;
        String MEltQuery = null;

        // processors implementation - here we go
        final MeltDBCPService dbcpService = context.getProperty(MELT_DBCP_SERVICE).asControllerService(MeltDBCPService.class);
        final Integer queryTimeout = context.getProperty(MELT_QUERY_TIMEOUT).asTimePeriod(TimeUnit.SECONDS).intValue();
        final StopWatch stopWatch = new StopWatch(true);

        try (final MeltDBConnection meltDb = dbcpService.getMeltDBConnection(); final Statement st = meltDb.getConnection().createStatement()) {

            MEltQuery = context.getProperty(MELT_ELT_STATEMENT).getValue();
            fileToProcess = session.putAttribute(fileToProcess, "melt.query", MEltQuery);

            st.setQueryTimeout(queryTimeout); // timeout in seconds
            this.getLogger().debug("Executing query '{}'", new Object[] { MEltQuery });
            boolean results = st.execute(MEltQuery);
            duration = stopWatch.getElapsed(TimeUnit.MILLISECONDS);
            fileToProcess = session.putAttribute(fileToProcess, "melt.query.duration", String.valueOf(duration));

            // pass the original flow file down the line to trigger downstream processors
            session.transfer(fileToProcess, REL_SUCCESS);
            // @TODO Review session.commit below.
            session.commit();
        } catch (final ProcessException | SQLException e) {
            this.getLogger().error("Unable to execute SQL query '{}' due to '{}'; routing to failure", new Object[] { MEltQuery, e });
            fileToProcess = session.putAttribute(fileToProcess, "melt.query.duration", String.valueOf(duration));
            context.yield();
            session.transfer(fileToProcess, REL_FAILURE);
        }
    }

}
