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

package com.hpe.application.automation.tools.octane.client;

import com.hp.mqm.client.MqmConnectionConfig;
import com.hp.mqm.client.MqmRestClient;
import com.hp.mqm.client.MqmRestClientImpl;
import com.hp.mqm.client.UsernamePasswordProxyCredentials;
import hudson.Extension;
import hudson.ProxyConfiguration;
import hudson.util.Secret;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Extension
public class JenkinsMqmRestClientFactoryImpl implements JenkinsMqmRestClientFactory {
	private static final Logger logger = LogManager.getLogger(JenkinsMqmRestClientFactoryImpl.class);
	private static final String CLIENT_TYPE = "HPE_CI_CLIENT";
	private MqmRestClient mqmRestClient;

	@Override
	public synchronized MqmRestClient obtain(String location, String sharedSpace, String username, Secret password) {
		if (mqmRestClient == null) {
			mqmRestClient = create(location, sharedSpace, username, password);
			logger.info("NGA REST Clint: initialized for " + location + " - " + sharedSpace + " - " + username);
		}
		return mqmRestClient;
	}

	@Override
	public MqmRestClient obtainTemp(String location, String sharedSpace, String username, Secret password) {
		MqmRestClient mqmRestClientTemp = create(location, sharedSpace, username, password);
		logger.info("NGA REST Clint: initialized for " + location + " - " + sharedSpace + " - " + username);
		return mqmRestClientTemp;
	}

	@Override
	public synchronized void updateMqmRestClient(String location, String sharedSpace, String username, Secret password) {
		mqmRestClient = create(location, sharedSpace, username, password);
		logger.info("NGA REST Clint: updated to " + location + " - " + sharedSpace + " - " + username);
	}

	private MqmRestClient create(String location, String sharedSpace, String username, Secret password) {
		MqmConnectionConfig clientConfig = new MqmConnectionConfig(location, sharedSpace, username, password.getPlainText(), CLIENT_TYPE);
		URL locationUrl;
		try {
			locationUrl = new URL(clientConfig.getLocation());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException(e);
		}
		if (isProxyNeeded(locationUrl.getHost())) {
			clientConfig.setProxyHost(getProxyHost());
			clientConfig.setProxyPort(getProxyPort());
			final String proxyUsername = getUsername();
			if (!StringUtils.isEmpty(proxyUsername)) {
				clientConfig.setProxyCredentials(new UsernamePasswordProxyCredentials(username, getPassword()));
			}
		}
		return new MqmRestClientImpl(clientConfig);
	}

	private String getProxyHost() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.name;
		}
		return null;
	}

	private Integer getProxyPort() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.port;
		}
		return null;
	}

	private String getUsername() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.getUserName();
		}
		return null;
	}

	private String getPassword() {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null) {
			return proxyConfiguration.getPassword();
		}
		return null;
	}

	private boolean isProxyNeeded(final String host) {
		final ProxyConfiguration proxyConfiguration = Jenkins.getInstance().proxy;
		if (proxyConfiguration != null && !StringUtils.isEmpty(proxyConfiguration.name)) {
			// if any patterns match the host, we will not use proxy
			final List<Pattern> patterns = proxyConfiguration.getNoProxyHostPatterns();
			if (patterns != null) {
				for (final Pattern pattern : patterns) {
					final Matcher matcher = pattern.matcher(host);
					if (matcher.matches()) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}
}
