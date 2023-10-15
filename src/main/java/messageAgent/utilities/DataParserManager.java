package messageAgent.utilities;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

public class DataParserManager {

    private Document doc;
    private final String stringData;

    public DataParserManager(String stringData) {
        this.stringData = stringData;
    }

    public List<String> getReceivers() {
        List<String> rec = new ArrayList<>();
        for (int i = 0; i < doc.getChildNodes().item(0).getChildNodes().getLength(); i++)
            rec.add(i, doc.getChildNodes().item(0).getChildNodes().item(i).getTextContent());
        return rec;
    }

    public String getMessage() {
        return doc.getChildNodes().item(0).getAttributes().getNamedItem("text").getTextContent();
    }

    public boolean CheckIfXml() {
        try {
            DocumentBuilderFactory documentBuildFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuildFactory.newDocumentBuilder();
            doc = documentBuilder.parse(new ByteArrayInputStream(stringData.getBytes()));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
