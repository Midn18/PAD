package messageAgent.sender;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import messageAgent.utilities.IOperation;
import messageAgent.utilities.TransportService;

public class Sender {

    public static void main(String[] args) throws IOException {

        while (true) {
            String message;
            String receiver;
            List<String> receivers = new ArrayList<>();
            Socket s;
            BufferedReader buffRead = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("SENDER OPTIONS:");
            try {
                s = new Socket("localhost", 1488);
                System.out.println("--Connection succeeded--");
                System.out.println("--Input the receivers--");
                while (!(receiver = buffRead.readLine()).equals(".")) {
                    receivers.add(receiver);
                }
                System.out.print("Input the message: ");
                message = buffRead.readLine();
                message = formXMLMessage(message, receivers);
                System.out.println("--Serialized data in XML--");
                System.out.println(message);
                IOperation sender = new TransportService(s);
                sender.writeAsync(message);
                System.out.println("--Data succesfully transmitted--");
            } catch (SocketException e) {
                System.out.println("--Connection failed--");
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            } catch (TransformerException e) {
                e.printStackTrace();
            }
        }
    }

    static String formXMLMessage(String message, List<String> rec) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        DOMImplementation impl = builder.getDOMImplementation();
        Document doc = impl.createDocument(null, null, null);
        Element rootNode = doc.createElement("message");
        rootNode.setAttribute("text", message);
        doc.appendChild(rootNode);
        for (int i = 0; i < rec.size(); i++) {
            Element childNode = doc.createElement("messageAgent/receiver");
            childNode.setTextContent(rec.get(i));
            rootNode.appendChild(childNode);
        }
        DOMSource domSource = new DOMSource(doc);
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        StringWriter sw = new StringWriter();
        StreamResult sr = new StreamResult(sw);
        transformer.transform(domSource, sr);
        return sw.toString();
    }
}
