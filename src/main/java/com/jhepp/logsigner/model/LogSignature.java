package com.jhepp.logsigner.model;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.logging.log4j.util.Strings;

import java.util.LinkedList;
import java.util.List;

/*
This is the representation of a hash tree, containing all the nodes and necessary operations for constructing and searching
 */
public class LogSignature {
    private SignatureNode root;

    public SignatureNode getRoot() {
        return root;
    }

    /*
    The tree is built recursively, since the higher levels need to be constructed first.
    Every recursive call handles one level of the tree, from bottom to the top (root).
    The nodes are taken in pairs to generate a new node referencing them. This new node is then
    added to the list that will be sent to the next recursive level, until there is only one (the root).
     */
    public void buildTree(List<SignatureNode> nodes) {
        if (nodes.size() > 0) {
            if (nodes.size() == 1) {
                root = nodes.get(0);
            } else {
                List<SignatureNode> newLevel = new LinkedList<>();
                for (int i = 0; i < nodes.size(); i += 2) {
                    SignatureNode newNode = new SignatureNode();
                    newNode.setLeftNode(nodes.get(i));
                    newNode.setRightNode(nodes.size() == i + 1 ? null : nodes.get(i + 1));
                    newNode.setValue(calculateNodeValue(newNode.getLeftNode(), newNode.getRightNode()));
                    newLevel.add(newNode);
                }
                buildTree(newLevel);
            }
        }
    }

    /*
    Orphan leaves have their value sent to the next level as is (concatenated with nothing)
     */
    private String calculateNodeValue(SignatureNode left, SignatureNode right) {
        return DigestUtils.sha256Hex(left.getValue().concat(right == null ? "" : right.getValue()));
    }

    /*
    The lookup is also done recursively, from top to bottom, left to right.
    Every path that does not lead to a positive match, returns an empty string.
    If a result is found, the recursive call chain will build the hash chain concatenating
    the current node value, from the bottom up.
     */
    public String retrieveHashChain(String value) {
        return retrieveHashChain(root, value);
    }

    private String retrieveHashChain(SignatureNode node, String value) {
        if (node == null) {
            return Strings.EMPTY;
        }

        if (node.getValue().equals(value)) {
            return value;
        }

        String leftHashChain = retrieveHashChain(node.getLeftNode(), value);
        if (Strings.isEmpty(leftHashChain)) {
            String rightHashChain = retrieveHashChain(node.getRightNode(), value);
            return Strings.isEmpty(rightHashChain) ? rightHashChain : String.format("%s -> %s", rightHashChain, node.getValue());
        }
        return String.format("%s -> %s", leftHashChain, node.getValue());
    }

    /*
    For debugging and better visualization, the whole tree can be printed.
    Like the lookup, the print function traverses the tree from top to bottom, left to right.
    A clear improvement would be to print it in an actual tree structure.
     */
    public void printTree() {
        System.out.println("\nFull tree (root first - left to right)");
        printNode(root);
    }

    private void printNode(SignatureNode node) {
        if (node != null) {
            System.out.println(node.getValue());
            printNode(node.getLeftNode());
            printNode(node.getRightNode());
        }
    }
}
