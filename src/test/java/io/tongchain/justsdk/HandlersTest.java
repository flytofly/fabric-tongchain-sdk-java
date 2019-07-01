package io.tongchain.justsdk;

import io.tongchain.justsdk.api.HandlerApi;
import io.tongchain.justsdk.model.ChaincodeInfo;
import io.tongchain.justsdk.model.JustBlockInfo;
import io.tongchain.justsdk.model.JustProperty;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

/**
 * @Author:wangkeke
 * @Date: 2019/5/20 0020下午 2:48
 */
public class HandlersTest {

    private static Handlers handle = null;
    @BeforeClass
    public static void readyJob() throws IOException, IllegalAccessException, InvalidArgumentException, InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, CryptoException {
        String fixture = HandlersTest.class.getClassLoader().getResource("fixture").getFile();
        String confPath = fixture.substring(0,fixture.lastIndexOf("/"));
        confPath = confPath.startsWith("/")?confPath.substring(1,confPath.length()):confPath;
        System.out.println(confPath);
        handle = new Handlers(confPath);
    }

    @Test
    public void TestCreateChannel() throws Exception {
        String channelName = "schoolchannel";
        String ordererName = "tongchain.tcorderer";
        handle.createChannel(channelName,ordererName);
    }

    @Test
    public void TestJoinChannel() throws Exception {
        String[] peerNameArr = {"peer0.tongchain","peer0.qifubao"};
        String channelName = "schoolchannel";
        String[] ordererName = {"tongchain.tcorderer","qifubao.tcorderer"};
        for(int i=0;i<peerNameArr.length;i++){
            handle.peerJoinChannel(ordererName[i],peerNameArr[i],channelName);
        }
    }

    @Test
    public void TestInstallCc() throws Exception {
        String orderername = "tongchain.tcorderer";
        String peerName = "peer0.tongchain";
        String version = "1.2";
        String chaincodeName = "fishdemo";
        String chaincodeType = "go";
        String chaincodePath = "chaincodedev/chaincode";
        String callback = handle.installChaincode(orderername,peerName, chaincodeName,chaincodeType, version, chaincodePath);
        System.out.println(callback);
    }


    @Test
    public void TestInstantiateCc() throws Exception {
        String channelName = "schoolchannel";
        String ordererName = "tongchain.tcorderer";
        String peerName = "peer0.tongchain";
        String version = "1.0";
        String chaincodeName = "fishdemo";
        String chaincodePath = "chaincodedev/chaincode";
        String[] args = {"a","888","b","999"};
        String fixture = HandlersTest.class.getClassLoader().getResource("fixture").getFile();
        fixture = fixture.startsWith("/")?fixture.substring(1):fixture;
        String endorsePath = fixture+"/conf/chaincodeendorsementpolicy.yaml";
        Map<String, String> callback = handle.instantieteChaincode(channelName, ordererName, peerName, chaincodeName, chaincodePath, version, args, endorsePath);
        System.out.println(callback);
    }


    @Test
    public void TestUpgradeCc() throws Exception {
        String channelName = "fishchannel";
        String ordererName = "tongchain.tcorderer";
        String peerName = "peer0.tongchain";
        String version = "1.2";
        String chaincodeName = "fishdemo";
        String chaincodePath = "chaincodedev/chaincode";
        String[] args = {"a","137","b","186"};
        String fixture = HandlersTest.class.getClassLoader().getResource("fixture").getFile();
        fixture = fixture.startsWith("/")?fixture.substring(1):fixture;
        String endorsePath = fixture+"/conf/chaincodeendorsementpolicy.yaml";
        Map<String, String> callback = handle.upgradeChaincode(channelName, ordererName, peerName, chaincodeName, chaincodePath, version, args, endorsePath);
        System.out.println(callback);
    }


    @Test
    public void TestInvokeCc() throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        String chaincodeName = chaincodeInfo.getChaincodeName();
        String chaincodeFunction = "invoke";
        String[] chaincodeArgs = {"a","b","3"};
        Map<String, Object> invoke = handle.invoke(channelName, ordererName, peerName,chaincodeName, chaincodeFunction, chaincodeArgs);
        System.out.println(invoke);
    }


    @Test
    public void TestQueryCc() throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        String chaincodeName = chaincodeInfo.getChaincodeName();
        String chaincodeFunction = "query";
        String[] chaincodeArgs = {"b"};
        Map<String, Object> query = handle.query(channelName,ordererName, peerName, chaincodeName, chaincodeFunction, chaincodeArgs);
        System.out.println(query);
    }

    @Test
    public void TestQueryBlock() throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        Map<String, Object> query = handle.queryBlock(channelName,ordererName,peerName);
        System.out.println(query);
    }

    @Test
    public void TestQueryAllChannels() throws Exception {
        Set<String> channels = handle.queryAllChannels();
        System.out.println(channels);
    }


    @Test
    public void TestQueryApi() throws Exception {
        HandlerApi api = new HandlerApi();
        String chaincodeFunction = "query";
        String[] chaincodeArgs = {"b"};
        Map<String, Object> query = api.query(chaincodeFunction, chaincodeArgs);
        System.out.println(query);
    }

}
