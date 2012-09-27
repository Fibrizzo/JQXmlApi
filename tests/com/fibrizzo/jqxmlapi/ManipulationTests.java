package com.fibrizzo.jqxmlapi;


import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

public class ManipulationTests {
    JQXmlApi xml;
    JQXmlApi books;
    JQXmlApi titles;
    JQXmlApi author;

    /**
     * Notes
     * - sprawdzić czy dokumenty zostają wstawione w odpowiednich miejscach
     */

    @Before
    public void setUp() {
        xml     = new JQXmlApi("utils/data/Manipulation.xml").setDebug(true);
        books   = xml.clone().get("//catalog/book");
        titles  = books.clone().get("title");
        author  = books.clone().get("author");
    }

    /**
     * Tworzy nowy elemnent o cechach podanych w parametrach
     * @param name Nazwa elementu
     * @param content Zawartość tekstowa elementu
     * @param attributesKV Lista atrybutów w postaci { {attrName,attrValue},...}
     * @param document Dokument dla którego tworzony jest nowy element
     * @return Nowy element o zadanych cechach
     */
    protected Element createElement( String name, String content, String[][] attributesKV, Document document ){
        Element el = document.createElement(name);
        for( String[] kv:attributesKV ){
            el.setAttribute( kv[0], kv[1]  );
        }
        el.appendChild( document.createTextNode(content) );
        return el;
    }

    @Test
    public void after_String(){
        author.after("<revision>1.0</revision>");
        JQXmlApi revisions = books.find("revision");
        //JQXmlUtils.printDocument( xml.doc, System.out );
        assertTrue(titles.size() == revisions.size());
    }

    @Test
    public void after_ArrayList(){
        author.after( titles.nodes);
        JQXmlApi allTitles = books.clone().find("title");
        //JQXmlUtils.printDocument( xml.doc, System.out );
        assertTrue( allTitles.size() == (titles.size()+1)*author.size() );
    }

    @Test
    public void after_JQXmlApi(){
        author.after( titles);
        JQXmlApi allTitles = books.clone().find("title");
        //JQXmlUtils.printDocument( xml.doc, System.out );
        assertTrue( allTitles.size() == (titles.size()+1)*author.size() );
    }

    @Test
    public void append_String(){
        books.append("<revision>1.0</revision>");
        JQXmlApi revisions = books.clone().find("revision");
        //JQXmlUtils.printDocument( xml.doc, System.out );
        assertTrue( books.size() == revisions.size() );
    }

    @Test
    public void append_ArrayList(){
        ArrayList<Node> arrayList = new ArrayList<Node>();
        String [][] attr = new String[][] {{"dialect","no"}};
        arrayList.add( createElement( "language", "English", attr, xml.doc) );
        arrayList.add( createElement( "language", "English", attr, xml.doc) );

        books.append( arrayList );
        //JQXmlUtils.printDocument( books.doc, System.out );

        JQXmlApi language = books.clone().find("language");
        assertTrue( books.size()*2 == language.size() );
    }

    @Test
    public void append_JQXmlApi(){
        author.append( titles);
        JQXmlApi allTitles = books.clone().find("title");
        //JQXmlUtils.printDocument( xml.doc, System.out );
        assertTrue( allTitles.size() == (titles.size()+1)*author.size() );
    }

    @Test
    public void appendTo_JQXmlApi(){
        assertTrue( true );
    }

    @Test
    public void development(){
        String [][] attr = new String[][] {{"dialect","no"}};
        Element el = createElement( "language", "English", attr, xml.doc);
        Node n = el;
        while( n.getParentNode()!=null ){

        }
        assertTrue( true );
    }

}
