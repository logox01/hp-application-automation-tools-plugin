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

package com.hpe.application.automation.tools.octane.tests.impl;

import hudson.FilePath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class ObjectStreamIterator<E> implements Iterator<E> {
	private static Logger logger = LogManager.getLogger(ObjectStreamIterator.class);

	private MyObjectInputStream ois;
	private FilePath filePath;
	private E next;

	public ObjectStreamIterator(FilePath filePath, boolean deleteOnClose) throws IOException, InterruptedException {
		this.filePath = filePath;
		ois = new MyObjectInputStream(new BufferedInputStream(filePath.read()), deleteOnClose);
	}

	@Override
	public boolean hasNext() {
		if (next != null) {
			return true;
		}
		try {
			next = (E) ois.readObject();
			return true;
		} catch (Exception e) {
			ois.close();
			return false;
		}
	}

	@Override
	public E next() {
		if (hasNext()) {
			E value = next;
			next = null;
			return value;
		} else {
			throw new NoSuchElementException();
		}
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	private class MyObjectInputStream extends ObjectInputStream {

		private boolean deleteOnClose;

		public MyObjectInputStream(InputStream in, boolean deleteOnClose) throws IOException {
			super(in);
			this.deleteOnClose = deleteOnClose;
		}

		@Override
		public void close() {
			try {
				super.close();
			} catch (IOException ioe) {
				logger.error("Failed to close the stream", ioe); // NON-NLS
			}
			if (deleteOnClose) {
				try {
					filePath.delete();
				} catch (Exception e) {
					logger.error("Failed to perform clean up", e); // NON-NLS
				}
			}
		}
	}
}
