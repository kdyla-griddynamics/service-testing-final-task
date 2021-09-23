package com.griddynamics.gridu.qa.util;

import com.griddynamics.gridu.qa.user.CreateUserRequest;
import com.griddynamics.gridu.qa.user.CreateUserResponse;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public class SOAPWrappers {

  public static byte[] getCreateUserRequestSOAP(CreateUserRequest request)
      throws ParserConfigurationException, JAXBException, SOAPException, IOException {
    Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    document.createAttributeNS(ServicesConstants.USER_MANAGEMENT_NAMESPACE, "targetNamespace");

    Marshaller marshaller = JAXBContext.newInstance(CreateUserRequest.class).createMarshaller();
    marshaller.marshal(request, document);

    SOAPMessage soapMessage = MessageFactory.newInstance().createMessage();
    soapMessage.getSOAPBody().addDocument(document);

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    soapMessage.writeTo(outputStream);
    return outputStream.toByteArray();
  }

  public static CreateUserResponse extractCreateUserResponse(InputStream responseInputStream)
      throws ParserConfigurationException, IOException, SAXException, JAXBException {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    dbf.setNamespaceAware(true);
    DocumentBuilder db = dbf.newDocumentBuilder();

    Document d = db.parse(responseInputStream);
    Node createUserResponseNode = d
        .getElementsByTagNameNS(ServicesConstants.USER_MANAGEMENT_NAMESPACE,
            "createUserResponse").item(0);

    JAXBContext jc = JAXBContext.newInstance(CreateUserResponse.class);
    Unmarshaller unmarshaller = jc.createUnmarshaller();
    JAXBElement<CreateUserResponse> createUserResponseJAXBElement = unmarshaller
        .unmarshal(new DOMSource(createUserResponseNode), CreateUserResponse.class);
    return createUserResponseJAXBElement.getValue();
  }

}
