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

package com.hpe.application.automation.tools.octane.tests.gherkin;

import com.hpe.application.automation.tools.octane.tests.junit.TestResultStatus;
import com.hpe.application.automation.tools.octane.tests.testResult.TestResult;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class GherkinTestResultsCollectorTest {
    String defaultResourceRelativePath = "f1";
    String defaultResourceName = "OctaneGherkinResults0.xml";

    private String getDefaultRootResourceFolder(){
        return getRootResourceFolder(defaultResourceRelativePath,defaultResourceName);
    }

    private String getRootResourceFolder(String resourceRelativePath,String resourceName){
        String resource;
        if(resourceRelativePath.isEmpty()){
            resource = resourceName;
        } else {
            resource = resourceRelativePath + "/" + resourceName;
        }
        URL url = getClass().getResource(resource);
        String path = url.getPath();
        return path.substring(0,path.lastIndexOf(resourceName)-1);
    }

    @Test
    public void testConstruct() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector.collectGherkinTestsResults(new File(getDefaultRootResourceFolder()));
    }

    @Test
    public void testGetResults() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        List<TestResult> gherkinTestsResults = GherkinTestResultsCollector.collectGherkinTestsResults(new File(getDefaultRootResourceFolder()));
        Assert.assertEquals(3,gherkinTestsResults.size());
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(0),"test Feature1",21, TestResultStatus.FAILED);
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(1),"test Feature10",21,TestResultStatus.FAILED);
        validateGherkinTestResult((GherkinTestResult)gherkinTestsResults.get(2),"test Feature2",21,TestResultStatus.PASSED);
    }

    @Test (expected=IllegalArgumentException.class)
    public void testXmlHasNoVersion() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector.collectGherkinTestsResults(new File(getRootResourceFolder("f2",defaultResourceName)));
    }

    @Test (expected=IllegalArgumentException.class)
    public void testXmlHasHigherVersion() throws InterruptedException, ParserConfigurationException, IOException, SAXException, TransformerException {
        GherkinTestResultsCollector.collectGherkinTestsResults(new File(getRootResourceFolder("f3",defaultResourceName)));
    }

    private void validateGherkinTestResult(GherkinTestResult gherkinTestResult, String name, long duration, TestResultStatus status){
        validateAttributes(gherkinTestResult, name, duration, status);
        Assert.assertNotNull(gherkinTestResult.getXmlElement());
    }

    private void validateAttributes(GherkinTestResult gherkinTestResult, String name, long duration, TestResultStatus status){
        Map<String, String> attributes = gherkinTestResult.getAttributes();
        Assert.assertEquals(name,attributes.get("name"));
        Assert.assertEquals(String.valueOf(duration),attributes.get("duration"));
        Assert.assertEquals(status.toPrettyName(),attributes.get("status"));
    }
}
