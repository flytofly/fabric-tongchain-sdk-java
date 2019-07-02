package io.tongchain.justsdk.model;

import io.tongchain.justsdk.util.Constant;
import io.tongchain.justsdk.util.StringUtils;
import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.*;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

/**
 * @Author:wangkeke
 * @Date: 2019/5/17 0017下午 5:00
 */
public class JustProperty {

    private boolean isTls;
    private String configPath;
    private String joinedPeer;
    private String profiles;
    private ChaincodeInfo chaincodeInfo = new ChaincodeInfo();
    private List<OrdererUnit> orgList = new ArrayList<>();
    private List<PeerOrg> peerOrgsList = new ArrayList<>();

    public JustProperty(String configPath) throws IOException {
        this.configPath = configPath;
        String path = configPath+ "/justsdk.properties";
        Properties properties = new Properties();
        properties.load(new FileInputStream(path));
        isTls = properties.getProperty(Constant.ISTLS).equals("true")? true : false;
        profiles = properties.getProperty(Constant.PROFILES) != null ? properties.getProperty(Constant.PROFILES) : null;
        chaincodeInfo.setChaincodeName(properties.getProperty(Constant.CHAINCODENAME) != null ? properties.getProperty(Constant.CHAINCODENAME) : null);
        chaincodeInfo.setChaincodePeerName(properties.getProperty(Constant.CHAINCODEPEERNAME) != null ? properties.getProperty(Constant.CHAINCODEPEERNAME) : null);
        chaincodeInfo.setChaincodeChannelName(properties.getProperty(Constant.CHAINCODECHANNELNAME) != null ? properties.getProperty(Constant.CHAINCODECHANNELNAME) : null);
        chaincodeInfo.setChaincodeOrdererName(properties.getProperty(Constant.CHAINCODEORDERERNAME) != null ? properties.getProperty(Constant.CHAINCODEORDERERNAME) : null);
        chaincodeInfo.setChaincodePath(properties.getProperty(Constant.CHAINCODEPATH) != null ? properties.getProperty(Constant.CHAINCODEPATH) : null);
        chaincodeInfo.setChaincodeVersion(properties.getProperty(Constant.CHAINCODEVERSION) != null ? properties.getProperty(Constant.CHAINCODEVERSION) : null);
        joinedPeer = properties.getProperty(Constant.JOINEDPEER) != null ? properties.getProperty(Constant.JOINEDPEER) : null;

        String orderers = properties.getProperty(Constant.ORDERERS) != null ? properties.getProperty(Constant.ORDERERS) : null;
        if (StringUtils.isNotNull(orderers)) {
            String[] ordererSplit = orderers.split(",");
            for (String s : ordererSplit) {
                String property = properties.getProperty(s);
                OrdererUnit orderer = parseOrderer(property);
                orgList.add(orderer);
            }
        }
        String orgs = properties.getProperty(Constant.ORG) != null ? properties.getProperty(Constant.ORG) : null;
        if(StringUtils.isNotNull(orgs)){
            String[] orgList = orgs.split(",");
            for(String s:orgList){
                String property = properties.getProperty(s);
                String[] peerSplit = property.split(",");
                PeerOrg peerOrg = parseOrg(properties.getProperty(s+peerSplit[0]));
                List<PeerUnit> peerList = new ArrayList<>();
                for(String ps:peerSplit){
                    String key = s+ps;
                    String property1 = properties.getProperty(key);
                    PeerUnit peer = parsePeer(property1);
                    peerList.add(peer);
                }
                peerOrg.setPeers(peerList);
                peerOrgsList.add(peerOrg);
            }
        }
    }

    public String getJoinedPeer(){
        return joinedPeer;
    }

    public boolean getTls(){
        return isTls;
    }

    public ChaincodeInfo getChaincodeInfo(){
        return chaincodeInfo;
    }

    public String getProfiles(){
        return profiles;
    }

    public List<OrdererUnit> getOrgList(){
        return orgList;
    }

    public List<PeerOrg> getPeerOrgsList(){
        return peerOrgsList;
    }

    private OrdererUnit parseOrderer(String str) {
        if(StringUtils.isNotNull(str)){
            OrdererUnit orderer = new OrdererUnit();
            String[] split = str.split(",");
            for(String s:split){
                String[] kv = s.split("=");
                if("domainname".equals(kv[0])){
                    orderer.setDomainName(kv[1]);
                }
                if("orderername".equals(kv[0])){
                    orderer.setOrdererName(kv[1]);
                }
                if("ordererlocation".equals(kv[0])){
                    if(getTls()){
                        kv[1] = kv[1].replaceFirst("^grpc://", "grpcs://");
                    }
                    orderer.setOrdererLocation(kv[1]);

                }
            }
            return orderer;
        }
        return null;
    }

    public Properties getOrdererProperties(String name){
        return getEndpointProperties("orderer",name);
    }

    public Properties getPeerProperties(String name) {
        return getEndpointProperties("peer", name);
    }

