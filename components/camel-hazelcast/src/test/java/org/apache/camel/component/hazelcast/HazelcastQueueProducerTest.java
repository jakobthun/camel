/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.hazelcast;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IQueue;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.builder.RouteBuilder;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;

import static org.mockito.Mockito.*;

public class HazelcastQueueProducerTest extends HazelcastCamelTestSupport {

    @Mock
    private IQueue<String> queue;

    @Override
    protected void trainHazelcastInstance(HazelcastInstance hazelcastInstance) {
        when(hazelcastInstance.<String>getQueue("bar")).thenReturn(queue);
    }

    @Override
    protected void verifyHazelcastInstance(HazelcastInstance hazelcastInstance) {
        verify(hazelcastInstance, atLeastOnce()).getQueue("bar");
    }

    @After
    public void verifyQueueMock() {
        verifyNoMoreInteractions(queue);
    }

    @Test(expected = CamelExecutionException.class)
    public void testWithInvalidOperation() {
        template.sendBody("direct:putInvalid", "foo");
    }

    @Test
    public void put() throws InterruptedException {
        template.sendBody("direct:put", "foo");
        verify(queue).put("foo");
    }

    @Test
    public void putWithOperationNumber() throws InterruptedException {
        template.sendBody("direct:putWithOperationNumber", "foo");
        verify(queue).put("foo");
    }

    @Test
    public void putWithOperationName() throws InterruptedException {
        template.sendBody("direct:putWithOperationName", "foo");
        verify(queue).put("foo");
    }

    @Test
    public void noOperation() {
        template.sendBody("direct:no-operation", "bar");
        verify(queue).add("bar");
    }

    @Test
    public void add() {
        template.sendBody("direct:add", "bar");
        verify(queue).add("bar");
    }

    @Test
    public void offer() {
        template.sendBody("direct:offer", "foobar");
        verify(queue).offer("foobar");
    }

    @Test
    public void removeSpecifiedValue() throws InterruptedException {
        template.sendBody("direct:removevalue", "foo2");
        verify(queue).remove("foo2");
    }

    @Test
    public void removeValue() {
        template.sendBody("direct:removevalue", null);
        verify(queue).remove();
    }

    @Test
    public void poll() throws InterruptedException {
        when(queue.poll()).thenReturn("foo");
        String answer = template.requestBody("direct:poll", null, String.class);
        verify(queue).poll();
        assertEquals("foo", answer);
    }

    @Test
    public void peek() throws InterruptedException {
        when(queue.peek()).thenReturn("foo");
        String answer = template.requestBody("direct:peek", null, String.class);
        verify(queue).peek();
        assertEquals("foo", answer);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:no-operation").to(String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:putInvalid").setHeader(HazelcastConstants.OPERATION, constant("bogus")).to(String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:put").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PUT_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:add").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.ADD_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:offer").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.OFFER_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:poll").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.POLL_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:peek").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.PEEK_OPERATION)).to(String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:removevalue").setHeader(HazelcastConstants.OPERATION, constant(HazelcastConstants.REMOVEVALUE_OPERATION)).to(
                        String.format("hazelcast:%sbar", HazelcastConstants.QUEUE_PREFIX));

                from("direct:putWithOperationNumber").toF(String.format("hazelcast:%sbar?operation=%s", HazelcastConstants.QUEUE_PREFIX, HazelcastConstants.PUT_OPERATION));

                from("direct:putWithOperationName").toF(String.format("hazelcast:%sbar?operation=put", HazelcastConstants.QUEUE_PREFIX));
            }
        };
    }

}
