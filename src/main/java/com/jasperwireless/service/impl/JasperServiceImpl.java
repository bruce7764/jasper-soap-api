package com.jasperwireless.service.impl;

import com.jasperwireless.service.JasperService;
import com.sun.xml.wss.ProcessingContext;
import com.sun.xml.wss.XWSSProcessor;
import com.sun.xml.wss.XWSSProcessorFactory;
import com.sun.xml.wss.XWSSecurityException;
import com.sun.xml.wss.impl.callback.PasswordCallback;
import com.sun.xml.wss.impl.callback.UsernameCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.xml.soap.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 中国联通物联网卡Jasper平台接口实现类
 *
 * @author Aaric, created on 2018-01-14T20:22.
 * @since 1.0-SNAPSHOT
 */
@Service
public class JasperServiceImpl implements JasperService {

    /**
     * Logger
     */
    private static final Logger logger = LoggerFactory.getLogger(JasperServiceImpl.class);

    /**
     * 常量信息
     */
    private static String NAMESPACE_URI = "http://api.jasperwireless.com/ws/schema";
    private static String PREFIX = "jws";

    /**
     * Jasper平台请求基地址
     */
    @Value("${jasper.baseUrl}")
    private String baseUrl;

    /**
     * Jasper平台授权key
     */
    @Value("${jasper.licenseKey}")
    private String licenseKey;

    /**
     * Jasper平台用户名
     */
    @Value("${jasper.username}")
    private String username;

    /**
     * Jasper平台密码
     */
    @Value("${jasper.password}")
    private String password;

    /**
     * 获得连接对象
     *
     * @return
     * @throws SOAPException
     */
    private SOAPConnection getConnection() throws SOAPException {
        SOAPConnectionFactory connectionFactory = SOAPConnectionFactory.newInstance();
        return connectionFactory.createConnection();
    }

    /**
     * 获得请求对象
     *
     * @param subPath    子路径
     * @param methodName 方法名称
     * @return
     * @throws SOAPException
     * @throws XWSSecurityException
     * @throws IOException
     */
    private SOAPMessage getRequest(String subPath, String methodName) throws SOAPException, XWSSecurityException, IOException {
        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPMessage request = messageFactory.createMessage();
        request.getMimeHeaders().addHeader("SOAPAction", String.format("http://api.jasperwireless.com/ws/service/%s/%s", subPath, methodName));

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
        XWSSProcessorFactory processorFactory = XWSSProcessorFactory.newInstance();
        try {
            String securityPolicyContent = "<xwss:SecurityConfiguration dumpMessages=\"false\" xmlns:xwss=\"http://java.sun.com/xml/ns/xwss/config\">";
            securityPolicyContent += "<xwss:UsernameToken digestPassword=\"false\" useNonce=\"false\"/>";
            securityPolicyContent += "</xwss:SecurityConfiguration>";
            policyStream = new ByteArrayInputStream(securityPolicyContent.getBytes());
            processor = processorFactory.createProcessorForSecurityConfiguration(policyStream, callbackHandler);
        } finally {
            if (policyStream != null) {
                policyStream.close();
            }
        }
        ProcessingContext context = processor.createProcessingContext(request);
        request = processor.secureOutboundMessage(context);
        System.out.println("Request: ");
        request.writeTo(System.out);
        System.out.println("");
        System.out.println("");

        return request;
    }

    /**
     * 获得请求Body对象
     *
     * @param request    请求对象
     * @param methodName 方法名称
     * @return
     */
    private SOAPBodyElement getRequestBody(SOAPMessage request, String methodName) throws SOAPException {
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();
        Name terminalRequestName = envelope.createName(String.format("%sRequest", methodName), PREFIX, NAMESPACE_URI);
        SOAPBodyElement bodyElement = request.getSOAPBody().addBodyElement(terminalRequestName);
        Name msgId = envelope.createName("messageId", PREFIX, NAMESPACE_URI);
        SOAPElement msgElement = bodyElement.addChildElement(msgId);
        msgElement.setValue("TCE-100-ABC-34084");
        Name version = envelope.createName("version", PREFIX, NAMESPACE_URI);
        SOAPElement versionElement = bodyElement.addChildElement(version);
        versionElement.setValue("1.0");
        Name license = envelope.createName("licenseKey", PREFIX, NAMESPACE_URI);
        SOAPElement licenseElement = bodyElement.addChildElement(license);
        licenseElement.setValue(licenseKey);
        return bodyElement;
    }

