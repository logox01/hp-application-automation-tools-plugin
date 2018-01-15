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

package com.hpe.application.automation.tools.octane.configuration;

import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import hudson.util.Secret;
import net.jcip.annotations.NotThreadSafe;
import net.sf.json.JSONObject;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;

// import com.gargoylesoftware.htmlunit.WebRequestSettings;
@SuppressWarnings({"squid:S2699","squid:S3658","squid:S2259","squid:S1872","squid:S2925","squid:S109","squid:S1607","squid:S2701","squid:S2698"})
@NotThreadSafe
public class ConfigApiTest {

	@ClassRule
	static final public JenkinsRule rule = new JenkinsRule();
	static final private JenkinsRule.WebClient client = rule.createWebClient();

	@Before
	public void init() throws Exception {
		HtmlPage configPage = client.goTo("configure");
		HtmlForm form = configPage.getFormByName("config");
		form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8008/ui/?p=1001/1002");
		form.getInputByName("_.username").setValueAttribute("username");
		form.getInputByName("_.password").setValueAttribute("password");
		rule.submit(form);
	}

	@Test
	public void testRead() throws Exception {
		Page page = client.goTo("nga/configuration/read", "application/json");
		String configAsString = page.getWebResponse().getContentAsString();
		JSONObject config = JSONObject.fromObject(configAsString);
		Assert.assertEquals("http://localhost:8008", config.getString("location"));
		Assert.assertEquals("1001", config.getString("sharedSpace"));
		Assert.assertEquals("username", config.getString("username"));
		Assert.assertEquals(ConfigurationService.getModel().getIdentity(), config.getString("serverIdentity"));
	}

	@Test
	public void testSave() throws Exception {
		//URL url = client.createCrumbedUrl("nga/configuration/save");
		//WebRequest request = new WebRequest(url,HttpMethod.POST);

		// basic scenario: location, shared space and credentials
		JSONObject config = new JSONObject();
		config.put("location", "http://localhost:8088");
		config.put("sharedSpace", "1001");
		config.put("username", "username1");
		config.put("password", "password1");
		JenkinsRule.WebClient wc = rule.createWebClient();
		WebRequest req = new WebRequest(wc.createCrumbedUrl("nga/configuration/save"), HttpMethod.POST);
		req.setEncodingType(null);
		req.setRequestBody(config.toString());
		Page page = wc.getPage(req);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8088", "1001", "username1", Secret.fromString("password1"));
		Assert.assertEquals(ConfigurationService.getModel().getIdentity(), config.getString("serverIdentity"));


		// location, shared space, no credentials
		config = new JSONObject();
		config.put("location", "http://localhost:8888");
		config.put("sharedSpace", "1002");
		req.setRequestBody(config.toString());
		page = client.getPage(req);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8888", "1002", "username1", Secret.fromString("password1"));
		Assert.assertEquals(ConfigurationService.getModel().getIdentity(), config.getString("serverIdentity"));
//
		// location, shared space and username without password
		config = new JSONObject();
		config.put("location", "http://localhost:8882");
		config.put("sharedSpace", "1003");
		config.put("username", "username3");
		req.setRequestBody(config.toString());
		page = client.getPage(req);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8882", "1003", "username3", Secret.fromString(""));
		Assert.assertEquals(ConfigurationService.getModel().getIdentity(), config.getString("serverIdentity"));

		// uiLocation and identity
		config = new JSONObject();
		config.put("uiLocation", "http://localhost:8881/ui?p=1001/1002");
		config.put("serverIdentity", "2d2fa955-1d13-4d8c-947f-ab11c72bf850");
		req.setRequestBody(config.toString());
		page = client.getPage(req);
		config = JSONObject.fromObject(page.getWebResponse().getContentAsString());
		checkConfig(config, "http://localhost:8881", "1001", "username3", Secret.fromString(""));
		Assert.assertEquals("2d2fa955-1d13-4d8c-947f-ab11c72bf850", config.getString("serverIdentity"));
		Assert.assertEquals("2d2fa955-1d13-4d8c-947f-ab11c72bf850", ConfigurationService.getModel().getIdentity());

		// requires POST
		req.setHttpMethod(HttpMethod.GET);
		try {
			client.getPage(req);
			Assert.fail("Only POST should be allowed");
		} catch (FailingHttpStatusCodeException ex) {
			// expected
		}
	}

	private void checkConfig(JSONObject config, String location, String sharedSpace, String username, Secret password) {
		// check values returned
		Assert.assertEquals(location, config.getString("location"));
		Assert.assertEquals(sharedSpace, config.getString("sharedSpace"));
		Assert.assertEquals(username, config.getString("username"));
		// check values stored
		ServerConfiguration serverConfiguration = ConfigurationService.getServerConfiguration();
		Assert.assertEquals(location, serverConfiguration.location);
		Assert.assertEquals(sharedSpace, serverConfiguration.sharedSpace);
		Assert.assertEquals(username, serverConfiguration.username);
		Assert.assertEquals(password, serverConfiguration.password);
	}
}
