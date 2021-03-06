/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.tests.ccvs.ui;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.team.core.TeamException;
import org.eclipse.team.core.synchronize.SyncInfoSet;
import org.eclipse.team.internal.ccvs.core.mapping.CVSActiveChangeSet;
import org.eclipse.team.internal.ui.synchronize.ChangeSetCapability;
import org.eclipse.team.internal.ui.synchronize.ChangeSetDiffNode;
import org.eclipse.team.internal.ui.synchronize.ChangeSetModelProvider;
import org.eclipse.team.internal.ui.synchronize.ChangeSetModelSorter;
import org.eclipse.team.internal.ui.synchronize.SynchronizeModelElementSorter;
import org.eclipse.team.internal.ui.synchronize.SynchronizePageConfiguration;
import org.eclipse.team.internal.ui.synchronize.TreeViewerAdvisor;
import org.eclipse.team.internal.ui.synchronize.UnchangedResourceModelElement;
import org.eclipse.team.tests.ccvs.core.EclipseTest;
import org.eclipse.team.ui.synchronize.ISynchronizePage;
import org.eclipse.team.ui.synchronize.ISynchronizePageConfiguration;
import org.eclipse.team.ui.synchronize.ISynchronizePageSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class PatchTreeTest extends EclipseTest {

	public void testChangeSetModelSorter() throws CoreException {
		MyTreeViewer tree = new MyTreeViewer();
		tree.init();
		ViewerSorter sorter = tree.getSorter();

		UnchangedResourceModelElement elementZ = new UnchangedResourceModelElement(
				null, getUniqueTestProject("z"));
		ChangeSetDiffNode nodeA = new ChangeSetDiffNode(null,
				new CVSActiveChangeSet(null, "a"));
		ChangeSetDiffNode nodeB = new ChangeSetDiffNode(null,
				new CVSActiveChangeSet(null, "b"));
		ChangeSetDiffNode nodeC = new ChangeSetDiffNode(null,
				new CVSActiveChangeSet(null, "c"));
		Object[] elements = { nodeB, nodeC, elementZ, nodeA };

		sorter.sort(tree, elements);

		assertEquals(nodeA, elements[0]);
		assertEquals(nodeB, elements[1]);
		assertEquals(nodeC, elements[2]);
		assertEquals(elementZ, elements[3]);
	}

	public void testDuplicatedElementsInPatchTree() throws TeamException,
			CoreException {
		MyTreeViewer tree = new MyTreeViewer();
		tree.init();

		UnchangedResourceModelElement element = new UnchangedResourceModelElement(
				null, getUniqueTestProject("z"));
		ChangeSetDiffNode nodeA = new ChangeSetDiffNode(null,
				new CVSActiveChangeSet(null, "a"));
		ChangeSetDiffNode nodeB = new ChangeSetDiffNode(null,
				new CVSActiveChangeSet(null, "b"));
		ChangeSetDiffNode nodeC = new ChangeSetDiffNode(null,
				new CVSActiveChangeSet(null, "c"));
		ChangeSetDiffNode nodeD = new ChangeSetDiffNode(null,
				new CVSActiveChangeSet(null, "d"));

		Item[] treeChildren = tree.testGetChildren(tree.getTree());
		assertEquals(0, treeChildren.length);

		Object children[] = { nodeC, nodeB, element, nodeA };
		tree.testInternalAdd(children);
		treeChildren = tree.testGetChildren(tree.getTree());
		assertEquals(children.length, treeChildren.length);

		Object childrenToAdd[] = { nodeD, element, nodeA, nodeB };
		tree.testInternalAdd(childrenToAdd);
		treeChildren = tree.testGetChildren(tree.getTree());

		Object[] expected = { nodeC, nodeB, element, nodeA, nodeD };
		assertEquals(expected.length, treeChildren.length);

		for (int i = 0; i < treeChildren.length; i++) {
			assertEquals(1, countByData(treeChildren, treeChildren[i]));
		}
	}

	private int countByData(final Item[] a, Item o) {
		int c = 0;
		for (int i = 0; i < a.length; i++) {
			if (a[i].getData() == o.getData())
				c++;
		}
		return c;
	}

	private SynchronizePageConfiguration getMyConfiguration(final Viewer viewer) {
		SynchronizePageConfiguration conf = new SynchronizePageConfiguration(
				null);
		conf.setPage(new ISynchronizePage() {

			public void init(ISynchronizePageSite site)
					throws PartInitException {
			}

			public Viewer getViewer() {
				return viewer;
			}

			public boolean aboutToChangeProperty(
					ISynchronizePageConfiguration configuration, String key,
					Object newValue) {
				return false;
			}
		});
		return conf;
	}

	private class MyModelProvider extends ChangeSetModelProvider implements
			IPropertyChangeListener {

		public ChangeSetCapability getChangeSetCapability() {
			return new ChangeSetCapability() {
			};
		}

		public MyModelProvider(final Viewer viewer) {
			super(getMyConfiguration(viewer), new SyncInfoSet(),
					"sampleProviderId");
			addPropertyChangeListener(this);
		}

		public void propertyChange(PropertyChangeEvent event) {
			// nothing to do
		}
	}

	private class MyTreeViewer extends
			TreeViewerAdvisor.NavigableCheckboxTreeViewer {

		public MyTreeViewer() {
			super(new Composite(PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow().getShell(), SWT.NONE),
					SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		}

		public void testInternalAdd(Object[] childElements) {
			internalAdd(this.getTree(), null, childElements);
		}

		public Item[] testGetChildren(Widget widget) {
			return this.getChildren(widget);
		}

		public void init() {
			ChangeSetModelProvider provider = new MyModelProvider(this);
			provider.setViewerSorter(new SynchronizeModelElementSorter());
			ChangeSetModelSorter sorter = new ChangeSetModelSorter(provider, 0);
			setSorter(sorter);
		}
	}
	
	public static Test suite() {
		return suite(PatchTreeTest.class);
	}

}
