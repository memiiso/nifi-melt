package com.cumpel.nifi.melt.processors;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.cumpel.nifi.melt.processors.db.MeltDatabaseAdapter;
import com.cumpel.nifi.melt.processors.db.MeltDatabaseFactory;
import org.apache.nifi.annotation.behavior.EventDriven;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.InputRequirement.Requirement;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.logging.ComponentLog;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.util.StopWatch;


@EventDriven
@InputRequirement(Requirement.INPUT_ALLOWED)
@Tags({"sql", "select", "jdbc", "query", "database"})
@CapabilityDescription("Execute provided SQL select query. Query result will be converted to Avro format."
        + " Streaming is used so arbitrarily large result sets are supported. This processor can be scheduled to run on "
        + "a timer, or cron expression, using the standard scheduling methods, or it can be triggered by an incoming FlowFile. "
        + "If it is triggered by an incoming FlowFile, then attributes of that FlowFile will be available when evaluating the "
        + "select query. FlowFile attribute 'ctas.row.count' indicates how many rows were selected.")
@WritesAttributes({
    @WritesAttribute(attribute="ctas.row.count", description = "Contains the number of rows returned in the select query"),
    @WritesAttribute(attribute="ctas.query.duration", description = "Duration of the query in milliseconds"),
    @WritesAttribute(attribute="ctas.query", description = "Ctas Query")
})
public class CTAS extends AbstractProcessor {

    public static final String RESULT_QUERY_DURATION = "ctas.query.duration";

    // Relationships
    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successfully created FlowFile from SQL query result set.")
            .build();
    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("SQL query execution failed. Incoming FlowFile will be penalized and routed to this relationship")
            .build();
    private final Set<Relationship> relationships;

    public static final PropertyDescriptor CONNECTION_POOL = new PropertyDescriptor.Builder()
            .name("Database Connection Pooling Service")
            .description("The Controller Service that is used to obtain connection to database")
            .required(true)
            .identifiesControllerService(DBCPService.class)
            .build();

    public static final PropertyDescriptor SQL_CTAS_STATEMENT = new PropertyDescriptor.Builder()
            .name("SQL CTAS Select query")
            .description("The SQL select query to execute.")
            .required(true)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            //.expressionLanguageSupported(true)
            .build();
    public static final PropertyDescriptor SQL_CTAS_TABLE = new PropertyDescriptor.Builder()
            .name("SQL CTAS Target Table")
            .description("SQL CTAS Target Table with schema name.")
            .required(true)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();

    public static final PropertyDescriptor QUERY_TIMEOUT = new PropertyDescriptor.Builder()
            .name("Max Wait Time")
            .description("The maximum amount of time allowed for a running SQL select query "
                    + " , zero means there is no limit. Max time less than 1 second will be equal to zero.")
            .defaultValue("0 seconds")
            .required(true)
            .addValidator(StandardValidators.TIME_PERIOD_VALIDATOR)
            .sensitive(false)
            .build();

    private final List<PropertyDescriptor> propDescriptors;

    public CTAS() {
        final Set<Relationship> r = new HashSet<>();
        r.add(REL_SUCCESS);
        r.add(REL_FAILURE);
        relationships = Collections.unmodifiableSet(r);

        final List<PropertyDescriptor> pds = new ArrayList<>();
        pds.add(CONNECTION_POOL);
        pds.add(SQL_CTAS_STATEMENT);
        pds.add(SQL_CTAS_TABLE);
        pds.add(QUERY_TIMEOUT);
        propDescriptors = Collections.unmodifiableList(pds);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return relationships;
    }

    @Override
    protected List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return propDescriptors;
    }

    @OnScheduled
    public void setup(ProcessContext context) {
    	
    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
    	
        FlowFile fileToProcess = null;
        fileToProcess = session.create();
        //if (context.hasIncomingConnection()) {
        //    fileToProcess = session.get();
        //}else {
        //    fileToProcess = session.create();
        //}
        int resultCount=0;
        long duration = 0;
        String CTASQuery = null;
        fileToProcess = session.putAttribute(fileToProcess, "ctas.row.count", String.valueOf(resultCount));
        fileToProcess = session.putAttribute(fileToProcess, "ctas.query.duration", String.valueOf(duration));
        fileToProcess = session.putAttribute(fileToProcess, "ctas.query", String.valueOf(CTASQuery));

        // CTAS implementation - here we go 
        final ComponentLog logger = getLogger();
        final DBCPService dbcpService = context.getProperty(CONNECTION_POOL).asControllerService(DBCPService.class);
        final Integer queryTimeout = context.getProperty(QUERY_TIMEOUT).asTimePeriod(TimeUnit.SECONDS).intValue();
        final StopWatch stopWatch = new StopWatch(true);

        try (final Connection con = dbcpService.getConnection();
            final Statement st = con.createStatement()) {
        		MeltDatabaseAdapter meltDb = MeltDatabaseFactory.getInstance(con);
            CTASQuery = meltDb.getCTASStatement(context.getProperty(SQL_CTAS_STATEMENT).getValue(), context.getProperty(SQL_CTAS_TABLE).getValue());
            fileToProcess = session.putAttribute(fileToProcess, "ctas.query", String.valueOf(CTASQuery));
            
            st.setQueryTimeout(queryTimeout); // timeout in seconds
            logger.debug("Executing query '{}'", new Object[]{CTASQuery});
            boolean results = st.execute(CTASQuery);
            duration = stopWatch.getElapsed(TimeUnit.MILLISECONDS);
           // pass the original flow file down the line to trigger downstream processors
            session.transfer(fileToProcess, REL_SUCCESS);
            // @TODO Review
            //session.commit();
        } catch (final ProcessException | SQLException e) {
            logger.error("Unable to execute SQL CTAS query '{}' due to '{}'; routing to failure",
                    new Object[]{CTASQuery, e});
            context.yield();
            session.transfer(fileToProcess, REL_FAILURE);
        }
    }
}
