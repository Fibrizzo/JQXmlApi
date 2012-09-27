package com.fibrizzo.jqxmlapi;

import javax.xml.xpath.*;

import org.w3c.dom.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.fibrizzo.jqxmlapi.JQXmlUtils.resultToArray;

public class JQXmlApi implements Iterable<JQXmlApi>, Cloneable {
    private JQXmlLog log;
    private XPath xpath = XPathFactory.newInstance().newXPath();

    protected Document doc = null;
    protected ArrayList<Node> previousNodes = new ArrayList<Node>();
    protected ArrayList<Node> nodes = new ArrayList<Node>();

    public JQXmlApi (String path){
        this.log = new JQXmlLog();
        JQXmlSourceResolver resolver = new JQXmlSourceResolver();
        try{
            this.doc = resolver.resolve(path);
            this.nodes.add(this.doc);
        }catch (Exception e){
            log.add(e);
            this.doc = null;
            this.nodes = null;
        }
    }

    public JQXmlApi (InputStream inputStream){
        try{
            this.doc = JQXmlSourceResolver.build(inputStream);
            this.nodes.add( this.doc );
        }catch (Exception e){
            log.add(e);
            this.doc = null;
            this.nodes = null;
        }
    }

    public JQXmlApi (Document doc, ArrayList<Node> nodes, ArrayList<Node> previousNodes, JQXmlLog log ){
        this.doc = doc;
        this.previousNodes = previousNodes;
        this.nodes = nodes;
        this.log = log;
    }

    private ArrayList<Node> removeRepetition( ArrayList<Node> arrayList ){
        for(int i=0; i<arrayList.size(); i++){
            Node node = arrayList.get(i);
            for(int j=i+1; j<arrayList.size(); j++){
                if( node.isSameNode( arrayList.get(j) ) ){
                    arrayList.remove(j);
                }
            }
        }
        return  arrayList;
    }

    private void setNodes( ArrayList<Node> result ){
        this.previousNodes = this.nodes;
        this.nodes = removeRepetition( result );
    }

    private void setNodes(Node node){
        setNodes(JQXmlUtils.resultToArray(node));
    }

    private void setNodes(Object result){
        setNodes( JQXmlUtils.resultToArray(result) );
    }

    private void setNodes(){
        setNodes( new ArrayList<Node>() );
    }

    public JQXmlApi setDebug(boolean debug){
        this.log.setPrintOnLog(debug);
        return this;
    }

    /* Method is obsolete due to end method - implementation should be moved */
    public JQXmlApi getPrevious(){
        this.nodes = this.previousNodes;
        this.previousNodes = new ArrayList<Node>();
        return this;
    }

    private XPathExpression compileXPathExpression(String expression){
        XPathExpression expr = null;
        try{
            expr = xpath.compile(expression);
        }catch(Exception e){
            log.add( new Exception("Can not compile to xpath expression: "+expression , e) );
        }
        return expr;
    }

    private ArrayList<Node> getNodes( String expression ) throws XPathExpressionException {
        XPathExpression expr = compileXPathExpression(expression);
        Object nodes = expr.evaluate(this.doc, XPathConstants.NODESET);
        return resultToArray(nodes);
    }

    protected ArrayList<Node> get(String expression, ArrayList<Node> list)throws Exception{
        XPathExpression expr = compileXPathExpression(expression);
        ArrayList<Node> results = new ArrayList<Node>();

        for( Node n: list){
            Object result = expr.evaluate(n, XPathConstants.NODESET);
            results.addAll( resultToArray(result) );
        }

        return results;
    }

    public JQXmlApi get(String expression) {
        try{
            ArrayList<Node> results = get(expression, this.nodes);
            setNodes(results);
        }
        catch (Exception e){
            log.add( e );
            setNodes();
        }
        return this;
    }

    public String getText(){
        StringBuilder stringBuilder = new StringBuilder();
        for (Node n: this.nodes ){
            stringBuilder.append( n.getTextContent() );
        }
        return stringBuilder.toString();
    }

