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
package org.eclipse.team.internal.ccvs.ui.repo;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.*;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.team.internal.ccvs.core.*;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.team.internal.ccvs.ui.actions.CVSAction;

/**
 * Action to add a root remote folder to a branch
 */
public class AddToBranchAction extends CVSAction {

	IInputValidator validator = new IInputValidator() {
		public String isValid(String newText) {
			IStatus status = CVSTag.validateTagName(newText);
			if (status.isOK()) return null;
			return status.getMessage();
		}
	};
	
	/**
	 * @see org.eclipse.team.internal.ccvs.ui.actions.CVSAction#execute(org.eclipse.jface.action.IAction)
	 */
	protected void execute(IAction action) throws InvocationTargetException, InterruptedException {
		run(new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				final ICVSRemoteFolder folder = getSelectedRootFolder();
				if (folder == null) return;
				Shell shell = getShell();
				final CVSException[] exception = new CVSException[] { null };
				shell.getDisplay().syncExec(new Runnable() {
					public void run() {
						InputDialog dialog = new InputDialog(getShell(), CVSUIMessages.AddToBranchAction_enterTag, CVSUIMessages.AddToBranchAction_enterTagLong, null, validator); // 
						if (dialog.open() == Window.OK) {
							CVSTag tag = new CVSTag(dialog.getValue(), CVSTag.BRANCH);
							try {
								CVSUIPlugin.getPlugin().getRepositoryManager().addTags(folder, new CVSTag[] {tag});
							} catch (CVSException e) {
								exception[0] = e;
							}
						}
					}
				});
				if (exception[0] != null)
					throw new InvocationTargetException(exception[0]);
			}
		}, false, PROGRESS_BUSYCURSOR); 
	}

	/**
	 * @see org.eclipse.team.internal.ui.actions.TeamAction#isEnabled()
	 */
	public boolean isEnabled() {
		return getSelectedRootFolder() != null;
	}

	protected ICVSRemoteFolder getSelectedRootFolder() {
		ICVSRemoteFolder[] folders = getSelectedRemoteFolders();
		ICVSRemoteFolder selectedFolder = null;
		for (int i = 0; i < folders.length; i++) {
			ICVSRemoteFolder folder = folders[i];
			if (folder.isDefinedModule() || new Path(null, folder.getRepositoryRelativePath()).segmentCount()==1) {
				// only return a folder if one valid one is selected.
				if (selectedFolder != null) return null;
				selectedFolder = folder;
			}
		}
		return selectedFolder;
	}
}
