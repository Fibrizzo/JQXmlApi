package com.fibrizzo.jqxmlapi;

import java.util.ArrayList;

public class JQXmlLog implements Cloneable{
    private ArrayList<Exception> log;
    private boolean print_on_log;

    public JQXmlLog(){
        log = new ArrayList<Exception>();
        print_on_log = false;
    }

    public JQXmlLog( ArrayList<Exception> log, boolean print_on_log){
        this.log = log;
        this.print_on_log = print_on_log;
    }

    /**
     * funkcja pozwala na logowanie błędów
     */
    public void add(Exception e){
        log.add(e);
        debugPrint();
    }

    public void setPrintOnLog(boolean debug){
        this.print_on_log=debug;
    }

    private void debugPrint(){
        if(print_on_log){
            Exception e = log.get( log.size()-1 );
            e.printStackTrace();
        }
    }

    @Override
    public JQXmlLog clone(){
        return new JQXmlLog( (ArrayList<Exception>)log.clone(), this.print_on_log );
    }

}


