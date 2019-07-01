package io.tongchain.justsdk.model;


import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;

import java.util.Set;


public class JustUser implements User{

    private static final long serialVersionUID = 5695080465408336815L;

    private String name;
    private String enrollSecret;
    private String mspid;
    private Set<String> roles;
    private String account;
    private String affiliation;
    private Enrollment enrollment;

    private String org;

    public JustUser(){}
    public JustUser(String name, String org){
        this.name = name;
        this.org = org;
    }
    public JustUser(String mspid, String name, String enrollSecret) {
        this.name = name;
        this.enrollSecret = enrollSecret;
        this.mspid = mspid;
    }



    public void setName(String name) {
        this.name = name;
    }
    public void setEnrollSecret(String enrollSecret) {
        this.enrollSecret = enrollSecret;
    }
    public String getMspid() {
        return mspid;
    }
    public void setMspid(String mspid) {
        this.mspid = mspid;
    }
    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
    public void setAccount(String account) {
        this.account = account;
    }
    public void setAffiliation(String affiliation) {
        this.affiliation = affiliation;
    }
    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }
    public String getEnrollSecret() {
        return enrollSecret;
    }
    @Override
    public String getName() {
        return name;
    }
    @Override
    public Set<String> getRoles() {
        return roles;
    }
    @Override
    public String getAccount() {
        return account;
    }
    @Override
    public String getAffiliation() {
        return affiliation;
    }
    @Override
    public Enrollment getEnrollment() {
        return enrollment;
    }
    public String getMspId() {
        return mspid;
    }
}