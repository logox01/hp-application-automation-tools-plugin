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

import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.hpe.application.automation.tools.octane.tests.ExtensionUtil;
import org.junit.*;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestExtension;

import java.util.ArrayList;
import java.util.List;

public class ConfigurationListenerTest {

	@ClassRule
	static final public JenkinsRule rule = new JenkinsRule();

	private TestConfigurationListener listener;

	@Before
	public void init() throws Exception {
		HtmlPage configPage = rule.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");

		form.getInputByName("_.uiLocation").setValueAttribute("http://localhost:8028/ui/?p=1001/1002");
		form.getInputByName("_.username").setValueAttribute("username");
		form.getInputByName("_.password").setValueAttribute("password");
		rule.submit(form);

		listener = ExtensionUtil.getInstance(rule, TestConfigurationListener.class);
		Assert.assertEquals("Listener count doesn't match 1",1, listener.getCount());

		List<ServerConfiguration> confs = listener.getConfigurationsChange();
		Assert.assertNotNull("Configuration is null",confs);
		Assert.assertEquals("Config size doesn't match 2",2, confs.size());
		Assert.assertEquals("location doesn't match localhost:8020","http://localhost:8028", confs.get(0).location);
		Assert.assertEquals("username doesn't match username","username", confs.get(0).username);
		Assert.assertNull(confs.get(1).location);
		Assert.assertNull(confs.get(1).username);
	}

	@Test
	public void testConfigurationListener() throws Exception {
		HtmlPage configPage = rule.createWebClient().goTo("configure");
		HtmlForm form = configPage.getFormByName("config");
		// password is cleared upon form retrieval (restore the value)
		form.getInputByName("_.password").setValueAttribute("password");

		// not increased on re-submit
		rule.submit(form);
		Assert.assertEquals("Listener count doesn't match 1",1, listener.getCount());

		configPage = rule.createWebClient().goTo("configure");
		form = configPage.getFormByName("config");
		// increased when configuration changes
		form.getInputByName("_.username").setValueAttribute("username2");
		rule.submit(form);
		Assert.assertEquals("Listener count doesn't match 2",2, listener.getCount());

		List<ServerConfiguration> confs = listener.getConfigurationsChange();
		Assert.assertNotNull("Configuration is null",confs);
		Assert.assertEquals("Config count doesn't match 2",2, confs.size());
		Assert.assertEquals("http://localhost:8028", confs.get(0).location);
		Assert.assertEquals("username2", confs.get(0).username);
		Assert.assertEquals("http://localhost:8028", confs.get(1).location);
		Assert.assertEquals("username", confs.get(1).username);
	}

	@TestExtension
	public static class TestConfigurationListener implements ConfigurationListener {

		private int count;
		private List<ServerConfiguration> newAndOld;

		@Override
		public void onChanged(ServerConfiguration conf, ServerConfiguration oldConf) {
			++count;
			newAndOld = new ArrayList<>();
			newAndOld.add(conf);
			newAndOld.add(oldConf);
		}

		public int getCount() {
			return count;
		}

		public List<ServerConfiguration> getConfigurationsChange() {
			return newAndOld;
		}
	}
}
