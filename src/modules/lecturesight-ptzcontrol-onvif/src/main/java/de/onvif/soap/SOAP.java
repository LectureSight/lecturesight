package de.onvif.soap;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class SOAP {

	private boolean logging = false; //

	private OnvifDevice onvifDevice;

	public SOAP(OnvifDevice onvifDevice) {
		super();

		this.onvifDevice = onvifDevice;
	}

	public Object createSOAPDeviceRequest(Object soapRequestElem, Object soapResponseElem,
			boolean needsAuthentification) throws SOAPException, ConnectException {
		return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getDeviceUri(), needsAuthentification);
	}

	public Object createSOAPPtzRequest(Object soapRequestElem, Object soapResponseElem, boolean needsAuthentification)
			throws SOAPException, ConnectException {
		return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getPtzUri(), needsAuthentification);
	}

	public Object createSOAPMediaRequest(Object soapRequestElem, Object soapResponseElem, boolean needsAuthentification)
			throws SOAPException, ConnectException {
		return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getMediaUri(), needsAuthentification);
	}

	public Object createSOAPImagingRequest(Object soapRequestElem, Object soapResponseElem,
			boolean needsAuthentification) throws SOAPException, ConnectException {
		return createSOAPRequest(soapRequestElem, soapResponseElem, onvifDevice.getImagingUri(), needsAuthentification);
	}

	/**
	 * 
	 * @param soapResponseElem
	 *            Answer object for SOAP request
	 * @return SOAP Response Element
	 * @throws SOAPException
	 * @throws ConnectException
	 */
	public Object createSOAPRequest(Object soapRequestElem, Object soapResponseElem, String soapUri,
			boolean needsAuthentification) throws SOAPException, ConnectException {
		SOAPConnection soapConnection = null;

		try {
			// Create SOAP Connection
			SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
			soapConnection = soapConnectionFactory.createConnection();

			SOAPMessage soapMessage = createSoapMessage(soapRequestElem, needsAuthentification);

			// Print the request message
			if (isLogging()) {
				System.out.print("Request SOAP Message (" + soapRequestElem.getClass().getSimpleName() + "): ");
				soapMessage.writeTo(System.out);
				System.out.println();
			}

			SOAPMessage soapResponse = null;
			soapResponse = soapConnection.call(soapMessage, soapUri);

			if (soapResponseElem == null) {
				throw new NullPointerException("Improper SOAP Response Element given (is null).");
			}

			// print SOAP Response
			if (isLogging()) {
				System.out.print("Response SOAP Message (" + soapResponseElem.getClass().getSimpleName() + "): ");
				soapResponse.writeTo(System.out);
				System.out.println();
			}

			String contextPath = soapResponseElem.getClass().getPackage().getName();
			ClassLoader cl = soapResponseElem.getClass().getClassLoader();

			JAXBContext context = JAXBContext.newInstance(contextPath, cl); // soapResponseElem.getClass()
			Unmarshaller unmarshaller = context.createUnmarshaller();

			try {
				soapResponseElem = unmarshaller.unmarshal(soapResponse.getSOAPBody().extractContentAsDocument());
			} catch (UnmarshalException e) {
				//System.out.println("unmarshall");
				// Fault soapFault = (Fault)
				// unmarshaller.unmarshal(soapResponse.getSOAPBody().extractContentAsDocument());
				onvifDevice.getLogger().warn("Could not unmarshal, ended in SOAP fault.");
				// throw new SOAPFaultException(soapFault);
			}

			return soapResponseElem;
		} catch (SocketException e) {
			//System.out.println("e");
			throw new ConnectException(e.getLocalizedMessage());
		} catch (SOAPException e) {
			//System.out.println("soap");
			onvifDevice.getLogger().error("Unhandled exception: " + e.getLocalizedMessage());
			return null;
		} catch (ParserConfigurationException | JAXBException | IOException e) {
			//System.out.println("jaxb");
			onvifDevice.getLogger().error("Unhandled exception:");
			e.printStackTrace();
			return null;
		} finally {
			try {
				//System.out.println("close");
				soapConnection.close();
			} catch (SOAPException e) {
				//System.out.println("f e");
				e.printStackTrace();
			}
		}
	}

	protected SOAPMessage createSoapMessage(Object soapRequestElem, boolean needAuthentification)
			throws SOAPException, ParserConfigurationException, JAXBException {
		MessageFactory messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
		SOAPMessage soapMessage = messageFactory.createMessage();

		Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();

		String contextPath = soapRequestElem.getClass().getPackage().getName();
		ClassLoader cl = soapRequestElem.getClass().getClassLoader();
		
		JAXBContext context = JAXBContext.newInstance(contextPath, cl); // soapRequestElem.getClass()

		Marshaller marshaller = context.createMarshaller();
		marshaller.marshal(soapRequestElem, document);

		// set the value for the soap scheme envelope to the required value
		NamedNodeMap map = document.getDocumentElement().getAttributes();
		for (int i = 0; i < map.getLength(); i++) {
			Attr node = (Attr) map.item(i);

			if (node.getValue() == "http://schemas.xmlsoap.org/soap/envelope/") {
				document.getDocumentElement().setAttribute(node.getName(), "http://www.w3.org/2003/05/soap-envelope");
			}
		}
		
		/*
		DOMImplementationLS domImplLS = (DOMImplementationLS) document.getImplementation();
		LSSerializer serializer = domImplLS.createLSSerializer();

		System.out.println(serializer.writeToString(document));
		System.out.println("-----------");
		*/
		
		soapMessage.getSOAPBody().addDocument(document);

		// if (needAuthentification)
			createSoapHeader(soapMessage);

		soapMessage.saveChanges();
		
		/*
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			soapMessage.writeTo(out);
			String strMsg = new String(out.toByteArray());
			System.out.println(strMsg);
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/

		return soapMessage;
	}

	protected void createSoapHeader(SOAPMessage soapMessage) throws SOAPException {
		onvifDevice.createNonce();
		String encrypedPassword = onvifDevice.getEncryptedPassword();
		if (encrypedPassword != null && onvifDevice.getUsername() != null) {

			SOAPPart sp = soapMessage.getSOAPPart();
			SOAPEnvelope se = sp.getEnvelope();
			SOAPHeader header = soapMessage.getSOAPHeader();
			se.addNamespaceDeclaration("wsse",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd");
			se.addNamespaceDeclaration("wsu",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd");

			SOAPElement securityElem = header.addChildElement("Security", "wsse");
			// securityElem.setAttribute("SOAP-ENV:mustUnderstand", "1");

			SOAPElement usernameTokenElem = securityElem.addChildElement("UsernameToken", "wsse");

			SOAPElement usernameElem = usernameTokenElem.addChildElement("Username", "wsse");
			usernameElem.setTextContent(onvifDevice.getUsername());

			SOAPElement passwordElem = usernameTokenElem.addChildElement("Password", "wsse");
			passwordElem.setAttribute("Type",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-username-token-profile-1.0#PasswordDigest");
			passwordElem.setTextContent(encrypedPassword);

			SOAPElement nonceElem = usernameTokenElem.addChildElement("Nonce", "wsse");
			nonceElem.setAttribute("EncodingType",
					"http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-soap-message-security-1.0#Base64Binary");
			nonceElem.setTextContent(onvifDevice.getEncryptedNonce());

			SOAPElement createdElem = usernameTokenElem.addChildElement("Created", "wsu");
			createdElem.setTextContent(onvifDevice.getLastUTCTime());
		}
	}

	public boolean isLogging() {
		return logging;
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}
}
