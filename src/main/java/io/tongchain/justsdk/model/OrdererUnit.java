package io.tongchain.justsdk.model;

import java.util.Properties;

/**
 * @Author:wangkeke
 * @Date: 2019/5/17 0017下午 5:02
 */
public class OrdererUnit {

    private String domainName;
    private String ordererName;
    private String ordererLocation;

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getOrdererName() {
        return ordererName;
    }

    public void setOrdererName(String ordererName) {
        this.ordererName = ordererName;
    }

    public String getOrdererLocation() {
        return ordererLocation;
    }

    public void setOrdererLocation(String ordererLocation) {
        this.ordererLocation = ordererLocation;
    }


}

