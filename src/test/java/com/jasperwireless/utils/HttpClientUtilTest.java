package com.jasperwireless.utils;

import org.junit.Test;

/**
 * HttpClientUtilTest
 *
 * @author Aaric, created on 2018-01-18T14:52.
 * @since 1.0-SNAPSHOT
 */
public class HttpClientUtilTest {

    @Test
    public void testDoGet() throws Exception {
        System.err.println(HttpClientUtil.doGet("http://www.baidu.com"));
    }

    @Test
    public void testDoPost() throws Exception {
        System.out.println(HttpClientUtil.doPost("http://www.curefun.com/cfgateway/login/loginByAccount", null));
    }
}
