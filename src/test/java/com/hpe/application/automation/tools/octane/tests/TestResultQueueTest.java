/*
 * © Copyright 2013 EntIT Software LLC
 *  Certain versions of software and/or documents (“Material”) accessible here may contain branding from
 *  Hewlett-Packard Company (now HP Inc.) and Hewlett Packard Enterprise Company.  As of September 1, 2017,
 *  the Material is now offered by Micro Focus, a separately owned and operated company.  Any reference to the HP
 *  and Hewlett Packard Enterprise/HPE marks is historical in nature, and the HP and Hewlett Packard Enterprise/HPE
 *  marks are the property of their respective owners.
 * __________________________________________________________________
 * MIT License
 *
 * Copyright (c) 2018 Micro Focus Company, L.P.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ___________________________________________________________________
 *
 */

package com.hpe.application.automation.tools.octane.tests;

import com.hpe.application.automation.tools.octane.ResultQueue;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

@SuppressWarnings("squid:S2699")
public class TestResultQueueTest {

    private TestsResultQueue queue;

    @Before
    public void init() throws IOException {
        File file = File.createTempFile("TestResultQueueTest", "");
        file.delete();
        queue = new TestsResultQueue(file);
    }

    @Test
    public void testQueue() {
        queue.add("foo", 1);
        ResultQueue.QueueItem item = queue.peekFirst();
        Assert.assertEquals("foo", item.getProjectName());
        Assert.assertEquals(1, item.getBuildNumber());
        Assert.assertEquals(0, item.getFailCount());
    }

    @Test
    public void testAddRemove() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        Assert.assertNotNull(queue.peekFirst());
        Assert.assertEquals(queue.peekFirst(), queue.peekFirst());
        queue.remove();
        Assert.assertNull(queue.peekFirst());
    }

    @Test
    public void testRetry() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        Assert.assertEquals(0, queue.peekFirst().getFailCount());
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().getProjectName());
        Assert.assertEquals(1, queue.peekFirst().getBuildNumber());
        Assert.assertEquals(1, queue.peekFirst().getFailCount());
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().getProjectName());
        Assert.assertEquals(1, queue.peekFirst().getBuildNumber());
        Assert.assertEquals(2, queue.peekFirst().getFailCount());
        Assert.assertTrue(queue.failed());
        Assert.assertEquals("foo", queue.peekFirst().getProjectName());
        Assert.assertEquals(1, queue.peekFirst().getBuildNumber());
        Assert.assertEquals(3, queue.peekFirst().getFailCount());
        Assert.assertFalse(queue.failed());
        Assert.assertNull(queue.peekFirst());
    }

    @Test
    public void testInvalidRemove() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        try {
            queue.remove();
            Assert.fail("should have failed");
        } catch (IllegalStateException e) {
            // expected
        }
    }

    @Test
    public void testInvalidFailed() {
        Assert.assertNull(queue.peekFirst());
        queue.add("foo", 1);
        try {
            queue.failed();
            Assert.fail("should have failed");
        } catch (IllegalStateException e) {
            // expected
        }
    }
}
