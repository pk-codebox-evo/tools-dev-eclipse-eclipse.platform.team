/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - bug 75886
 *******************************************************************************/
package org.eclipse.team.internal.ui.dialogs;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.compare.internal.TabFolderLayout;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferencePageContainer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.team.internal.ui.*;
import org.eclipse.ui.PlatformUI;

public class PreferencePageContainerDialog extends Dialog implements IPreferencePageContainer {

	private PreferencePage[] pages;
	private PreferencePage currentPage;
	
	private Composite fTitleArea;
	private Label fTitleImage;
	private CLabel fMessageLabel;
	
	private String fMessage;
	private Color fNormalMsgAreaBackground;
	private Image fErrorMsgImage;

	private Button fOkButton;
	
	/**
	 * The Composite in which a page is shown.
	 */
	private Composite fPageContainer;

	/**
	 * The minimum page size; 200 by 200 by default.
	 *
	 * @see #setMinimumPageSize(Point)
	 */
	private Point fMinimumPageSize = new Point(200,200);
    private TabFolder tabFolder;
    private Map pageMap = new HashMap();
    
	/**
	 * Must declare our own images as the JFaceResource images will not be created unless
	 * a property/preference dialog has been shown
	 */
	protected static final String PREF_DLG_TITLE_IMG = "preference_page_container_image";//$NON-NLS-1$
	protected static final String PREF_DLG_IMG_TITLE_ERROR = "preference_page_container_title_error_image";//$NON-NLS-1$
    private String helpId;
	static {
		ImageRegistry reg = TeamUIPlugin.getPlugin().getImageRegistry();
		reg.put(PREF_DLG_TITLE_IMG, ImageDescriptor.createFromFile(PreferenceDialog.class, "images/pref_dialog_title.gif"));//$NON-NLS-1$
		reg.put(PREF_DLG_IMG_TITLE_ERROR, ImageDescriptor.createFromFile(Dialog.class, "images/message_error.gif"));//$NON-NLS-1$
	}
		
	public PreferencePageContainerDialog(Shell shell, PreferencePage[] pages) {
		super(shell);
		this.pages = pages;
	}
	
	/**
	 * @see Dialog#okPressed()
	 */
	protected void okPressed() {
		for (int i = 0; i < pages.length; i++) {
            PreferencePage page = pages[i];
			page.performOk();
        }
		
		handleSave();
		
		super.okPressed();
	}
	
	/**
	 * Sets the title for this dialog.
	 * @param title the title.
	 */
	public void setTitle(String title) {
		Shell shell= getShell();
		if ((shell != null) && !shell.isDisposed()) {
			shell.setText(title);
		}
	}
	
