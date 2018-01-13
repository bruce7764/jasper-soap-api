package com.jasperwireless.client;

import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * ChinaUnicomSoapTest
 *
 * @author Aaric, created on 2018-01-13T22:55.
 * @since 1.0-SNAPSHOT
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class ChinaUnicomSoapTest {

    private static String NAMESPACE_URI = "http://api.jasperwireless.com/ws/schema";
    private static String PREFIX = "jws";

    @Value("${jasper.baseUrl}")
    private String baseUrl;

    @Value("${jasper.licenseKey}")
    private String licenseKey;

    @Value("${jasper.username}")
    private String username;

    @Value("${jasper.password}")
    private String password;

    @Test
    public void testPrint() throws Exception {
        String url = baseUrl + "terminal";
        System.out.println(url);
        String iccid = "8986061501000089113";

        SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
        MessageFactory messageFactory = MessageFactory.newInstance();
        XWSSProcessorFactory processorFactory = XWSSProcessorFactory.newInstance();

        SOAPConnection connection = connectionFactory.createConnection();

        SOAPMessage request = messageFactory.createMessage();
        request.getMimeHeaders().addHeader("SOAPAction", "http://api.jasperwireless.com/ws/service/terminal/GetTerminalDetails");
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
        Name terminalRequestName = envelope.createName("GetTerminalDetailsRequest", PREFIX, NAMESPACE_URI);
        SOAPBodyElement terminalRequestElement = request.getSOAPBody().addBodyElement(terminalRequestName);
        Name msgId = envelope.createName("messageId", PREFIX, NAMESPACE_URI);
        SOAPElement msgElement = terminalRequestElement.addChildElement(msgId);
        msgElement.setValue("TCE-100-ABC-34084");
        Name version = envelope.createName("version", PREFIX, NAMESPACE_URI);
        SOAPElement versionElement = terminalRequestElement.addChildElement(version);
        versionElement.setValue("1.0");
        Name license = envelope.createName("licenseKey", PREFIX, NAMESPACE_URI);
        SOAPElement licenseElement = terminalRequestElement.addChildElement(license);
        licenseElement.setValue(licenseKey);
        Name iccids = envelope.createName("iccids", PREFIX, NAMESPACE_URI);
        SOAPElement iccidsElement = terminalRequestElement.addChildElement(iccids);
        Name iccidName = envelope.createName("iccid", PREFIX, NAMESPACE_URI);
        SOAPElement iccidElement = iccidsElement.addChildElement(iccidName);
        iccidElement.setValue(iccid);

        CallbackHandler callbackHandler = new CallbackHandler() {
            public void handle(Callback[] callbacks) throws UnsupportedCallbackException {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof UsernameCallback) {
                        UsernameCallback callback = (UsernameCallback) callbacks[i];
                        callback.setUsername(username);
                    } else if (callbacks[i] instanceof PasswordCallback) {
                        PasswordCallback callback = (PasswordCallback) callbacks[i];
                        callback.setPassword(password);
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i]);
                    }
                }
            }
        };
        InputStream policyStream = null;
        XWSSProcessor processor = null;
        try {
            //policyStream = getClass().getResourceAsStream("securityPolicy.xml");
            String policyXML = "<xwss:SecurityConfiguration dumpMessages=\"false\" xmlns:xwss=\"http://java.sun.com/xml/ns/xwss/config\">";
            policyXML += "<xwss:UsernameToken digestPassword=\"false\" useNonce=\"false\"/>";
            policyXML += "</xwss:SecurityConfiguration>";
            policyStream = new ByteArrayInputStream(policyXML.getBytes());
            processor = processorFactory.createProcessorForSecurityConfiguration(policyStream, callbackHandler);
        }
        finally {
            if (policyStream != null) {
                policyStream.close();
            }
        }
        ProcessingContext context = processor.createProcessingContext(request);
        request = processor.secureOutboundMessage(context);
        System.out.println("Request: ");
        request.writeTo(System.out);
        System.out.println("");

        SOAPMessage response = connection.call(request, url);
        System.out.println("Response: ");
        response.writeTo(System.out);
        System.out.println("");


        if (!response.getSOAPBody().hasFault()) {
            /*SOAPEnvelope */envelope = response.getSOAPPart().getEnvelope();
            Name terminalResponseName = envelope.createName("GetTerminalDetailsResponse", PREFIX, NAMESPACE_URI);
            SOAPBodyElement terminalResponseElement = (SOAPBodyElement) response.getSOAPBody().getChildElements(terminalResponseName).next();
            String terminalValue = terminalResponseElement.getTextContent();
            Name terminals = envelope.createName("terminals", PREFIX, NAMESPACE_URI);
            Name terminal = envelope.createName("terminal", PREFIX, NAMESPACE_URI);
            SOAPBodyElement terminalsElement = (SOAPBodyElement) terminalResponseElement.getChildElements(terminals).next();
            SOAPBodyElement terminalElement = (SOAPBodyElement) terminalsElement.getChildElements(terminal).next();
            NodeList list = terminalElement.getChildNodes();
            Node n = null;
            for (int i = 0; i < list.getLength(); i ++) {
                n = list.item(i);
                if ("status".equalsIgnoreCase(n.getLocalName()))
                    break;
            }

            System.out.println("status of device = " + n.getTextContent());
            System.out.println("Terminal Response [" + terminalValue + "]");
        } else {
            SOAPFault fault = response.getSOAPBody().getFault();
            System.err.println("Received SOAP Fault");
            System.err.println("SOAP Fault Code :" + fault.getFaultCode());
            System.err.println("SOAP Fault String :" + fault.getFaultString());
        }
    }
}
