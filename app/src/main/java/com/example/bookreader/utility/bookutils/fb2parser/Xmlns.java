package com.example.bookreader.utility.bookutils.fb2parser;

import org.w3c.dom.Node;

public class Xmlns {
    
    protected String name;
    protected String value;

    public Xmlns() {
    }

    Xmlns(Node node) {
        this.name = node.getNodeName();
        this.value = node.getNodeValue();
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
