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
package org.eclipse.team.internal.ccvs.ui.tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.contentassist.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.contentassist.*;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.*;
import org.eclipse.team.internal.ccvs.core.CVSTag;
import org.eclipse.ui.contentassist.ContentAssistHandler;

/**
 * A content assist processor for tags for use with Text widgets.
 */
public class TagContentAssistProcessor implements ISubjectControlContentAssistProcessor {

    private FilteredTagList tags;
    private Map images = new HashMap();

    public static void createContentAssistant(Text text, TagSource tagSource, int includeFlags) {
		final TagContentAssistProcessor tagContentAssistProcessor = new TagContentAssistProcessor(tagSource, includeFlags);
		text.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                tagContentAssistProcessor.dispose();
            }
        });
        ContentAssistHandler.createHandlerForText(text, createSubjectContentAssistant(tagContentAssistProcessor));
	}

    private static SubjectControlContentAssistant createSubjectContentAssistant(IContentAssistProcessor processor) {
		final SubjectControlContentAssistant contentAssistant= new SubjectControlContentAssistant();
		
		contentAssistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);
		
		//ContentAssistPreference.configure(contentAssistant, JavaPlugin.getDefault().getPreferenceStore());
		
		contentAssistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		contentAssistant.setInformationControlCreator(new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent);
			}
		});
		
		return contentAssistant;
	}
	
    public TagContentAssistProcessor(TagSource tagSource, int includeFlags) {
        tags = new FilteredTagList(tagSource, TagSource.convertIncludeFlaqsToTagTypes(includeFlags));
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.contentassist.IContentAssistSubjectControl, int)
     */
    public ICompletionProposal[] computeCompletionProposals(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
        Control c = contentAssistSubjectControl.getControl();
        int docLength = contentAssistSubjectControl.getDocument().getLength();
        if (c instanceof Text) {
            Text t = (Text)c;
            String filter = t.getText();
            tags.setPattern(filter);
            CVSTag[] matching = tags.getMatchingTags();
            if (matching.length > 0) {
                List proposals = new ArrayList();
                for (int i = 0; i < matching.length; i++) {
                    CVSTag tag = matching[i];
                    String name = tag.getName();
                    ImageDescriptor desc = TagElement.getImageDescriptor(tag);
                    Image image = null;
                    if (desc != null) {
                        image = (Image)images.get(desc);
                        if (image == null) {
                            image = desc.createImage();
                            images.put(desc, image);
                        }
                    }
                    CompletionProposal proposal = new CompletionProposal(name, 0, docLength, name.length(), image, name, null, null);
                    proposals.add(proposal);
                }
                return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
            }
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.contentassist.ISubjectControlContentAssistProcessor#computeContextInformation(org.eclipse.jface.contentassist.IContentAssistSubjectControl, int)
     */
    public IContextInformation[] computeContextInformation(IContentAssistSubjectControl contentAssistSubjectControl, int documentOffset) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
     */
    public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
        Assert.isTrue(false, "ITextViewer not supported"); //$NON-NLS-1$
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
     */
    public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
     */
    public char[] getCompletionProposalAutoActivationCharacters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
     */
    public char[] getContextInformationAutoActivationCharacters() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
     */
    public String getErrorMessage() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
     */
    public IContextInformationValidator getContextInformationValidator() {
        return null;
    }
    
	/**
     * Dispose of any images created by the assistant
     */
    public void dispose() {
        for (Iterator iter = images.values().iterator(); iter.hasNext();) {
            Image image = (Image) iter.next();
            image.dispose();
        }
    }
    
}
