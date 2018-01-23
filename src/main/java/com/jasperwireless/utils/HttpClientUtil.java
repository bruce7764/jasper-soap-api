package com.jasperwireless.utils;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * HttpClient Tools
 *
 * @author Aaric, created on 2018-01-18T14:49.
 * @since 1.0-SNAPSHOT
 */
public class HttpClientUtil {

    /**
     * Singleton Http Client
     */
    private static CloseableHttpClient singletonHttpClient;

    /**
     * Get default http client
     *
     * @return
     */
    public static CloseableHttpClient getDefaultHttpClient() {
        if (null == singletonHttpClient) {
            singletonHttpClient = HttpClients.createDefault();
        }
        return singletonHttpClient;
    }

    /**
     * doGet
     *
     * @param url
     * @return
     * @throws IOException
     */
    public static String doGet(String url) throws IOException {
        HttpGet httpGet = new HttpGet(url);

        CloseableHttpResponse httpResponse = getDefaultHttpClient().execute(httpGet);
        HttpEntity httpEntity = httpResponse.getEntity();
        String result = EntityUtils.toString(httpEntity);
        httpResponse.close();
        return result;
    }

    /**
     * doPost
     *
     * @param url
     * @param params
     * @return
     */
    public static String doPost(String url, Map<String, Object> params) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> pairList = new ArrayList<>();
        pairList.add(new BasicNameValuePair("username", "zhangsan"));
        pairList.add(new BasicNameValuePair("password", "123456"));
        httpPost.setEntity(new UrlEncodedFormEntity(pairList));

        CloseableHttpResponse httpResponse = getDefaultHttpClient().execute(httpPost);
        HttpEntity httpEntity = httpResponse.getEntity();
        String result = EntityUtils.toString(httpEntity);
        httpResponse.close();
        return result;
    }
}
