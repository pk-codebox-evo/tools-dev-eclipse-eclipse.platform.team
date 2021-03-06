/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.core.client;

import org.eclipse.team.internal.ccvs.core.CVSTag;

public class RDiff extends RemoteCommand {

	/*** Local options: specific to rdiff ***/
	public static final LocalOption SUMMARY = new LocalOption("-s", null); //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.core.client.Request#getRequestId()
	 */
	protected String getRequestId() {
		return "rdiff"; //$NON-NLS-1$
	}

	/**
	 * Makes a -r or -D option for a tag.
	 * Valid for: checkout export history rdiff update
	 */
	public static LocalOption makeTagOption(CVSTag tag) {
		if (tag == null) tag = CVSTag.DEFAULT;
		int type = tag.getType();
		switch (type) {
			case CVSTag.BRANCH:
			case CVSTag.VERSION:
			case CVSTag.HEAD:
				return new LocalOption("-r", tag.getName()); //$NON-NLS-1$
			case CVSTag.DATE:
				return new LocalOption("-D", tag.getName()); //$NON-NLS-1$
			default:
				// Unknow tag type!!!
				throw new IllegalArgumentException();
		}
	}
}
