package com.cumpel.nifi.melt.web.api.sqlexpression;

import javax.servlet.ServletContext;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.inmemory.InMemoryTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import com.bazaarvoice.jolt.JsonUtils;
import com.cumpel.nifi.melt.web.api.processor.TestProcessorResource;
import com.cumpel.nifi.melt.web.api.sqlexpression.dto.DatabaseDTO;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class GetDatabaseInformationTest extends JerseyTest {

    public static final ServletContext servletContext = mock(ServletContext.class);

    @Override
    protected Application configure() {
        final ResourceConfig config = new ResourceConfig();
        config.register(GetDatabaseInformation.class);
        config.register(JacksonFeature.class);
        config.register(new AbstractBinder(){
            @Override
            public void configure() {
                bindFactory(TestProcessorResource.MockServletContext.class).to(ServletContext.class);
            }
        });
        return config;
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new InMemoryTestContainerFactory();
    }

    @Test
    public void testGetDatabaseInformation() {
        DatabaseDTO databaseDTO  = client().target(getBaseUri())
                .path("/melt/ctas/database")
                .request()
                .get(DatabaseDTO.class);

        assertNotNull(databaseDTO);
        System.out.println(getBaseUri());

        System.out.println(JsonUtils.toPrettyJsonString(databaseDTO));
    }
}