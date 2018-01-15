package com.jasperwireless.service;

import com.google.gson.Gson;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * JasperServiceTest
 *
 * @author Aaric, created on 2018-01-14T20:30.
 * @since 1.0-SNAPSHOT
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class JasperServiceTest {

    @Autowired
    protected JasperService jasperService;

    @Test
    public void testGetSessionInfo() throws Exception {
        System.out.println(new Gson().toJson(jasperService.getSessionInfo("8986061501000089136")));
    }

    @Test
    public void testQueryTerminalList() throws Exception {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println(new Gson().toJson(jasperService.queryTerminalList(dateFormat.parse("2015-05-15 00:00:00"), 1)));
    }

    @Test
    public void testGetDetailByICCID() throws Exception {
        System.out.println(new Gson().toJson(jasperService.getDetailByICCID("8986061501000089136")));
    }
}
