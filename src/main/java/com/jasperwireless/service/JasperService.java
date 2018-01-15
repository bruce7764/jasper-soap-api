package com.jasperwireless.service;

import java.util.Date;
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
     * 获得指定日期之后的设备列表
     *
     * @param sinceDate  指定日期
     * @param pageNumber 查询页码
     * @param pageSize   分页大小
     * @return
     * @throws Exception
     */
    Map<String, Object> queryTerminalList(Date sinceDate, Integer pageNumber, Integer pageSize) throws Exception;

    /**
     * 获得物联网卡详情
     *
     * @param iccid 物联网卡ICCID
     * @return
     */
    Map<String, String> getDetailByICCID(String iccid) throws Exception;
}