    public JQXmlApi eq(int idx){
        if( Math.abs(this.nodes.size()) <= idx){
            setNodes( new ArrayList<Node>());
            log.add( new Exception("Index is out of scope (index:"+idx+" ,scope:"+this.nodes.size()+")" ) );
        }else if(idx >=0){
            setNodes( this.nodes.get(idx) );
        }else{
            setNodes( this.nodes.get(this.nodes.size() + idx) );
        }
        return this;
    }

    @Override
    public JQXmlApi clone(){
        return new JQXmlApi(
            this.doc,
            JQXmlUtils.cloneArrayList(this.nodes),
            JQXmlUtils.cloneArrayList(this.previousNodes),
            this.log.clone()
        );
    }

    @Override
    public Iterator<JQXmlApi> iterator() {
        return new JQXmlApiIterator( this, this.nodes );
    }

    public JQXmlApi getDocument(){
        setNodes( this.doc );
        return this;
    }

    public JQXmlApi add( JQXmlApi jqxml ){
        ArrayList<Node> addends = jqxml.nodes;
        return add(addends);
    }

    private JQXmlApi add( ArrayList<Node> addends ){
        ArrayList<Node> nodes = JQXmlUtils.cloneArrayList( this.nodes );
        for( Node a:addends ){
            boolean add = true;
            for(Node n:nodes){
                if( a.isSameNode(n)){
                    add=false;
                    break;
                }
            }
            if(add)nodes.add(a);
        }
        setNodes(nodes);
        return this;
    }


    public int size(){
        return this.nodes.size();
    }


    public JQXmlApi andSelf(){
        return add( this.previousNodes );
    }

    public JQXmlApi first(){
        return this.eq(0);
    }

    public JQXmlApi last(){
        return this.eq(this.size()-1);
    }

    public JQXmlApi children(){
        this.get("child::*");
        return this;
    }

    public JQXmlApi closest( String tagName ){
        this.get("ancestor::"+tagName+"[1]");
        return this;
    }

    public JQXmlApi contents(){
        this.get("node()");
        return this;
    }

    //rbart
    public JQXmlApi each(){
        //To-do
        return this;
    }

    public JQXmlApi end(){
        getPrevious();
        return this;
    }

    public JQXmlApi filter(String expression){
        try{
            XPathExpression expr = compileXPathExpression(expression);
            Object result = expr.evaluate(this.doc, XPathConstants.NODESET);
            ArrayList<Node> resultList = resultToArray(result);
            resultList.retainAll( this.nodes );
            setNodes( resultList );
            return this;
        }catch (Exception e){
            log.add(e);
            return this;
        }
    }

    public JQXmlApi find(String expression){
        return get(expression);
    }

    protected boolean has(XPathExpression expr, Node n){
        NodeList nodeList = null;
        try{
            nodeList = (NodeList)expr.evaluate(n, XPathConstants.NODESET );
        }catch(Exception e){
            log.add(e);
        }
        return (nodeList!=null && nodeList.getLength()>0);
    }

    public JQXmlApi has(String expression){
        XPathExpression expr = null;
            try{
                expr = compileXPathExpression(expression);
            }catch (Exception e){
                log.add(e);
        }

        ArrayList<Node> result = new ArrayList<Node>();
        for(Node n: nodes){
            if( has(expr, n) ){
                result.add(n);
            }
        }
        setNodes(result);
        return this;
    }

    public boolean is(String expression){
        try{
            XPathExpression expr = compileXPathExpression(expression);
            Object result = expr.evaluate(this.doc, XPathConstants.NODESET);
            ArrayList<Node> resultList = resultToArray(result);
            resultList.retainAll( this.nodes );
            return ( resultList.size()>0 );
        }catch (Exception e){
            log.add(e);
            return false;
        }
    }

    public boolean is(Node node){
        boolean result = false;
        for(Node n:this.nodes){
            if( n.isSameNode(node)){
                result=true;
                break;
            }
        }
        return result;
    }

    public boolean is(JQXmlApi set){
        boolean result = false;
        for(Node n:this.nodes){
            if( set.nodes.contains(n) ){
                result = true;
            }
        }
        return result;
    }

    /** ================================================================== **/
    /** =                         NEXT FUNCTIONS                           **/
    /** ================================================================== **/

