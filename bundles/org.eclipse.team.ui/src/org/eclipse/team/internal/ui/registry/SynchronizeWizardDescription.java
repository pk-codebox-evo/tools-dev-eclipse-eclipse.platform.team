/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.registry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.team.internal.ui.TeamUIPlugin;
import org.eclipse.ui.internal.WorkbenchPlugin;

/**
 * Descriptor for accessing and creating synchronize wizards
 */
public class SynchronizeWizardDescription {
	
	public  static final String ATT_ID = "id"; //$NON-NLS-1$
	public  static final String ATT_NAME = "name"; //$NON-NLS-1$
	public  static final String ATT_ICON = "icon"; //$NON-NLS-1$
	public  static final String ATT_CLASS = "class"; //$NON-NLS-1$
	
	private String label;
	private String className;
	private String id;
	private ImageDescriptor imageDescriptor;
	
	private IConfigurationElement configElement;
	
	public SynchronizeWizardDescription(IConfigurationElement e, String descText) throws CoreException {
		configElement = e;
		loadFromExtension();
	}
	
	public IWizard createWizard() throws CoreException {
		Object obj = WorkbenchPlugin.createExtension(configElement, ATT_CLASS);
		return (IWizard) obj;
	}
	
	private void loadFromExtension() throws CoreException {
		String identifier = configElement.getAttribute(ATT_ID);
		label = configElement.getAttribute(ATT_NAME);
		className = configElement.getAttribute(ATT_CLASS);
		
		// Sanity check.
		if ((label == null) || (className == null) || (identifier == null)) {
			throw new CoreException(new Status(IStatus.ERROR, configElement.getDeclaringExtension().getDeclaringPluginDescriptor().getUniqueIdentifier(), 0, "Invalid extension (missing label or class name): " + identifier, //$NON-NLS-1$
					null));
		}
		
		id = identifier;
	}
	
	public String getId() {
		return id;
	}
	
	public ImageDescriptor getImageDescriptor() {
		if (imageDescriptor != null)
			return imageDescriptor;
		String iconName = configElement.getAttribute(ATT_ICON);
		if (iconName == null)
			return null;
		imageDescriptor = TeamUIPlugin.getImageDescriptorFromExtension(configElement.getDeclaringExtension(), iconName);
		return imageDescriptor;
	}

	public String getName() {
		return label;
	}
	
	public String toString() {
		return "Synchronize Participant(" + getId() + ")"; //$NON-NLS-2$//$NON-NLS-1$
	}
}
