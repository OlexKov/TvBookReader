package com.example.bookreader.utility.bookutils.fb2parser;

import org.jetbrains.annotations.Nullable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class IdElement {

    protected String id;

    protected IdElement() {
    }

    protected IdElement(Node node) {
        NamedNodeMap map = node.getAttributes();
        for (int index = 0; index < map.getLength(); index++) {
            Node attr = map.item(index);
            if (attr.getNodeName().equals("id")) {
                id = attr.getNodeValue();
            }
        }
    }


    @Nullable
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
