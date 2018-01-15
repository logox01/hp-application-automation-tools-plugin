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

package com.hpe.application.automation.tools.pc;

import com.thoughtworks.xstream.XStream;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by bemh on 5/23/2017.
 */
public class TestInstanceCreateRequest {

    private String xmlns = PcRestProxy.PC_API_XMLNS;

    private int testId;
    private int testSetId;

    public TestInstanceCreateRequest(int testId, int testSetId) {
        this.testId = testId;
        this.testSetId = testSetId;
    }

    public String objectToXML() {
        XStream xstream = new XStream();
        xstream.useAttributeFor(TestInstanceCreateRequest.class, "xmlns");
        xstream.alias("TestInstance", TestInstanceCreateRequest.class);
        xstream.aliasField("TestID", TestInstanceCreateRequest.class, "testId");
        xstream.aliasField("TestSetID", TestInstanceCreateRequest.class, "testSetId");
        return xstream.toXML(this);
    }

    public int getTestInstanceIDFromResponse(String xml, String getTestInstanceID) throws IOException, SAXException, ParserConfigurationException {


        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        InputSource is = new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8")));
        Document doc = builder.parse(is);
        Element element = doc.getDocumentElement();


        NodeList nListTrendedRun = doc.getElementsByTagName("TestInstanceID");
        return Integer.parseInt(nListTrendedRun.item(0).getTextContent());

    }
}
