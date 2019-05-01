package com.jhepp.logsigner.model;

/*
The basic component of the tree, holding the actual hashed value and references to the child nodes.
Alternatively a reference to the parent node could also be included, but for the current needs it is overkill.
 */
public class SignatureNode {
    private String value;
    private SignatureNode leftNode;
    private SignatureNode rightNode;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public SignatureNode getLeftNode() {
        return leftNode;
    }

    public void setLeftNode(SignatureNode leftNode) {
        this.leftNode = leftNode;
    }

    public SignatureNode getRightNode() {
        return rightNode;
    }

    public void setRightNode(SignatureNode rightNode) {
        this.rightNode = rightNode;
    }
}
