package com.jasperwireless.service;

import java.util.List;
import java.util.Map;

/**
 * 中国联通物联网卡Jasper平台接口
 *
 * @author Aaric, created on 2018-01-13T22:48.
 * @since 1.0-SNAPSHOT
 */
public interface JasperService {

    /**
     * 获得回话信息
     *
     * @param iccid 物联网卡ICCID
     * @return
     * @throws Exception
     */
    List<Map<String, String>> getSessionInfo(String iccid) throws Exception;

    /**
     * 获得物联网卡详情
     *
     * @param iccid 物联网卡ICCID
     * @return
     */
    Map<String, String> getDetailByICCID(String iccid) throws Exception;
}
