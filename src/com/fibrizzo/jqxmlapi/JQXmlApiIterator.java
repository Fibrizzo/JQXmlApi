package com.fibrizzo.jqxmlapi;

import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.Iterator;

public class JQXmlApiIterator implements Iterator<JQXmlApi> {
    private JQXmlApi xml;
    private ArrayList<Node> nodes;
    private int idx;

    public JQXmlApiIterator(JQXmlApi xml, ArrayList<Node> nodes ){
        this.xml = xml;
        this.nodes = nodes;
        this.idx = 0;
    }

    @Override
    public boolean hasNext() {
        return ( idx < nodes.size() );
    }

    @Override
    public JQXmlApi next() {
        return this.xml.clone().eq(idx++);
    }

    @Override
    public void remove() {

    }
}
