package com.fibrizzo.jqxmlapi;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class TraversingTests {

    JQXmlApi xml;
    JQXmlApi cds;
    JQXmlApi titles;
    JQXmlApi artists;

    @Before
    public void setUp() {
        xml     = new JQXmlApi("utils/data/Example_1.xml").setDebug(true);
        cds     = xml.clone().get("//CATALOG/CD");
        titles  = cds.clone().get("TITLE");
        artists = cds.clone().get("ARTIST");
    }

    @Test
    public void testAdding(){
        JQXmlApi sum = xml.clone().add(titles).add(artists);
        assertEquals(sum.nodes.size()-1, ( titles.nodes.size() + artists.nodes.size() ) );
    }

    @Test
    public void testAddingSame(){
        JQXmlApi titles1 = titles.clone();
        JQXmlApi titles2 = titles.clone();
        JQXmlApi sum = titles1.clone().add(titles2);
        assertEquals( sum.size(), titles1.size() );
        assertEquals( sum.size(), titles2.size() );
    }

    @Test
    public void testAndSelf1(){
        JQXmlApi cdsAndDoc = cds.clone().andSelf();
        assertEquals( cds.size()+1, cdsAndDoc.size() );
    }

    @Test
    public void testAndSelf2(){
        JQXmlApi cdsAndTitles = cds.clone().get("TITLE").andSelf();
        assertEquals( cdsAndTitles.size(), cds.size()+titles.size() );
    }

    @Test
    public void testFirst(){
        JQXmlApi firstcd1 = cds.clone().first();
        JQXmlApi firstcd2 = xml.clone().get("//CATALOG/CD[1]");
        assertEquals(firstcd1.size(), 1 );
        assertEquals( firstcd1.nodes.get(0), firstcd2.nodes.get(0) );
    }

    @Test
    public void testLast(){
        JQXmlApi lastcd1 = cds.clone().last();
        JQXmlApi lastcd2 = xml.clone().get("//CATALOG/CD[last()]");
        assertEquals( lastcd1.size(), 1 );
        assertEquals( lastcd1.nodes.get(0), lastcd2.nodes.get(0) );
    }

    @Test
    public void testChildren(){
        final JQXmlApi childrens1 = xml.clone().get("//CATALOG/CD/*") ;
        final JQXmlApi childrens2 = cds.clone().children();
        final JQXmlApi sum = childrens1.clone().add( childrens2 );
        assertEquals( childrens1.size(), childrens2.size() );
        assertEquals( childrens1.size(), sum.size()        );
    }

    @Test
    public void testChildrenWithTextNode(){
        JQXmlApi childrens1 = cds.clone().first().children();
        JQXmlApi childrens2 = cds.clone().last().children();
        assertEquals( childrens1.size(), childrens2.size() );
    }

    @Test
    public void testContents(){
        JQXmlApi childrens1 = cds.clone().first().contents();
        assertEquals( 15, childrens1.size() );
    }

    @Test
    public void testFilter(){
        JQXmlApi cds = this.cds.clone();
        cds.filter("//CATALOG/CD");
        assertEquals( cds.size(), this.cds.size() );
    }

    @Test
    public void testFilterRemoveExtraElements(){
        JQXmlApi cdsFrom1985 = this.cds.clone().filter("//CATALOG/CD[YEAR=1985]");
        assertEquals( 2, cdsFrom1985.size() );
    }

    @Test
    public void testHas(){
        final JQXmlApi cdsFrom1985 = this.cds.clone().has("self::node()[YEAR=1985]");
        assertEquals( 2, cdsFrom1985.size() );
    }

    @Test
    public void testHasNoElements(){
        final JQXmlApi cdsFrom1985 = this.cds.clone().has("self::node()[YEAR=2003]");
        assertEquals( 0, cdsFrom1985.size() );
    }

    @Test
    public void testIsExpression(){
        final JQXmlApi cds = this.cds.clone();
        assertTrue( cds.is("//CD") );
    }

    @Test
    public void testIsExpressionOnlyOneElementMaches(){
        final JQXmlApi cds = this.cds.clone();
        assertTrue( cds.is("//CATALOG/CD[last()]") );
    }

    @Test
    public void testIsNode(){
        JQXmlApi cds = this.cds.clone();
        Node n = cds.nodes.get(1);
        assertTrue( cds.is(n) );
    }

    @Test
    public void testIsJQXmlIsInSet(){
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi firstCd = this.cds.clone().last();
        assertTrue( firstCd.is( cds ) );
    }

    @Test
    public void testIsJQXmlIsNotInSet(){
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi titles = this.titles.clone();
        assertFalse( cds.is(titles) );
    }
    @Test
    public void testEq(){
        JQXmlApi firstCd1 = cds.clone().eq(0);
        JQXmlApi firstCd2 = xml.clone().get("//CATALOG/CD[1]");
        assertTrue( firstCd1.nodes.containsAll(firstCd2.nodes) );
        assertTrue( firstCd2.nodes.containsAll(firstCd1.nodes) );
    }

    @Test
    public void testEqNegativeIndex(){
        JQXmlApi oneBeforeLast = cds.clone().eq(-2);
        assertTrue( oneBeforeLast.get("TITLE").getText().equals("Red") );
    }

    @Test
    public void testNext(){
       final JQXmlApi firstcd = this.cds.clone().first();
       final JQXmlApi secondcd = this.cds.clone().eq(1);

       assertTrue( firstcd.next().is(secondcd) );
    }

    @Test
    public void testNextSelector(){
        final JQXmlApi firstcd = this.cds.clone().first();
        final JQXmlApi secondcd = this.cds.clone().eq(1);

        assertTrue( firstcd.next("//CD").is(secondcd) );
    }

    @Test
    public void testNextAll(){
        final JQXmlApi firstCd = this.cds.clone().first();
        final JQXmlApi cds = this.cds.clone();
        assertTrue( firstCd.nextAll().size() == (cds.size()-1) );
    }
    
    @Test
    public void testNextAllSelector(){
        final JQXmlApi firstCd = this.cds.clone().first();
        assertTrue( firstCd.nextAll("//CD[last()]").size()==1 );
    }

    @Test
    public void testNextUntil(){
        final JQXmlApi firstCd = cds.clone().first();
        final Node thirdNode = cds.clone().nodes.get(2);
        assert ( firstCd.nextUntil(thirdNode).size()==1 );
    }

    @Test
    public  void testNextUntilSelector(){
        final JQXmlApi firstCd = cds.clone().first();
        final Node thirdNode = cds.clone().nodes.get(3);
        assert ( firstCd.nextUntil(thirdNode, "//CD[position()<3]").size()==1 );
    }

    @Test
    public void testNotSelector(){
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi allCdExceptFirst = cds.clone().not("//CD[position()<2]");
        assertTrue( allCdExceptFirst.size() == (cds.size()-1) );
    }

    @Test
    public void testNotCollection(){
        final JQXmlApi firstCd = this.cds.clone().first();
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi cdsExceptFirst = this.cds.clone().not(firstCd.nodes);
        assertTrue(cdsExceptFirst.size() == cds.size() - 1);
    }

    @Test
    public void testNotJqxmlapi(){
        final JQXmlApi firstCd = this.cds.clone().first();
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi cdsExceptFirst = this.cds.clone().not( firstCd );
        assertTrue( cdsExceptFirst.size() == cds.size()-1 );
    }

    @Test
    public void testParent(){
        final JQXmlApi cdsParent = this.cds.clone().parent();
        final JQXmlApi catalog = this.cds.clone().get("/*");
        assertTrue( cdsParent.is( catalog ) );
    }

    @Test
    public void testParentSelector(){
        final JQXmlApi specialCd     = this.xml.clone().get("//CD[last()]");
        final JQXmlApi specialParent = titles.clone().parent("//CD[last()]");

        assertTrue( specialParent.is(specialCd) );
        assertTrue( specialParent.size()==1     );
    }

    @Test
    public void testParents(){
        final JQXmlApi firstTitleParents = this.titles.clone().first().parents();
        assertTrue( firstTitleParents.size()==2 );
    }

    @Test
    public void testParentsSelector(){
        final JQXmlApi firstTitleCdParents = this.titles.clone().first();
        firstTitleCdParents.parents("//CD");
        assertTrue( firstTitleCdParents.size()==1 );
        assertTrue( firstTitleCdParents.get("ARTIST").getText().equals("Bob Dylan") );
    }

    @Test
    public void testParentsUntilNode(){
        final Node catalogNode = this.cds.clone().first().parent().eq(0).nodes.get(0);
        final JQXmlApi parentsUntilCatalog = this.titles.clone().first();
        parentsUntilCatalog.parentsUntil(catalogNode);
        assertTrue( parentsUntilCatalog.size()==1 );
    }


    @Test
    public void testParentsUntilSelector(){
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi titlesParentsUntilCatalog = this.titles.clone().parentsUntil("//CATALOG");

        assertTrue( titlesParentsUntilCatalog.size() == titles.size() );
        assertTrue( titlesParentsUntilCatalog.is(cds) );
    }

    @Test
    public void testPrev(){
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi prev = this.cds.clone().prev();

        assertTrue( prev.size() == cds.size()-1 );
    }

    @Test
    public void testPrevAll(){
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi allPrevCds = this.cds.clone().prevAll();

        assertTrue( allPrevCds.size() == cds.size()-1 );
    }

    @Test
    public void testPrevAll2(){
        final JQXmlApi cds = this.cds.clone();
        final JQXmlApi prevOfLastCd = this.cds.clone().last().prevAll();

        assertTrue( prevOfLastCd.size()== cds.size()-1 );
    }

    @Test
    public void testPrevUntilNode(){
        final Node firstCd = this.cds.clone().first().nodes.get(0);
        final JQXmlApi cdsPrevFromLastUntilFirst = this.cds.clone().last().prevUntil(firstCd);

        assertTrue( cdsPrevFromLastUntilFirst.size() == this.cds.size()-2 );
    }

    @Test
    public void testPrevUntilSelector(){
        final JQXmlApi cdsPrevFromLastUntilSecond = this.cds.clone().last().prevUntil("//CD[position()=2]");
        assertTrue( cdsPrevFromLastUntilSecond.size() == this.cds.size()-3 );
    }

    @Test
    public void testPrevUntilJQ(){
        final JQXmlApi firstTwoCds = this.cds.clone().get("//CD[position()=2]");
        final JQXmlApi cdsPrevFromLastUntilSecond = this.cds.clone().last().prevUntil(firstTwoCds);
        assertTrue( cdsPrevFromLastUntilSecond.size() == this.cds.size()-3 );
    }

    @Test
    public void testSiblings(){
        final JQXmlApi artistsSiblings = this.artists.clone().siblings();
        assertTrue( artistsSiblings.size() == 6*this.cds.size() );
    }

    @Test
    public void testSliceError(){
        final JQXmlApi cdElements = this.cds.first().children();
        JQXmlApi result = null;
        Exception exception = null;
        try{
            result = cdElements.slice(0 , 15);
        }catch (Exception e){
            exception = e;
        }
        assertTrue( exception!=null );
        assertTrue( result==null );
    }

    @Test
    public void testSliceBoundaries(){
        JQXmlApi cdElements = this.cds.first().children();
        cdElements.slice(0,6);
        assertTrue( cdElements.size()==6 );
    }

    @Test
    public void testSliceNegatives(){
        JQXmlApi cdElements = this.cds.first().children();
        cdElements.slice(-4,-1);
        assertTrue( cdElements.size()==3);
    }

    @Test
    public void testSliceStart(){
        JQXmlApi cdElements = this.cds.first().children();
        cdElements.slice(4);
        assertTrue( cdElements.size()==2 );
    }

    @Test
    public void testAttr(){
        final JQXmlApi firstCdTitle = this.titles.clone().first();
        final String attrLangValue = firstCdTitle.attr("lang");
        assertTrue( attrLangValue.equals("eng") );
    }

    @Test
    public void testAttrNoNodes(){
        final JQXmlApi empty = this.cds.clone().first().get("ASD");
        final String value = empty.attr("value");
        assertNull( value );
    }

    @Test
    public void testAttrNoAtrr(){
        final JQXmlApi firstCd = this.cds.clone().first();
        final String attrValue = firstCd.attr("title");
        assertNull( attrValue );
    }

    @Test
    public void testCreateDocumentFragment(){
        final JQXmlApi lastCd = this.cds.clone().last();
        final String xmlFragment = "<NumberOfTracks>13</NumberOfTracks>";
        DocumentFragment dc = lastCd.createFragment( xmlFragment );
        assertTrue(dc.getChildNodes().getLength()==1);
    }

    @Test
    public void testXml(){
        final JQXmlApi lastCd = this.cds.clone().last();
        final String xmlFragment = "<NumberOfTracks>13</NumberOfTracks>";
        lastCd.xml(xmlFragment);
        assertTrue(lastCd.children().size()==1);
        setUp();
    }

    @Test
    public void testRemoveAttr(){
        final JQXmlApi lastCd = this.cds.clone().last();
        lastCd.attr( "attr1", "1" );
        assertTrue( lastCd.attr("attr1").equals("1"));
        lastCd.removeAttr("attr1");
        assertNull( lastCd.attr("attr1"));
    }


}
