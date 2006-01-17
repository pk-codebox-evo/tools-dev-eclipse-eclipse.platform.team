/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.team.internal.ccvs.ui;

import org.eclipse.team.ui.history.IFileHistoryProviderParticipant;
import org.eclipse.ui.part.Page;

public class CVSFileHistoryProviderParticipant implements IFileHistoryProviderParticipant {

	public Page createPage() {
		CVSHistoryPage page = new CVSHistoryPage();
		return page;
	}

}