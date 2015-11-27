package cv.lecturesight.manpages;

import org.pmw.tinylog.Logger;
import java.util.LinkedList;
import java.util.List;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.osgi.framework.Bundle;

/**
 * Data model for the ManualPages UI that also implements the TreeModel for the
 * swing tree view in the UI form.
 *
 * @author wulff
 */
public class ManPagesModel implements TreeModel {
  
  enum EventType {

    ADDED, REMOVED, CHANGED;
  }

  // tree has only two levels: bundle -> page
  // so we can use a list of lists to describe the tree
  List<BundleNode> nodes = new LinkedList<BundleNode>();

  // our listeners
  List<TreeModelListener> listeners = new LinkedList<TreeModelListener>();

  RootElement rootElement = new RootElement();

  /**
   * Returns the BundleNode containing bundle or null if such a node can not be
   * found.
   *
   * @param bundle
   * @return
   */
  public BundleNode getBundleNode(Bundle bundle) {
    for (BundleNode bnode : nodes) {
      if (bundle.getSymbolicName().equals(bnode.bundle.getSymbolicName())) {
        return bnode;
      }
    }
    return null;
  }

  /**
   * Add a bundle to the Model. If the Bundle is already contained in the model
   * its list of pages will be reseted.
   *
   * @param bundle
   */
  public BundleNode addBundleNode(Bundle bundle) {
    removeBundleNode(bundle);
    BundleNode newNode = new BundleNode(bundle);
    nodes.add(newNode);
    int index = nodes.indexOf(newNode);

    // send notifications
    Object[] path = {rootElement};
    int[] childIndices = {index};
    Object[] changedChildren = {newNode};
    TreeModelEvent event = new TreeModelEvent(this, path, childIndices, changedChildren);
    notifyListeners(event, EventType.ADDED);

    Logger.debug("Added BundleNode " + newNode);
    
    return newNode;
  }

  /**
   * Removes the BundleNode containing bundle from the model.
   *
   * @param bundle
   */
  public void removeBundleNode(Bundle bundle) {
    BundleNode bn = getBundleNode(bundle);
    int index = nodes.indexOf(bn);
    if (bn != null) {
      nodes.remove(bn);

      // send notifications
      Object[] path = {rootElement};
      int[] childIndices = {index};
      Object[] changedChildren = {bn};
      TreeModelEvent event = new TreeModelEvent(this, path, childIndices, changedChildren);
      notifyListeners(event, EventType.REMOVED);
      
      Logger.debug("Removed BundleNode " + bn);
    }
  }

  /**
   * Adds pages to the BundleNode containing bundle.
   *
   * @param bundle
   * @param pages
   */
  public void addPages(Bundle bundle, List<ManPage> pages) {
    BundleNode bnode = getBundleNode(bundle);
    if (bnode == null) {
      bnode = addBundleNode(bundle);
    }

    int[] indices = new int[pages.size()];
    int i = 0;
    for (ManPage p : pages) {
      bnode.pages.add(p);
      indices[i++] = bnode.pages.indexOf(p);
    }

    // send notifications
    Object[] path = {rootElement, bnode};
    Object[] changedChildren = pages.toArray();
    TreeModelEvent event = new TreeModelEvent(this, path, indices, changedChildren);
    notifyListeners(event, EventType.ADDED);
    
    Logger.debug("Added " + i + " pages to BundleNode " + bnode);
  }

  @Override
  public Object getRoot() {
    return rootElement;
  }

  @Override
  public Object getChild(Object parent, int index) {
    if (parent instanceof RootElement) {
      return nodes.get(index);
    } else {  // (parent instanceof BundleNode)
      return ((BundleNode) parent).pages.get(index);
    }
  }

  @Override
  public int getChildCount(Object parent) {
    if (parent instanceof RootElement) {
      return nodes.size();
    } else {  // (parent instanceof BundleNode)
      return ((BundleNode) parent).pages.size();
    }
  }

  @Override
  public boolean isLeaf(Object node) {
    return node instanceof ManPage;
  }

  @Override
  public void valueForPathChanged(TreePath path, Object newValue) {
    // No need to implement, JTree will not be edited by user.
  }

  @Override
  public int getIndexOfChild(Object parent, Object child) {
    if (parent instanceof RootElement) {
      return nodes.indexOf(child);
    } else {  // (parent instanceof BundleNode)
      return ((BundleNode) parent).pages.indexOf(child);
    }
  }

  @Override
  public void addTreeModelListener(TreeModelListener l) {
    listeners.add(l);
  }

  @Override
  public void removeTreeModelListener(TreeModelListener l) {
    listeners.remove(l);
  }

  void notifyListeners(TreeModelEvent e, EventType t) {
    for (TreeModelListener l : listeners) {
      switch (t) {
        case ADDED:
          l.treeNodesInserted(e);
          break;
        case REMOVED:
          l.treeNodesRemoved(e);
          break;
        case CHANGED:
          l.treeNodesChanged(e);
          break;
      }
    }
  }

  class RootElement {

    public String toString() {
      return "LectureSight Manual";
    }
  }
}