    private boolean addOnlyElementsNodes(Node node, ArrayList<Node> arrayList){
        if( node==null ){
            return false;
        }
        short type = node.getNodeType();
        if( type == Node.ELEMENT_NODE || type == Node.DOCUMENT_NODE ){
            arrayList.add(node);
            return true;
        }
        return false;
    }

    private ArrayList<Node> getNextSibling(ArrayList<Node> nodes){
        ArrayList<Node> result = new ArrayList<Node>();
        for(Node n:nodes){
            do{
                n = n.getNextSibling();
            }while (n!=null && !addOnlyElementsNodes(n, result));
        }
        return result;
    }

    public JQXmlApi next(){
        ArrayList<Node> result= getNextSibling(this.nodes);
        setNodes(result);
        return this;
    }


    public JQXmlApi next(String expression){
        ArrayList<Node> siblings = getNextSibling(this.nodes);
        try{
            ArrayList<Node> result = getNodes(expression);
            result.retainAll( siblings );
            setNodes(result);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    private ArrayList<Node> getAllNextSiblings(){
        ArrayList<Node> result = new ArrayList<Node>();
        for(Node n:this.nodes){
            Node s = n.getNextSibling();
            while( s!=null ){
                addOnlyElementsNodes(s, result);
                s = s.getNextSibling();
            }
        }
        return result;
    }

    public JQXmlApi nextAll(){
        ArrayList<Node> result = getAllNextSiblings();
        setNodes(result);
        return this;
    }

    public JQXmlApi nextAll(String expression){
        ArrayList<Node> siblings = getAllNextSiblings();
        try{
            ArrayList<Node>result = getNodes(expression);
            result.retainAll( siblings );
            setNodes(result);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    private ArrayList<Node> getNextUntil(Node untilNode){
        ArrayList<Node> result = new ArrayList<Node>();
        for (Node n : this.nodes){
            Node s = n.getNextSibling();
            while ( s != null && !s.isSameNode(untilNode) ){
                addOnlyElementsNodes(s, result);
                s = s.getNextSibling();
            }
        }
        return result;
    }

    public JQXmlApi nextUntil(Node untilNode) {
        ArrayList<Node> result = getNextUntil(untilNode);
        setNodes(result);
        return this;
    }

    public JQXmlApi nextUntil(Node untilNode,String expression){
        ArrayList<Node> results = getNextUntil(untilNode);
        try{
            results.retainAll( getNodes(expression) );
            setNodes(results);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    public JQXmlApi not(String expression){
        try{
            ArrayList<Node> not = getNodes(expression);
            ArrayList<Node> result = new ArrayList<Node>(this.nodes);
            result.removeAll(not);
            setNodes(result);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    public JQXmlApi not( ArrayList<Node> excludedNodes ){
        ArrayList<Node> result = new ArrayList<Node>(this.nodes);
        result.removeAll(excludedNodes);
        setNodes( result );
        return this;
    }

    public JQXmlApi not( JQXmlApi excluded ){
        return not( excluded.nodes );
    }

    public ArrayList<Node> getParent( ArrayList<Node> nodes ){
        ArrayList<Node> result = new ArrayList<Node>();
        for( Node n : nodes){
            n = n.getParentNode();
            addOnlyElementsNodes(n, result);
        }
        return result;
    }

    public JQXmlApi parent(){
        ArrayList<Node> result = getParent(this.nodes);
        setNodes(result);
        return this;
    }

    public JQXmlApi parent( String expression){
        try{
            ArrayList<Node> result = getParent(this.nodes);
            result.retainAll( getNodes(expression) );
            setNodes(result);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    private ArrayList<Node> getParents(List<Node> nodes){
        ArrayList<Node> results = new ArrayList<Node>();
        for(Node n: this.nodes){
            Node r = n.getParentNode();
            while (r!=null && r.getNodeType()!=Node.DOCUMENT_NODE){
                results.add(r);
                r=r.getParentNode();
            }
        }
        return results;
    }

    public JQXmlApi parents(){
        ArrayList<Node> results = getParents(this.nodes);
        setNodes(results);
        return this;
    }

    public JQXmlApi parents(String expression){
        try{
            ArrayList<Node> results = getParents(this.nodes);
            ArrayList<Node> only = getNodes(expression);
            results.retainAll( only );
            setNodes(results);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    public JQXmlApi parentsUntil(Node untilNode){
        ArrayList<Node> parents = getParents(this.nodes);
        for( int i=0; i<parents.size(); i++){
            if( parents.get(i).isSameNode(untilNode)){
                parents = new ArrayList<Node>(parents.subList(0, i));
                break;
            }
        }
        setNodes(parents);
        return this;
    }

    private ArrayList<Node> getParents(Node node, ArrayList<Node> untilNodes){
        ArrayList<Node> result = new ArrayList<Node>();
        Node parentNode = node.getParentNode();
        while (
                parentNode!=null
                && parentNode.getNodeType()!=Node.DOCUMENT_NODE
                && ( untilNodes==null || !untilNodes.contains(parentNode) )
              ){
            result.add(parentNode);
            parentNode = parentNode.getParentNode();
        }
        return result;
    }

    public JQXmlApi parentsUntil( String expression ){
        try{
            ArrayList<Node> untilNodes = getNodes(expression);
            ArrayList<Node> result = new ArrayList<Node>();
            for(Node n:this.nodes){
                result.addAll( getParents( n, untilNodes ) );
            }
            setNodes(result);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    public JQXmlApi prev(){
        ArrayList<Node> result = new ArrayList<Node>();
        for(Node n :this.nodes){
            Node previous = n.getPreviousSibling();
            while( previous!=null && !addOnlyElementsNodes(previous, result) ){
                previous = previous.getPreviousSibling();
            }
        }
        setNodes( result );
        return this;
    }

    public JQXmlApi prevAll(){
        ArrayList<Node> result = new ArrayList<Node>();
        for(Node n:this.nodes){
            Node previous = n.getPreviousSibling();
            while (previous!=null){
                addOnlyElementsNodes( previous, result );
                previous = previous.getPreviousSibling();
            }
        }
        setNodes(result);
        return this;
    }

    private ArrayList<Node> getPrevUntil(ArrayList<Node> untilNodes){
        ArrayList<Node> result = new ArrayList<Node>();
        for(Node n:this.nodes ){
            Node previous = n.getPreviousSibling();
            while( previous!=null && !untilNodes.contains(previous) ){
                addOnlyElementsNodes( previous, result );
                previous = previous.getPreviousSibling();
            }
        }
        return result;
    }

    public JQXmlApi prevUntil(Node untilNode){
        ArrayList<Node> until = new ArrayList<Node>();
        until.add(untilNode);
        ArrayList<Node> result = getPrevUntil( until );
        setNodes(result);
        return this;
    }

    public JQXmlApi prevUntil(String expression){
        try{
            ArrayList<Node> result = getPrevUntil( getNodes(expression) );
            setNodes(result);
        }catch (Exception e){
            log.add(e);
            setNodes();
        }
        return this;
    }

    public JQXmlApi prevUntil(JQXmlApi until){
        ArrayList<Node> result = getPrevUntil( until.nodes );
        setNodes(result);
        return this;
    }

    public JQXmlApi siblings(){
        ArrayList<Node> result = new ArrayList<Node>();
        ArrayList<Node> parents = getParent(this.nodes);
        removeRepetition( parents );
        for(Node p:parents){
            ArrayList<Node> childs = JQXmlUtils.resultToArray( p.getChildNodes() );
            for(Node c:childs) addOnlyElementsNodes(c ,result);
        }
        setNodes( result );
        return this;
    }

    public JQXmlApi slice(int start, int end){
        int length = this.nodes.size();
        int realFrom = (start<0)? length+start : start;
        int realTo   = (end<0)?   length+end   : end;

        if( realFrom<0 || realTo>length || realFrom>realTo){
            throw new IndexOutOfBoundsException(
                "JQXmlApi size of elements:" + length +
                " (start:"+start+" end:"+end+" -evalueted-> start:"+realFrom+" end:"+realTo+" )"
            );
        }
        ArrayList<Node> result = new ArrayList<Node>( this.nodes.subList(realFrom, realTo) );
        setNodes(result);
        return this;
    }

    public JQXmlApi slice(int start){
        return slice( start, this.nodes.size() );
    }

    /** ================================================================== **/
    /** =                           ATTRIBUTES                             **/
    /** ================================================================== **/

    public String attr(String attrName){
        if( this.nodes.size()==0 ){
            return null;
        }
        Node node = this.nodes.get(0);
        if( !node.hasAttributes() ){
            return null;
        }else{
            Node attr = node.getAttributes().getNamedItem(attrName);
            return (attr == null)? null:attr.getNodeValue();
        }
    }

    public void attr(String attrName, String attrValue ){
        for(Node n:this.nodes){
            Element e = (Element) n;
            e.setAttribute(attrName, attrValue);
        }
    }

    public JQXmlApi removeAttr(String attrName){
        for(Node n:this.nodes){
            Element e = (Element) n;
            e.removeAttribute(attrName);
        }
        return this;
    }

    //rbart to implement eventualy
    public JQXmlApi toogleAttr(){
        return this;
    }

    /** ================================================================== **/
    /** =                           MANIPULATION                           **/
    /** ================================================================== **/
    protected DocumentFragment createFragment(String xmlFragment){
        DocumentFragment df = this.doc.createDocumentFragment();
        String xml = "<root>"+ xmlFragment.trim() +"</root>";
        JQXmlApi jqXmlApi = new JQXmlApi( new ByteArrayInputStream(xml.getBytes() ) );
        NodeList nl = jqXmlApi.doc.getChildNodes().item(0).getChildNodes();
        for(int i=0; i<nl.getLength(); i++){
            Node n =this.doc.adoptNode( nl.item(i) );
            df.appendChild( n );
        }
        return df;
    }

    public JQXmlApi xml(String xmlFragment){
        Element e;
        DocumentFragment df = createFragment(xmlFragment);
        for(Node n:this.nodes){
            e = (Element) n;
            empty(e);
            e.appendChild( df.cloneNode(true) );
        }
        return this;
    }

    protected void empty(Node n){
        for( Node c: resultToArray(n.getChildNodes()) ){
            n.removeChild(c);
        }
    }

    public JQXmlApi after (DocumentFragment documentFragment){
        for(Node n:this.nodes){
            DocumentFragment dfc = (DocumentFragment)documentFragment.cloneNode(true);
            Node nextSibling = n.getNextSibling();
            Node parent = n.getParentNode();
            if( nextSibling!=null){
                NodeList toInsert = dfc.getChildNodes();
                while( toInsert.getLength()>0){
                    parent.insertBefore( toInsert.item(0), nextSibling );
                }
            }else{
                n.appendChild( dfc );
            }
        }
        return this;
    }

    public JQXmlApi after(String xmlFragment){
        DocumentFragment df = createFragment(xmlFragment);
        return after(df);
    }

    public JQXmlApi after(ArrayList<Node> nodes){
        DocumentFragment documentFragment = JQXmlUtils.arrayListToDocumentFragment(nodes, this.doc);
        return after(documentFragment);
    }

    public JQXmlApi after( JQXmlApi jqXmlApi){
        return after(jqXmlApi.nodes);
    }

    public JQXmlApi append( DocumentFragment documentFragment ){
        for(Node n:this.nodes){
            n.appendChild(documentFragment.cloneNode(true));
        }
        return this;
    }

    public JQXmlApi append(String xmlFragment){
        DocumentFragment df = createFragment(xmlFragment);
        return append(df);
    }

    public JQXmlApi append(ArrayList<Node> nodes){
        DocumentFragment documentFragment = JQXmlUtils.arrayListToDocumentFragment(nodes, this.doc);
        return append(documentFragment);
    }

    public JQXmlApi append(JQXmlApi jqXmlApi){
        return append( jqXmlApi.nodes );
    }


    public JQXmlApi appendTo( JQXmlApi newParent ){
        DocumentFragment fragment = JQXmlUtils.arrayListToDocumentFragment( this.nodes, newParent.doc );
        newParent.append(fragment);
        return this;
    }




}