    /**
     * 获得响应Body对象
     *
     * @param response   相应对象
     * @param methodName 方法名称
     * @return
     * @throws SOAPException
     */
    public SOAPBodyElement getResponsBody(SOAPMessage response, String methodName) throws SOAPException {
        if (response.getSOAPBody().hasFault()) {
            SOAPFault fault = response.getSOAPBody().getFault();
            System.err.println("Received SOAP Fault");
            System.err.println("SOAP Fault Code :" + fault.getFaultCode());
            System.err.println("SOAP Fault String :" + fault.getFaultString());
            return null;
        }
        SOAPEnvelope envelope = response.getSOAPPart().getEnvelope();
        Name responseName = envelope.createName(String.format("%sResponse", methodName), PREFIX, NAMESPACE_URI);
        return (SOAPBodyElement) response.getSOAPBody().getChildElements(responseName).next();
    }

    @Override
    public List<Map<String, String>> getSessionInfo(String iccid) throws Exception {
        // Jasper平台soap api请求地址
        String methodName = "GetSessionInfo";
        String subPath = "terminal";
        String url = baseUrl + subPath;

        // 构建公共部分
        SOAPMessage request = getRequest(subPath, methodName);
        SOAPBodyElement requestBodyElement = getRequestBody(request, methodName);
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();

        // 追加私有部分
        // iccid
        Name iccidName = envelope.createName("iccid", PREFIX, NAMESPACE_URI);
        SOAPElement iccidElement = requestBodyElement.addChildElement(iccidName);
        iccidElement.setValue(iccid);

        // 请求数据
        SOAPConnection connection = getConnection();
        SOAPMessage response = connection.call(request, url);
        System.out.println("Response: ");
        response.writeTo(System.out);
        System.out.println("");
        System.out.println("");

        // 处理结果
        SOAPBodyElement responseBodyElement = getResponsBody(response, methodName);
        if (null == responseBodyElement) {
            return null;
        }
        logger.info("Terminal Response [{}]", responseBodyElement.getTextContent());

        // 搜寻数据节点
        Name sessionInfoName = envelope.createName("sessionInfo", PREFIX, NAMESPACE_URI);
        Name sessionName = envelope.createName("session", PREFIX, NAMESPACE_URI);
        Iterator itrParent = requestBodyElement.getChildElements(sessionInfoName);
        if (itrParent.hasNext()) {
            // 获得父节点信息
            SOAPBodyElement terminalsElement = (SOAPBodyElement) itrParent.next();

            // 封装数据并返回
            Map<String, String> itemMap;
            List<Map<String, String>> dataList = new ArrayList<>();
            Iterator itr = terminalsElement.getChildElements(sessionName);
            SOAPBodyElement terminalElement;
            NodeList list;
            Node node;
            while (itr.hasNext()) {
                terminalElement = (SOAPBodyElement) itr.next();
                list = terminalElement.getChildNodes();

                itemMap = new HashMap<>();
                for (int i = 0; i < list.getLength(); i++) {
                    node = list.item(i);
                    itemMap.put(node.getLocalName(), node.getTextContent());
                }
                dataList.add(itemMap);
            }
            return dataList;
        }

        return null;
    }

