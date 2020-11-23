package model;

import java.io.Serializable;
import java.util.LinkedList;

import utils.Cloneable;

public class BTree<K extends Comparable<K>> implements Serializable {
    private static final long serialVersionUID = 123456789;
    private Node<K> root = null;
    private int order, index, treeSize;
    private final int halfNumber;
    public final Node<K> nullNode = new Node<>();
    private LinkedList<BTree<K>> stepTrees = new LinkedList<>();

    /**
     * @param order of B-tree
     */
    public BTree(int order) {
        if (order < 3) {
            try {
                throw new Exception("B-tree's order can not lower than 3");
            } catch (Exception e) {
                e.printStackTrace();
            }
            order = 3;
        }
        this.order = order;
        halfNumber = (order - 1) / 2;
    }

    /**
     * @return true, if tree is empty
     */
    public boolean isEmpty() {
        return root == null;
    }

    /**
     * @return root node
     */
    public Node<K> getRoot() {
        return root;
    }

    public void setRoot(Node<K> root) {
        this.root = root;
    }

    /**
     * @return size of nodes in the tree
     */

    public int getTreeSize() {
        return treeSize;
    }

    public int getHalfNumber() {
        return halfNumber;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public LinkedList<BTree<K>> getStepTrees() {
        return stepTrees;
    }

    public void setStepTrees(LinkedList<BTree<K>> stepTrees) {
        this.stepTrees = stepTrees;
    }

    /**
     * @return height of tree
     */
    public int getHeight() {
        if (isEmpty()) {
            return 0;
        } else {
            return getHeight(root);
        }
    }

    /**
     * @param node , the node
     * @return the height of the node position
     */
    public int getHeight(Node<K> node) {
        int height = 0;
        Node<K> currentNode = node;
        while (!currentNode.equals(nullNode)) {
            currentNode = currentNode.getChild(0);
            height++;
        }
        return height;
    }

    /**
     * @param key , use key to find node
     * @return the node which contains of the key
     */
    public Node<K> getNode(K key) {
        if (isEmpty()) {
            return nullNode;
        }
        Node<K> currentNode = root;
        while (!currentNode.equals(nullNode)) {
            int i = 0;
            while (i < currentNode.getSize()) {
                if (currentNode.getKey(i).equals(key)) {
                    index = i;
                    return currentNode;
                } else if (currentNode.getKey(i).compareTo(key) > 0) {
                    currentNode = currentNode.getChild(i);
                    i = 0;
                } else {
                    i++;
                }
            }
            if (!currentNode.isNull()) {
                currentNode = currentNode.getChild(currentNode.getSize());
            }
        }
        return nullNode;
    }

    /**
     * @param key      , the key
     * @param fullNode , full node
     * @return half of the full node after inserting inside
     */
    private Node<K> getHalfKeys(K key, Node<K> fullNode) {
        int fullNodeSize = fullNode.getSize();


        for (int i = 0; i < fullNodeSize; i++) {
            if (fullNode.getKey(i).compareTo(key) > 0) {
                fullNode.addKey(i, key);
                break;
            }
        }
        if (fullNodeSize == fullNode.getSize())
            fullNode.addKey(fullNodeSize, key);


        stepTrees.add(Cloneable.clone(this));

        return getHalfKeys(fullNode);
    }

    /**
     * @param fullNode , full node
     * @return half of the full node
     */
    private Node<K> getHalfKeys(Node<K> fullNode) {
        Node<K> newNode = new Node<>(order);
        for (int i = 0; i < halfNumber; i++) {
            newNode.addKey(i, fullNode.getKey(0));
            fullNode.removeKey(0);
        }
        return newNode;
    }

    /**
     * @param halfNode , the rest of the full node
     * @return the left keys of full node
     */
    private Node<K> getRestOfHalfKeys(Node<K> halfNode) {
        Node<K> newNode = new Node<>(order);
        int halfNodeSize = halfNode.getSize();
        for (int i = 0; i < halfNodeSize; i++) {
            if (i != 0) {
                newNode.addKey(i - 1, halfNode.getKey(1));
                halfNode.removeKey(1);
            }
            newNode.addChild(i, halfNode.getChild(0));
            halfNode.removeChild(0);
        }
        return newNode;
    }

    /**
     * @param childNode , merge childNode with its fatherNode
     * @param index     , where to add node
     */
    private void mergeWithFatherNode(Node<K> childNode, int index) {
        childNode.getFather().addKey(index, childNode.getKey(0));
        childNode.getFather().removeChild(index);
        childNode.getFather().addChild(index, childNode.getChild(0));
        childNode.getFather().addChild(index + 1, childNode.getChild(1));
    }

    /**
     * @param childNode , merge childNode with its fatherNode
     */
    private void mergeWithFatherNode(Node<K> childNode) {
        int fatherNodeSize = childNode.getFather().getSize();
        for (int i = 0; i < fatherNodeSize; i++) {
            if (childNode.getFather().getKey(i).compareTo(childNode.getKey(0)) > 0) {
                mergeWithFatherNode(childNode, i);
                break;
            }
        }
        if (fatherNodeSize == childNode.getFather().getSize()) {
            mergeWithFatherNode(childNode, fatherNodeSize);
        }
        for (int i = 0; i <= childNode.getFather().getSize(); i++)
            childNode.getFather().getChild(i).setFather(childNode.getFather());
    }

    /**
     * @param node , set father for split node
     */
    private void setSplitFatherNode(Node<K> node) {
        for (int i = 0; i <= node.getSize(); i++)
            node.getChild(i).setFather(node);
    }

    /**
     * @param currentNode , process node if the keys size is overflow
     */
    private void processOverflow(Node<K> currentNode) {
        Node<K> newNode = getHalfKeys(currentNode);
        for (int i = 0; i <= newNode.getSize(); i++) {
            newNode.addChild(i, currentNode.getChild(0));
            currentNode.removeChild(0);
        }
        Node<K> originalNode = getRestOfHalfKeys(currentNode);
        currentNode.addChild(0, newNode);
        currentNode.addChild(1, originalNode);
        originalNode.setFather(currentNode);
        newNode.setFather(currentNode);
        setSplitFatherNode(originalNode);
        setSplitFatherNode(newNode);


        stepTrees.add(Cloneable.clone(this));
    }

    /**
     * @param key , the key to find a place to insert
     */
    public void insert(K key) {

        if (isEmpty()) {
            root = new Node<>(order);
            root.addKey(0, key);
            treeSize++;
            root.setFather(nullNode);
            root.addChild(0, nullNode);
            root.addChild(1, nullNode);


            stepTrees.add(Cloneable.clone(this));
            return;
        }

        Node<K> currentNode = root;


        while (!currentNode.isLastInternalNode()) {
            int i = 0;
            while (i < currentNode.getSize()) {

                if (currentNode.isLastInternalNode()) {
                    i = currentNode.getSize();
                } else if (currentNode.getKey(i).compareTo(key) > 0) {
                    currentNode = currentNode.getChild(i);
                    i = 0;
                } else {
                    i++;
                }
            }
            if (!currentNode.isLastInternalNode())
                currentNode = currentNode.getChild(currentNode.getSize());
        }

        if (!currentNode.isFull()) {
            int i = 0;
            while (i < currentNode.getSize()) {

                if (currentNode.getKey(i).compareTo(key) > 0) {
                    currentNode.addKey(i, key);
                    currentNode.addChild(currentNode.getSize(), nullNode);
                    treeSize++;


                    stepTrees.add(Cloneable.clone(this));
                    return;
                } else {
                    i++;
                }
            }
            currentNode.addKey(currentNode.getSize(), key);
            currentNode.addChild(currentNode.getSize(), nullNode);
            treeSize++;


            stepTrees.add(Cloneable.clone(this));
        } else {
            Node<K> newChildNode = getHalfKeys(key, currentNode);
            for (int i = 0; i < halfNumber; i++) {
                newChildNode.addChild(i, currentNode.getChild(0));
                currentNode.removeChild(0);
            }
            newChildNode.addChild(halfNumber, nullNode);

            Node<K> originalFatherNode = getRestOfHalfKeys(currentNode);
            currentNode.addChild(0, newChildNode);
            currentNode.addChild(1, originalFatherNode);
            originalFatherNode.setFather(currentNode);
            newChildNode.setFather(currentNode);
            treeSize++;

            stepTrees.add(Cloneable.clone(this));

            if (!currentNode.getFather().equals(nullNode)) {
                while (!currentNode.getFather().isOverflow() && !currentNode.getFather().equals(nullNode)) {
                    boolean flag = currentNode.getSize() == 1 && !currentNode.getFather().isOverflow();
                    if (currentNode.isOverflow() || flag) {
                        mergeWithFatherNode(currentNode);
                        currentNode = currentNode.getFather();

                        stepTrees.add(Cloneable.clone(this));

                        if (currentNode.isOverflow()) {
                            processOverflow(currentNode);
                        }
                    } else {
                        break;
                    }
                }
            }
        }
    }

    /**
     * @param node , the node
     * @return the number of the node's father child index which matches the node
     */
    private int findChild(Node<K> node) {
        if (!node.equals(root)) {
            Node<K> fatherNode = node.getFather();

            for (int i = 0; i <= fatherNode.getSize(); i++) {
                if (fatherNode.getChild(i).equals(node))
                    return i;
            }
        }
        return -1;
    }

    /**
     * @param node , the node's father have different height of right and left
     *             subtree balance the unbalanced tree
     */
    private Node<K> balanceDeletedNode(Node<K> node) {
        boolean flag;
        int nodeIndex = findChild(node);
        K pair;
        Node<K> fatherNode = node.getFather();
        Node<K> currentNode;
        if (nodeIndex == 0) {
            currentNode = fatherNode.getChild(1);

            flag = true;
        } else {
            currentNode = fatherNode.getChild(nodeIndex - 1);
            flag = false;
        }

        int currentSize = currentNode.getSize();
        if (currentSize > halfNumber) {
            if (flag) {
                pair = fatherNode.getKey(0);
                node.addKey(node.getSize(), pair);
                fatherNode.removeKey(0);
                pair = currentNode.getKey(0);
                currentNode.removeKey(0);
                node.addChild(node.getSize(), currentNode.getChild(0));
                currentNode.removeChild(0);
                fatherNode.addKey(0, pair);
            } else {
                pair = fatherNode.getKey(nodeIndex - 1);
                node.addKey(0, pair);
                fatherNode.removeKey(nodeIndex - 1);
                pair = currentNode.getKey(currentSize - 1);
                currentNode.removeKey(currentSize - 1);
                node.addChild(0, currentNode.getChild(currentSize));
                currentNode.removeChild(currentSize);
                fatherNode.addKey(nodeIndex - 1, pair);
            }

            if (node.isLastInternalNode()) {
                node.removeChild(0);
            }
            stepTrees.add(Cloneable.clone(this));
            return node;
        } else {
            if (flag) {
                currentNode.addKey(0, fatherNode.getKey(0));
                fatherNode.removeKey(0);
                fatherNode.removeChild(0);
                if (root.getSize() == 0) {
                    root = currentNode;
                    currentNode.setFather(nullNode);
                }
                if (node.getSize() == 0) {
                    currentNode.addChild(0, node.getChild(0));
                    currentNode.getChild(0).setFather(currentNode);
                }
                for (int i = 0; i < node.getSize(); i++) {
                    currentNode.addKey(i, node.getKey(i));
                    currentNode.addChild(i, node.getChild(i));
                    currentNode.getChild(i).setFather(currentNode);
                }
            } else {
                currentNode.addKey(currentNode.getSize(), fatherNode.getKey(nodeIndex - 1));
                fatherNode.removeKey(nodeIndex - 1);
                fatherNode.removeChild(nodeIndex);
                if (root.getSize() == 0) {
                    root = currentNode;
                    currentNode.setFather(nullNode);
                }
                int currentNodeSize = currentNode.getSize();
                if (node.getSize() == 0) {
                    currentNode.addChild(currentNodeSize, node.getChild(0));
                    currentNode.getChild(currentNodeSize).setFather(currentNode);
                }
                for (int i = 0; i < node.getSize(); i++) {
                    currentNode.addKey(currentNodeSize + i, node.getKey(i));
                    currentNode.addChild(currentNodeSize + i, node.getChild(i));
                    currentNode.getChild(currentNodeSize + i).setFather(currentNode);
                }
            }
            stepTrees.add(Cloneable.clone(this));
            return fatherNode;
        }
    }

    /**
     * @param node , use the last internal node to replace the node
     * @return the last internal node
     */
    private Node<K> replaceNode(Node<K> node) {
        Node<K> currentNode = node.getChild(index + 1);
        while (!currentNode.isLastInternalNode()) {
            currentNode = currentNode.getChild(0);
        }

        if (currentNode.getSize() - 1 < halfNumber) {
            currentNode = node.getChild(index);
            int currentNodeSize = currentNode.getSize();
            while (!currentNode.isLastInternalNode()) {
                currentNode = currentNode.getChild(currentNodeSize);
            }
            node.addKey(index, currentNode.getKey(currentNodeSize - 1));
            currentNode.removeKey(currentNodeSize - 1);
            currentNode.addKey(currentNodeSize - 1, node.getKey(index + 1));
            node.removeKey(index + 1);
            index = currentNode.getSize() - 1;
            stepTrees.add(Cloneable.clone(this));
        } else {
            node.addKey(index + 1, currentNode.getKey(0));
            currentNode.removeKey(0);
            currentNode.addKey(0, node.getKey(index));
            node.removeKey(index);
            index = 0;

            stepTrees.add(Cloneable.clone(this));
        }
        return currentNode;
    }

    /**
     * @param key , the key to be deleted
     */

    /*
     * Case 1: If k is in the node x which is a leaf and x.size -1 >= halfNumber
     * Case 2: If k is in the node x which is a leaf and x.size -1 < halfNumber Case
     * 3: If k is in the node x and x is an internal node (not a leaf)
     */
    public void delete(K key) {

        stepTrees.add(Cloneable.clone(this));
        Node<K> node = getNode(key);
        Node<K> deleteNode = null;
        if (node.equals(nullNode))
            return;

        if (node.equals(root) && node.getSize() == 1 && node.isLastInternalNode()) {
            root = null;
            treeSize--;
            stepTrees.add(Cloneable.clone(this));
        } else {
            boolean flag = true;
            boolean isReplaced = false;

            if (!node.isLastInternalNode()) {
                node = replaceNode(node);
                deleteNode = node;
                isReplaced = true;
            }

            if (node.getSize() - 1 < halfNumber) {
                node = balanceDeletedNode(node);
                if (isReplaced) {
                    for (int i = 0; i <= node.getSize(); i++) {
                        for (int j = 0; i < node.getChild(i).getSize(); j++) {
                            if (node.getChild(i).getKey(j).equals(key)) {
                                deleteNode = node.getChild(i);
                                break;
                            }
                        }
                    }
                }
            } else if (node.isLastInternalNode()) {
                node.removeChild(0);
            }

            while (!node.getChild(0).equals(root) && node.getSize() < halfNumber && flag) {
                if (node.equals(root)) {
                    for (int i = 0; i <= root.getSize(); i++) {
                        if (root.getChild(i).getSize() == 0) {
                            flag = true;
                            break;
                        } else {
                            flag = false;
                        }
                    }
                }
                if (flag) {
                    node = balanceDeletedNode(node);
                }
            }

            if (deleteNode == null) {
                node = getNode(key);
            } else {
                node = deleteNode;
            }

            if (!node.equals(nullNode)) {
                for (int i = 0; i < node.getSize(); i++) {
                    if (node.getKey(i) == key) {
                        node.removeKey(i);
                    }
                }
                treeSize--;
                stepTrees.add(Cloneable.clone(this));
            }
        }
    }
}