    public JustUser enrollUser(String ordererName) throws IOException {
        String path = configPath +"/fixture/conf/";
        if(StringUtils.isNotNull(getPeerOrgsList())){
            JustUser user = new JustUser();
            String domainName = null;
            if(StringUtils.isNotNull(ordererName)){
                domainName = getName(ordererName);
            }else{
                domainName = getPeerOrgsList().get(0).getOrgName();
            }
            user.setMspid(domainName+"MSP");
            user.setName(domainName+"Admin");
            File skFile = Paths.get(path, "crypto-config/peerOrganizations",
                    domainName,String.format("users/Admin@%s/msp/keystore", domainName)).toFile();
            File certificateFile = Paths.get(path, "crypto-config/peerOrganizations",
                    domainName,String.format("users/Admin@%s/msp/signcerts/Admin@%s-cert.pem",domainName, domainName)).toFile();
            File privateKeyFile = findFileSk(skFile);
            String certificate = new String(IOUtils.toByteArray(new FileInputStream(certificateFile)), "UTF-8");
            PrivateKey privateKey = getPrivateKeyFromBytes(IOUtils.toByteArray(new FileInputStream(privateKeyFile)));
            JustEnrollment justEnrollment = new JustEnrollment(privateKey, certificate);
            user.setEnrollment(justEnrollment);
            return user;
        }
        return null;
    }

    private static File findFileSk(File directory) {
        File[] matches = directory.listFiles((dir, name) -> name.endsWith("_sk"));
        if (null == matches) {
            throw new RuntimeException(format("Matches returned null does {} directory exist?", directory.getAbsoluteFile().getName()));
        }
        if (matches.length != 1) {
            throw new RuntimeException(format("Expected in {} only 1 sk file but found {}", directory.getAbsoluteFile().getName(), matches.length));
        }
        return matches[0];
    }

    private PrivateKey getPrivateKeyFromBytes(byte[] data) throws IOException {
        final Reader pemReader = new StringReader(new String(data));
        final PrivateKeyInfo pemPair;
        try (PEMParser pemParser = new PEMParser(pemReader)) {
            pemPair = (PrivateKeyInfo) pemParser.readObject();
        }
        JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
        PrivateKey privateKey = jcaPEMKeyConverter.getPrivateKey(pemPair);
        return privateKey;
    }

    private Properties getEndpointProperties(final String type,String name){
        String path = configPath +"/fixture/conf/";
        final String domainName = getDomainName(name);
        File cert = Paths.get(path, "crypto-config/ordererOrganizations".replace("orderer", type),
                domainName,  type + "s", name, "tls/server.crt").toFile();

        File clientCert = Paths.get(path, "crypto-config/ordererOrganizations".replace("orderer", type),
                domainName,  "users/Admin@"+domainName, "tls/client.crt").toFile();
        File clientKey = Paths.get(path, "crypto-config/ordererOrganizations".replace("orderer", type),
                domainName,  "users/Admin@"+domainName, "tls/client.key").toFile();
        if (!cert.exists()) {
            throw new RuntimeException(String.format("Missing cert file for: %s. Could not find at location: %s", name,
                    cert.getAbsolutePath()));
        }
        Properties prop = new Properties();
        prop.setProperty("pemFile", cert.getAbsolutePath());
        if(getTls()){
            prop.setProperty("clientKeyFile", clientKey.getAbsolutePath());
            prop.setProperty("clientCertFile", clientCert.getAbsolutePath());
        }
        prop.setProperty("hostnameOverride",name);
        prop.setProperty("sslProvider", "openSSL");
        prop.setProperty("negotiationType", "TLS");
        if("orderer".equals(type)){
            prop.setProperty("ordererWaitTimeMilliSecs", "300000");
            prop.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] {5L, TimeUnit.MINUTES});
            prop.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] {8L, TimeUnit.SECONDS});
            prop.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[] {true});
        }
        prop.put("grpc.ManagedChannelBuilderOption.maxInboundMessageSize", 9000000);
        return prop;
    }

    private String getName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(0,dot);
        }
    }

    private String getDomainName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(dot + 1);
        }
    }


    private PeerOrg parseOrg(String str) {
        if(StringUtils.isNotNull(str)){
            PeerOrg peerOrg = new PeerOrg();
            String[] split = str.split(",");
            for(String s:split){
                String[] kv = s.split("=");
                if("orgname".equals(kv[0])){
                    peerOrg.setOrgName(kv[1]);
                }
                if("mspid".equals(kv[0])){
                    peerOrg.setOrgMspId(kv[1]);
                }
            }
            return peerOrg;
        }
        return null;
    }

    private PeerUnit parsePeer(String str) {
        if(StringUtils.isNotNull(str)){
            PeerUnit peer = new PeerUnit();
            String[] split = str.split(",");
            for(String s:split){
                String[] kv = s.split("=");
                if("peerdomainname".equals(kv[0])){
                    peer.setPeerDomainName(kv[1]);
                }
                if("peername".equals(kv[0])){
                    peer.setPeerName(kv[1]);
                }
                if("peerlocation".equals(kv[0])){
                    if(getTls()){
                        kv[1] = kv[1].replaceFirst("^grpc://", "grpcs://");
                    }
                    peer.setPeerLocation(kv[1]);
                }
            }
            return peer;
        }
        return null;
    }
}
