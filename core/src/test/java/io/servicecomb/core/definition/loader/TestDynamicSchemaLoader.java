/*
 * Copyright 2017 Huawei Technologies Co., Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.servicecomb.core.definition.loader;

import java.util.ArrayList;
import java.util.Collections;

import org.hamcrest.Matchers;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import io.servicecomb.core.Const;
import io.servicecomb.core.CseContext;
import io.servicecomb.core.definition.MicroserviceMetaManager;
import io.servicecomb.core.definition.SchemaMeta;
import io.servicecomb.core.unittest.UnitTestMeta;
import io.servicecomb.serviceregistry.RegistryUtils;
import io.servicecomb.serviceregistry.ServiceRegistry;
import io.servicecomb.serviceregistry.api.registry.Microservice;
import io.servicecomb.serviceregistry.registry.ServiceRegistryFactory;

public class TestDynamicSchemaLoader {
    private static MicroserviceMetaManager microserviceMetaManager = new MicroserviceMetaManager();

    private static SchemaLoader loader = new SchemaLoader();

    private static Microservice microservice;

    @BeforeClass
    public static void init() {
        UnitTestMeta.init();

        loader.setMicroserviceMetaManager(microserviceMetaManager);

        SchemaListenerManager schemaListenerManager = new SchemaListenerManager();
        schemaListenerManager.setSchemaListenerList(Collections.emptyList());

        CseContext context = CseContext.getInstance();
        context.setSchemaLoader(loader);
        context.setSchemaListenerManager(schemaListenerManager);

        ServiceRegistry serviceRegistry = ServiceRegistryFactory.createLocal();
        serviceRegistry.init();

        microservice = serviceRegistry.getMicroservice();
        RegistryUtils.setServiceRegistry(serviceRegistry);
    }

    @AfterClass
    public static void teardown() {
        RegistryUtils.setServiceRegistry(null);
    }

    @Test
    public void testRegisterSchemas() {
        DynamicSchemaLoader.INSTANCE.registerSchemas("classpath*:test/test/schema.yaml");
        SchemaMeta schemaMeta = microserviceMetaManager.ensureFindSchemaMeta("perfClient", "schema");
        Assert.assertEquals("cse.gen.pojotest.perfClient.schema", schemaMeta.getPackageName());
    }

    @Test
    public void testRegisterShemasAcrossApp() {
        DynamicSchemaLoader.INSTANCE.registerSchemas("CSE:as", "classpath*:test/test/schema.yaml");
        SchemaMeta schemaMeta = microserviceMetaManager.ensureFindSchemaMeta("CSE:as", "schema");
        Assert.assertEquals("cse.gen.CSE.as.schema", schemaMeta.getPackageName());
    }

    @Test
    public void testPutSelfBasePathIfAbsent_noUrlPrefix() {
        System.clearProperty(Const.URL_PREFIX);
        microservice.setPaths(new ArrayList<>());

        loader.putSelfBasePathIfAbsent("perfClient", "/test");

        Assert.assertThat(microservice.getPaths().size(), Matchers.is(1));
        Assert.assertThat(microservice.getPaths().get(0).getPath(), Matchers.is("/test"));
    }

    @Test
    public void testPutSelfBasePathIfAbsent_WithUrlPrefix() {
        System.setProperty(Const.URL_PREFIX, "/root/rest");
        microservice.setPaths(new ArrayList<>());

        loader.putSelfBasePathIfAbsent("perfClient", "/test");

        Assert.assertThat(microservice.getPaths().size(), Matchers.is(1));
        Assert.assertThat(microservice.getPaths().get(0).getPath(), Matchers.is("/root/rest/test"));

        System.clearProperty(Const.URL_PREFIX);
    }

    @Test
    public void testPutSelfBasePathIfAbsent_WithUrlPrefix_StartWithUrlPrefix() {
        System.setProperty(Const.URL_PREFIX, "/root/rest");
        microservice.setPaths(new ArrayList<>());

        loader.putSelfBasePathIfAbsent("perfClient", "/root/rest/test");

        Assert.assertThat(microservice.getPaths().size(), Matchers.is(1));
        Assert.assertThat(microservice.getPaths().get(0).getPath(), Matchers.is("/root/rest/test"));

        System.clearProperty(Const.URL_PREFIX);
    }
}
