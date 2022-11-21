package tv.game88.common.utils;

import lombok.extern.log4j.Log4j2;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;

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
}