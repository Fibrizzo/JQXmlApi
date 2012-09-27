package com.fibrizzo.jqxmlapi;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileInputStream;
import java.io.InputStream;

public class JQXmlSourceResolver {

    public Document resolve(String path) throws Exception{
        FileInputStream fis = new FileInputStream(path);
        return build(fis);
    }

    static public Document build(InputStream inputStream) throws Exception{
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(inputStream);
    }

}
