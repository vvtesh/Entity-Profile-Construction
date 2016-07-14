package in.ac.iiitd.pag.util;


import java.io.StringReader;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class XMLUtil {

	public static String getStringElement(StartElement startElement, String elementName) {
		Attribute bodyAttr = startElement.getAttributeByName(new QName(elementName));
		   String body = null;
		   if (bodyAttr != null)
			   body = bodyAttr.getValue();
		return body;
	}


	public static int getIntElement(StartElement startElement, String elementName) {
		int parent = 0;
		Attribute parentIdAttr = startElement.getAttributeByName(new QName(elementName));
		   if (parentIdAttr != null) {
			   parent = Integer.parseInt(parentIdAttr.getValue());
		   }
		return parent;
	}

	public static String getItem(String line, String element) throws XMLStreamException {
		String title = null;
		
		XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();        
        XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader(new StringReader(line));        
        while(xmlEventReader.hasNext()){
           XMLEvent xmlEvent = xmlEventReader.nextEvent();
           if (xmlEvent.isStartElement()){
               StartElement startElement = xmlEvent.asStartElement();
               if(startElement.getName().getLocalPart().equalsIgnoreCase("row")){
            	   title = XMLUtil.getStringElement(startElement, element);
               }
           }
        }
        xmlEventReader.close();
        return title;
	}
	
	
}
