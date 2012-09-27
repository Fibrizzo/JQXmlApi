import com.fibrizzo.jqxmlapi.JQXmlApi;

public class Test {
    public static void main(String[] args) throws Exception{
        String path = "utils/data/Example_1.xml";
        JQXmlApi xml = new JQXmlApi(path);
        xml.get("//CATALOG/CD/TITLE");
        System.out.println( xml.getText() );
    }
}
