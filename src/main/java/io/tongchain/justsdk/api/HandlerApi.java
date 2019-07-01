package io.tongchain.justsdk.api;

import io.tongchain.justsdk.Handlers;
import io.tongchain.justsdk.model.ChaincodeInfo;
import io.tongchain.justsdk.model.JustProperty;
import net.sf.json.JSONArray;

import java.io.IOException;
import java.util.Map;

/**
 * @Author:wangkeke
 * @Date: 2019/6/5 0005上午 10:00
 */
public class HandlerApi {

    private static Handlers handle = null;

    public HandlerApi() throws IOException {
        String fixture = HandlerApi.class.getClassLoader().getResource("fixture").getFile();
        String confPath = fixture.substring(0,fixture.lastIndexOf("/fixture"));
        String os = System.getProperty("os.name");
        if (os.startsWith("win") || os.startsWith("Win")) {
            confPath = confPath.startsWith("/")?confPath.substring(1,confPath.length()):confPath;
        }
        handle = new Handlers(confPath);
    }

    public  Map<String, Object> queryBlock(String fcn,String[] args) throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        Map<String, Object> map = handle.queryBlock(channelName, ordererName, peerName);
        String chaincodeName = chaincodeInfo.getChaincodeName();
        Map<String, Object> orderQuery = handle.query(channelName, ordererName, peerName, chaincodeName, fcn, args);
        if(orderQuery.get("state").equals(10000)){
            map.put("orderNum", Integer.parseInt(orderQuery.get("payload").toString()));
        }else{
            map.put("orderNum", 0);
        }
        return map;
    }

    public  Map<String, Object> blockInfo() throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        Map<String, Object> map = handle.queryBlock(channelName, ordererName, peerName);
        return map;
    }

    public  JSONArray baseBlockInfo() throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        return handle.queryBlockBaseInfo(channelName, ordererName, peerName);
    }



    public  Map<String, Object> query(String fcn,String[] args) throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        String chaincodeName = chaincodeInfo.getChaincodeName();
        return handle.query(channelName,ordererName, peerName, chaincodeName, fcn, args);
    }

    public  Map<String, Object> query(String peerName,String chaincodeName,String fcn,String[] args) throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        return handle.query(channelName,ordererName, peerName, chaincodeName, fcn, args);
    }


    public  Map<String, Object> invoke(String fcn, String[] args) throws Exception {
        JustProperty conf = handle.getConf();
        ChaincodeInfo chaincodeInfo = conf.getChaincodeInfo();
        String channelName = chaincodeInfo.getChaincodeChannelName();
        String ordererName = chaincodeInfo.getChaincodeOrdererName();
        String peerName = chaincodeInfo.getChaincodePeerName();
        String chaincodeName = chaincodeInfo.getChaincodeName();
        return handle.invoke(channelName, ordererName, peerName,chaincodeName, fcn, args);
    }


}
