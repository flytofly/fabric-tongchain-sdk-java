package io.tongchain.justsdk.model;

import org.hyperledger.fabric.sdk.Enrollment;

import java.io.Serializable;
import java.security.PrivateKey;

public class JustEnrollment implements Enrollment, Serializable {

    /** 私钥 */
    private PrivateKey privateKey;
    /** 授权证书 */
    private String certificate;
    public JustEnrollment(PrivateKey privateKey, String certificate) {
        this.certificate = certificate;
        this.privateKey = privateKey;
    }

    @Override
    public PrivateKey getKey() {
        return privateKey;
    }
    @Override
    public String getCert() {
        return certificate;
    }
}
