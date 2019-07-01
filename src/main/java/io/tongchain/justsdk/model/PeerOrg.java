package io.tongchain.justsdk.model;

import java.util.List;

/**
 * @Author:wangkeke
 * @Date: 2019/5/17 0017下午 5:04
 */
public class PeerOrg {

    private String orgName;
    private String orgMspId;
    private List<PeerUnit> peers;

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgMspId() {
        return orgMspId;
    }

    public void setOrgMspId(String orgMspId) {
        this.orgMspId = orgMspId;
    }

    public List<PeerUnit> getPeers() {
        return peers;
    }

    public void setPeers(List<PeerUnit> peers) {
        this.peers = peers;
    }
}