	/**
	 * @see Dialog#createDialogArea(Composite)
	 */
	protected Control createDialogArea(Composite parent) {		
		Composite composite = (Composite)super.createDialogArea(parent);
		((GridLayout) composite.getLayout()).numColumns = 1;
		
		createDescriptionArea(composite);
		
		if (isSinglePage()) {
		    createSinglePageArea(composite, pages[0]);
		} else {
		    createMultiplePageArea(composite);
		}
			
		// Build the separator line
		Label separator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		separator.setLayoutData(gd);
	
		setTitle(TeamUIMessages.PreferencePageContainerDialog_6); 
		applyDialogFont(parent);
        if (helpId != null) {
            PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, helpId);
        }
		return composite;
	}
	
    private void createMultiplePageArea(Composite composite) {
		// create a tab folder for the page
		tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setLayout(new TabFolderLayout());
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));		
		
		for (int i = 0; i < pages.length; i++) {
            PreferencePage page = pages[i];
			// text decoration options
			TabItem tabItem = new TabItem(tabFolder, SWT.NONE);
			tabItem.setText(page.getTitle());//		
			tabItem.setControl(createPageArea(tabFolder, page));
			pageMap.put(tabItem, page);
        }
		
		tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                updatePageSelection();
            }
        });
		updatePageSelection();
    }

    protected void updatePageSelection() {
        TabItem[] items = tabFolder.getSelection();
        if (items.length == 1) {
            currentPage = (PreferencePage)pageMap.get(items[0]);
            updateMessage();
        }
    }

    private boolean isSinglePage() {
        return pages.length == 1;
    }

    /*
     * Create the page contents for a single preferences page
     */
    private void createSinglePageArea(Composite composite, PreferencePage page) {
		createPageArea(composite, page);
		currentPage = page;
		updateMessage();
    }

    private Control createPageArea(Composite composite, PreferencePage page) {
        // Build the Page container
		fPageContainer = createPageContainer(composite);
		fPageContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		page.setContainer(this);
		page.createControl(fPageContainer);
		return fPageContainer;
    }

    private void createDescriptionArea(Composite composite) {
        // Build the title area and separator line
		Composite titleComposite = new Composite(composite, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		titleComposite.setLayout(layout);
		titleComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		createMessageArea(titleComposite);
	
		Label titleBarSeparator = new Label(titleComposite, SWT.HORIZONTAL | SWT.SEPARATOR);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		titleBarSeparator.setLayoutData(gd);
    }

    /**
	 * Creates the dialog's title area.
	 *
	 * @param parent the SWT parent for the title area composite
	 * @return the created title area composite
	 */
	private Composite createMessageArea(Composite parent) {
		
		// Create the title area which will contain
		// a title, message, and image.
		fTitleArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.numColumns = 2;
		
		// Get the colors for the title area
		Display display = parent.getDisplay();
		Color bg = JFaceColors.getBannerBackground(display);
		Color fg = JFaceColors.getBannerForeground(display);
		
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		fTitleArea.setLayout(layout);
		fTitleArea.setLayoutData(layoutData);
		fTitleArea.setBackground(bg);
	
		// Message label
		fMessageLabel = new CLabel(fTitleArea, SWT.LEFT);
		fMessageLabel.setBackground(bg);
		fMessageLabel.setForeground(fg);
		fMessageLabel.setText(" ");//$NON-NLS-1$
		fMessageLabel.setFont(JFaceResources.getBannerFont());
		
		final IPropertyChangeListener fontListener = new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if(JFaceResources.BANNER_FONT.equals(event.getProperty()) ||
					JFaceResources.DIALOG_FONT.equals(event.getProperty())) {
					updateMessage();
				}
			}
		};
		
		fMessageLabel.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				JFaceResources.getFontRegistry().removeListener(fontListener);
			}
		});
		
		JFaceResources.getFontRegistry().addListener(fontListener);
				
		GridData gd = new GridData(GridData.FILL_BOTH);
		fMessageLabel.setLayoutData(gd);
	
		// Title image
		fTitleImage = new Label(fTitleArea, SWT.LEFT);
		fTitleImage.setBackground(bg);
		fTitleImage.setImage(TeamUIPlugin.getPlugin().getImageRegistry().get(PREF_DLG_TITLE_IMG));
		gd = new GridData(); 
		gd.horizontalAlignment = GridData.END;
		fTitleImage.setLayoutData(gd);
		updateMessage();
		return fTitleArea;
	}

	/**
	 * Creates the inner page container.
	 */
	private Composite createPageContainer(Composite parent) {
		Composite result = new Composite(parent, SWT.NULL);
		FillLayout layout = new FillLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 5;
		result.setLayout(layout);
		return result;
	}

	/**
	 * Sets the minimum page size.
	 *
	 * @param size the page size encoded as
	 *   <code>new Point(width,height)</code>
	 * @see #setMinimumPageSize(int,int)
	 */
	public void setMinimumPageSize(Point size) {
		fMinimumPageSize.x = size.x;
		fMinimumPageSize.y = size.y;
	}
	
	/**
	 * Display the given error message. The currently displayed message
	 * is saved and will be redisplayed when the error message is set
	 * to <code>null</code>.
	 *
	 * @param errorMessage the errorMessage to display or <code>null</code>
	 */
	public void setErrorMessage(String errorMessage) {
		if (errorMessage == null) {
			if (fMessageLabel.getImage() != null) {
				// we were previously showing an error
				fMessageLabel.setBackground(fNormalMsgAreaBackground);
				fMessageLabel.setImage(null);
				fTitleImage.setImage(TeamUIPlugin.getPlugin().getImageRegistry().get(PREF_DLG_TITLE_IMG));
				fTitleArea.layout(true);
			}
	
			// show the message
			setMessage(fMessage);
	
		} else {
			fMessageLabel.setText(errorMessage);
			if (fMessageLabel.getImage() == null) {
				// we were not previously showing an error
							
				// lazy initialize the error background color and image
				if (fErrorMsgImage == null) {
					fErrorMsgImage = TeamUIPlugin.getPlugin().getImageRegistry().get(PREF_DLG_IMG_TITLE_ERROR);
				}
	
				// show the error	
				fNormalMsgAreaBackground = fMessageLabel.getBackground();
				fMessageLabel.setBackground(JFaceColors.getErrorBackground(fMessageLabel.getDisplay()));
				fMessageLabel.setImage(fErrorMsgImage);
				fTitleImage.setImage(null);
				fTitleArea.layout(true);
			}
		}
	}
	/**
	 * Set the message text. If the message line currently displays an error,
	 * the message is stored and will be shown after a call to clearErrorMessage
	 */
	public void setMessage(String newMessage) {
		fMessage = newMessage;
		if (fMessage == null) {
			fMessage = "";//$NON-NLS-1$
		}
		if (fMessageLabel.getImage() == null) {
			// we are not showing an error
			fMessageLabel.setText(fMessage);
		}
	}
	
	/**
	 * @see IPreferencePageContainer#updateMessage()
	 */
	public void updateMessage() {
	    if (currentPage != null) {
			String pageMessage = currentPage.getMessage();
			String pageErrorMessage = currentPage.getErrorMessage();
	
			// Adjust the font
			if (pageMessage == null && pageErrorMessage == null)
				fMessageLabel.setFont(JFaceResources.getBannerFont());
			else
				fMessageLabel.setFont(JFaceResources.getDialogFont());
	
			// Set the message and error message	
			if (pageMessage == null) {
			    if (isSinglePage()) {
			        setMessage(TeamUIMessages.PreferencePageContainerDialog_6); 
			    } else {
			    	//remove mnemonic see bug 75886
			    	String title = currentPage.getTitle();
			    	title = title.replaceAll("&", "");//$NON-NLS-1$ //$NON-NLS-2$
			    	setMessage(title);
			    }
			} else {
				setMessage(pageMessage);
			}
			setErrorMessage(pageErrorMessage);
	    }
	}
	
	/**
	 * @see IPreferencePageContainer#getPreferenceStore()
	 */
	public IPreferenceStore getPreferenceStore() {
		return null;
	}

	/**
	 * @see IPreferencePageContainer#updateButtons()
	 */
	public void updateButtons() {
		if (fOkButton != null) {
		    boolean isValid = true;
		    for (int i = 0; i < pages.length; i++) {
	            PreferencePage page = pages[i];
	            if (!page.isValid()) {
	                isValid = false;
	                break;
	            }
		    }
			fOkButton.setEnabled(isValid);
		}
	}

	/**
	 * @see IPreferencePageContainer#updateTitle()
	 */
	public void updateTitle() {
		updateMessage();
	}
	
	/**
	 * @see Dialog#createButtonsForButtonBar(Composite)
	 */
	protected void createButtonsForButtonBar(Composite parent) {
		fOkButton= createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	/**
	 * Save the values specified in the pages.
	 * <p>
	 * The default implementation of this framework method saves all
	 * pages of type <code>PreferencePage</code> (if their store needs saving
	 * and is a <code>PreferenceStore</code>).
	 * </p>
	 * <p>
	 * Subclasses may override.
	 * </p>
	 */
	protected void handleSave() {
		// Save now in case tbe workbench does not shutdown cleanly
	    for (int i = 0; i < pages.length; i++) {
            PreferencePage page = pages[i];
			IPreferenceStore store = page.getPreferenceStore();
			if (store != null
				&& store.needsSaving()
				&& store instanceof IPersistentPreferenceStore) {
				try {
					((IPersistentPreferenceStore) store).save();
				} catch (IOException e) {
					Utils.handle(e); 
				}
			}
        }
	}

    public void setHelpContextId(String helpId) {
        this.helpId = helpId;    
    }
}