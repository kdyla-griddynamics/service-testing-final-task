package com.griddynamics.gridu.qa.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.dom.DOMSource;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SOAPWrappers {

  private static final Logger logger = Logger.getLogger(SOAPWrappers.class);

  public static byte[] getSOAPRequestOfGivenType(Class<?> requestClass, Object request) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    try {
      Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
      document.createAttributeNS(ServicesConstants.USER_MANAGEMENT_NAMESPACE, "targetNamespace");

      Marshaller marshaller = JAXBContext.newInstance(requestClass).createMarshaller();
      marshaller.marshal(request, document);

      SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
      soapMessage.getSOAPBody().addDocument(document);

      outputStream = new ByteArrayOutputStream();
      soapMessage.writeTo(outputStream);
      return outputStream.toByteArray();
    } catch (IOException | JAXBException | ParserConfigurationException | SOAPException exception) {
      logger.error(exception.getMessage());
    }
    return outputStream.toByteArray();
  }

  public static <T> T extractResponseOfGivenType(InputStream responseInputStream,
      Class<T> responseClass, String responseLocalName) {
    try {
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();

      Document d = db.parse(responseInputStream);
      Node createUserResponseNode = d
          .getElementsByTagNameNS(ServicesConstants.USER_MANAGEMENT_NAMESPACE,
              responseLocalName).item(0);

      JAXBContext jc = JAXBContext.newInstance(responseClass);
      Unmarshaller unmarshaller = jc.createUnmarshaller();
      JAXBElement<T> createUserResponseJAXBElement = unmarshaller
          .unmarshal(new DOMSource(createUserResponseNode), responseClass);
      return createUserResponseJAXBElement.getValue();
    } catch (IOException | JAXBException | ParserConfigurationException | SAXException exception) {
      logger.error(exception.getMessage());
    }
    throw new IllegalArgumentException("Could not extract the response");
  }

}