    @Override
    public Map<String, Object> queryTerminalList(Date sinceDate, Integer pageNumber, Integer pageSize) throws Exception {
        // 验证日期
        if (null == sinceDate) {
            throw new IllegalArgumentException("sinceDate can't be null");
        }

        // Jasper平台soap api请求地址
        String methodName = "GetModifiedTerminals";
        String subPath = "terminal";
        String url = baseUrl + subPath;

        // 构建公共部分
        SOAPMessage request = getRequest(subPath, methodName);
        SOAPBodyElement requestBodyElement = getRequestBody(request, methodName);
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();

        // 追加私有部分
        // since
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        Name sinceName = envelope.createName("since", PREFIX, NAMESPACE_URI);
        SOAPElement sinceElement = requestBodyElement.addChildElement(sinceName);
        sinceElement.setValue(dateFormat.format(sinceDate));
        // pageNumber
        /*if (null != pageNumber) {
            Name pageNumberName = envelope.createName("pageNumber", PREFIX, NAMESPACE_URI);
            SOAPElement pageNumberElement = requestBodyElement.addChildElement(pageNumberName);
            pageNumberElement.setValue(String.valueOf(pageNumber));
        }*/
        // pageSize
        /*if(null != pageSize) {
            Name pageSizeName = envelope.createName("pageSize", PREFIX, NAMESPACE_URI);
            SOAPElement pageSizeElement = requestBodyElement.addChildElement(pageSizeName);
            pageSizeElement.setValue(String.valueOf(pageSize));
        }*/

        // 请求数据
        SOAPConnection connection = getConnection();
        SOAPMessage response = connection.call(request, url);
        System.out.println("Response: ");
        response.writeTo(System.out);
        System.out.println("");
        System.out.println("");

        // 处理结果
        SOAPBodyElement responseBodyElement = getResponsBody(response, methodName);
        if (null == responseBodyElement) {
            return null;
        }
        logger.info("Terminal Response [{}]", responseBodyElement.getTextContent());

        // 搜寻数据节点
        Name iccidsName = envelope.createName("iccids", PREFIX, NAMESPACE_URI);
        Name totalPagesName = envelope.createName("totalPages", PREFIX, NAMESPACE_URI);
        SOAPBodyElement iccidsElement = (SOAPBodyElement) responseBodyElement.getChildElements(iccidsName).next();
        SOAPBodyElement totalPagesElement = (SOAPBodyElement) responseBodyElement.getChildElements(totalPagesName).next();

        // 封装数据并返回
        // list for iccids
        Node node;
        NodeList list = iccidsElement.getChildNodes();
        Map<String, Object> dataMap = new HashMap<>();
        List<String> iccidList = new ArrayList<>();
        for (int i = 0; i < list.getLength(); i++) {
            node = list.item(i);
            iccidList.add(node.getTextContent());
        }
        dataMap.put("list", iccidList);

        // total
        if(0 < iccidList.size()) {
            dataMap.put("total", Integer.valueOf(totalPagesElement.getTextContent()));
        }

        return dataMap;
    }

    @Override
    public Map<String, String> getDetailByICCID(String iccid) throws Exception {
        // Jasper平台soap api请求地址
        String methodName = "GetTerminalDetails";
        String subPath = "terminal";
        String url = baseUrl + subPath;

        // 构建公共部分
        SOAPMessage request = getRequest(subPath, methodName);
        SOAPBodyElement requestBodyElement = getRequestBody(request, methodName);
        SOAPEnvelope envelope = request.getSOAPPart().getEnvelope();

        // 追加私有部分
        // iccids
        Name iccidsName = envelope.createName("iccids", PREFIX, NAMESPACE_URI);
        SOAPElement iccidsElement = requestBodyElement.addChildElement(iccidsName);
        // iccids->iccid
        Name iccidName = envelope.createName("iccid", PREFIX, NAMESPACE_URI);
        SOAPElement iccidElement = iccidsElement.addChildElement(iccidName);
        iccidElement.setValue(iccid);

        // 请求数据
        SOAPConnection connection = getConnection();
        SOAPMessage response = connection.call(request, url);
        System.out.println("Response: ");
        response.writeTo(System.out);
        System.out.println("");
        System.out.println("");

        // 处理结果
        SOAPBodyElement responseBodyElement = getResponsBody(response, methodName);
        if (null == responseBodyElement) {
            return null;
        }
        logger.info("Terminal Response [{}]", responseBodyElement.getTextContent());

        // 搜寻数据节点
        Name terminalsName = envelope.createName("terminals", PREFIX, NAMESPACE_URI);
        Name terminalName = envelope.createName("terminal", PREFIX, NAMESPACE_URI);
        SOAPBodyElement terminalsElement = (SOAPBodyElement) responseBodyElement.getChildElements(terminalsName).next();
        SOAPBodyElement terminalElement = (SOAPBodyElement) terminalsElement.getChildElements(terminalName).next();
        NodeList list = terminalElement.getChildNodes();

        // 封装数据并返回
        Node node;
        Map<String, String> dataMap = new HashMap<>();
        for (int i = 0; i < list.getLength(); i++) {
            node = list.item(i);
            dataMap.put(node.getLocalName(), node.getTextContent());
        }

        return dataMap;
    }
}
