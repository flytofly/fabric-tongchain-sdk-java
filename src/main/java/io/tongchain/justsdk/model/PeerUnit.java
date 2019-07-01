package io.tongchain.justsdk.model;

/**
 * @Author:wangkeke
 * @Date: 2019/5/17 0017下午 5:43
 */
public class PeerUnit {

    private String peerName;
    private String peerDomainName;
    private String peerLocation;

    public String getPeerName() {
        return peerName;
    }

    public void setPeerName(String peerName) {
        this.peerName = peerName;
    }

    public String getPeerDomainName() {
        return peerDomainName;
    }

    public void setPeerDomainName(String peerDomainName) {
        this.peerDomainName = peerDomainName;
    }

    public String getPeerLocation() {
        return peerLocation;
    }

    public void setPeerLocation(String peerLocation) {
        this.peerLocation = peerLocation;
    }
}
