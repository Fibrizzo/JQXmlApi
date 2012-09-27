package com.fibrizzo.jqxmlapi;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

abstract class JQXmlUtils {

    public static ArrayList<Node> resultToArray(Object obj){
        ArrayList<Node> arrayList = new ArrayList<Node>();
        if( obj instanceof NodeList){
            NodeList nodeList = (NodeList) obj;
            for( int i=0; nodeList.getLength()>i; i++ ){
                arrayList.add( nodeList.item(i));
            }
        }else if ( obj == null ){
        }else{
            throw new RuntimeException( "Unsupported type expecting: null, Node, NodeList");
        }
        return arrayList;
    }

    public static ArrayList<Node> resultToArray(Node node){
        ArrayList<Node> arrayList = new ArrayList<Node>();
        arrayList.add( node );
        return arrayList;
    }

    public static ArrayList<Node> cloneArrayList(ArrayList<Node> array){
        ArrayList<Node> clone = new ArrayList<Node>();
        for( Node n:array )clone.add(n);
        return clone;
    }

    public static DocumentFragment arrayListToDocumentFragment( ArrayList<Node> nodes, Document doc ){
        DocumentFragment documentFragment = doc.createDocumentFragment();
        for(Node n: nodes){
            Node nodeClone = n.cloneNode(true);
            doc.adoptNode( nodeClone );
            documentFragment.appendChild( nodeClone );
        }
        return documentFragment;
    }

    public static void printDocument(Document doc, OutputStream out){
        try{
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            transformer.transform(new DOMSource(doc),
                    new StreamResult(new OutputStreamWriter(out, "UTF-8")));
        }catch (Exception e){
            System.out.println("Wystąpił błąd uniemożliwiający wydrukowanie elementu\n");
            System.out.print( e );
        }
    }
    /*
    public static void printDocument(Document doc, OutputStream out) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc),
                new StreamResult(new OutputStreamWriter(out, "UTF-8")));
    }
    */

}
