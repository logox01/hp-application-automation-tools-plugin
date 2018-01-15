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

package com.hpe.application.automation.tools.octane.model;

import com.hp.octane.integrations.dto.DTOFactory;
import com.hp.octane.integrations.dto.causes.CIEventCause;
import com.hp.octane.integrations.dto.causes.CIEventCauseType;
import com.hpe.application.automation.tools.octane.tests.build.BuildHandlerUtils;
import hudson.model.Cause;
import hudson.triggers.SCMTrigger;
import hudson.triggers.TimerTrigger;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: gullery
 * Date: 20/10/14
 * Time: 18:19
 * To change this template use File | Settings | File Templates.
 */

public final class CIEventCausesFactory {
	protected CIEventCausesFactory(){}
	private static final DTOFactory dtoFactory = DTOFactory.getInstance();

	public static List<CIEventCause> processCauses(List<Cause> causes) {
		List<CIEventCause> result = new LinkedList<>();
		CIEventCause tmpResultCause;
		Cause.UserIdCause tmpUserCause;
		Cause.UpstreamCause tmpUpstreamCause;

		if (causes != null) {
			for (Cause cause : causes) {
				tmpResultCause = dtoFactory.newDTO(CIEventCause.class);
				if (cause instanceof SCMTrigger.SCMTriggerCause) {
					tmpResultCause.setType(CIEventCauseType.SCM);
				} else if (cause instanceof TimerTrigger.TimerTriggerCause) {
					tmpResultCause.setType(CIEventCauseType.TIMER);
				} else if (cause instanceof Cause.UserIdCause) {
					tmpUserCause = (Cause.UserIdCause) cause;
					tmpResultCause.setType(CIEventCauseType.USER);
					tmpResultCause.setUser(tmpUserCause.getUserId());
				} else if (cause instanceof Cause.UpstreamCause) {
					tmpUpstreamCause = (Cause.UpstreamCause) cause;
					tmpResultCause.setType(CIEventCauseType.UPSTREAM);
					tmpResultCause.setProject(resolveJobCiId(tmpUpstreamCause.getUpstreamProject()));
					tmpResultCause.setBuildCiId(String.valueOf(tmpUpstreamCause.getUpstreamBuild()));
					tmpResultCause.setCauses(processCauses(tmpUpstreamCause.getUpstreamCauses()));
				} else {
					tmpResultCause.setType(CIEventCauseType.UNDEFINED);
				}
				result.add(tmpResultCause);
			}
		}
		return result;
	}

	private static String resolveJobCiId(String jobPlainName) {
		if(!jobPlainName.contains(",")) {
			return BuildHandlerUtils.translateFolderJobName(jobPlainName);
		}
		return jobPlainName;
	}
}
