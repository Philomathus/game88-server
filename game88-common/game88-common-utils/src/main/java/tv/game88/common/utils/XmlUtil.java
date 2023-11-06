package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

@Log4j2
public class XmlUtil {

    public static Document getDocument( String xml ) throws Exception {
        //获取具体的解析器
        DocumentBuilder db  = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        InputSource     src = new InputSource();
        src.setCharacterStream( new StringReader( xml ) );
        //解析xml文件，获取document对象
        return db.parse( src );
    }

    public static String elementToString( Element element ) throws Exception {
        DocumentBuilder db  = DocumentBuilderFactory.newInstance().newDocumentBuilder();

        Document newDoc = db.newDocument();
        newDoc.appendChild( element );

        DOMSource domSource = new DOMSource(newDoc);
        StringWriter       stringWriter       = new StringWriter();
        StreamResult       streamResult       = new StreamResult(stringWriter);
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer        transformer        = transformerFactory.newTransformer();
        transformer.transform(domSource, streamResult);
        return stringWriter.toString();
    }
}