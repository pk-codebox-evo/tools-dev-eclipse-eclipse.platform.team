/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ccvs.ui.mappings;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.mapping.ResourceMapping;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.team.core.diff.*;
import org.eclipse.team.core.mapping.IMergeContext;
import org.eclipse.team.core.mapping.ISynchronizationContext;
import org.eclipse.team.internal.ccvs.ui.CVSUIMessages;
import org.eclipse.team.internal.ccvs.ui.CVSUIPlugin;
import org.eclipse.ui.IWorkbenchPart;

public class ModelReplaceOperation extends ModelUpdateOperation {
	
	boolean hasPrompted = false;

	public ModelReplaceOperation(IWorkbenchPart part, ResourceMapping[] selectedMappings, boolean consultModels) {
		super(part, selectedMappings, consultModels);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.TeamOperation#getJobName()
	 */
	protected String getJobName() {
		return CVSUIMessages.ReplaceOperation_taskName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#isAttemptHeadlessMerge()
	 */
	protected boolean isAttemptHeadlessMerge() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#hasChangesOfInterest()
	 */
	protected boolean hasChangesOfInterest() {
		IMergeContext context = (IMergeContext)getContext();
		return !context.getDiffTree().isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.internal.ccvs.ui.mappings.ModelUpdateOperation#getMergeType()
	 */
	protected int getMergeType() {
		return ISynchronizationContext.TWO_WAY;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.team.ui.operations.ResourceMappingMergeOperation#performMerge(org.eclipse.core.runtime.IProgressMonitor)
	 */
	protected IStatus performMerge(IProgressMonitor monitor) throws CoreException {
		if (!hasLocalChanges() || promptForOverwrite()) {
			return super.performMerge(monitor);
		}
		return new Status(IStatus.ERROR, CVSUIPlugin.ID, REQUEST_PREVIEW, "", null); //$NON-NLS-1$
	}

	/*
	 * Mde porotected to be overriden by test cases.
	 */
	protected boolean promptForOverwrite() {
		if (hasPrompted)
			return true;
		final int[] result = new int[] { 1 };
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
		        MessageDialog dialog = new MessageDialog(getShell(), CVSUIMessages.ModelReplaceOperation_0, null, // accept
		                // the
		                // default
		                // window
		                // icon
		        		CVSUIMessages.ModelReplaceOperation_1, 
		        		MessageDialog.QUESTION, new String[] { CVSUIMessages.ModelReplaceOperation_2, CVSUIMessages.ModelReplaceOperation_3,
		                        IDialogConstants.CANCEL_LABEL }, result[0]); // preview is the default
		        
		        result[0] = dialog.open();

			};
		});
        if (result[0] == 2)
        	throw new OperationCanceledException();
        hasPrompted = true;
        return result[0] == 0;
	}

	private boolean hasLocalChanges() {
		return getContext().getDiffTree().hasMatchingDiffs(ResourcesPlugin.getWorkspace().getRoot().getFullPath(), new FastDiffFilter() {
			public boolean select(IDiff node) {
				if (node instanceof IThreeWayDiff) {
					IThreeWayDiff twd = (IThreeWayDiff) node;
					int direction = twd.getDirection();
					if (direction == IThreeWayDiff.OUTGOING || direction == IThreeWayDiff.CONFLICTING) {
						return true;
					}
				} else {
					// Return true for any two-way change
					return true;
				}
				return false;
			}
		});
	}
}
