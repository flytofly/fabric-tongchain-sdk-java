package io.tongchain.justsdk.model;

/**
 * @Author:wangkeke
 * @Date: 2019/6/4 0004下午 4:42
 */
public class ChaincodeInfo {

    private String chaincodeName;
    private String chaincodePeerName;
    private String chaincodeChannelName;
    private String chaincodeOrdererName;
    private String chaincodePath;
    private String chaincodeVersion;

    public String getChaincodeName() {
        return chaincodeName;
    }

    public void setChaincodeName(String chaincodeName) {
        this.chaincodeName = chaincodeName;
    }

    public String getChaincodePeerName() {
        return chaincodePeerName;
    }

    public void setChaincodePeerName(String chaincodePeerName) {
        this.chaincodePeerName = chaincodePeerName;
    }

    public String getChaincodeChannelName() {
        return chaincodeChannelName;
    }

    public void setChaincodeChannelName(String chaincodeChannelName) {
        this.chaincodeChannelName = chaincodeChannelName;
    }

    public String getChaincodeOrdererName() {
        return chaincodeOrdererName;
    }

    public void setChaincodeOrdererName(String chaincodeOrdererName) {
        this.chaincodeOrdererName = chaincodeOrdererName;
    }

    public String getChaincodePath() {
        return chaincodePath;
    }

    public void setChaincodePath(String chaincodePath) {
        this.chaincodePath = chaincodePath;
    }

    public String getChaincodeVersion() {
        return chaincodeVersion;
    }

    public void setChaincodeVersion(String chaincodeVersion) {
        this.chaincodeVersion = chaincodeVersion;
    }
}
