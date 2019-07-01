package io.tongchain.justsdk.model;

import com.google.protobuf.InvalidProtocolBufferException;
import io.tongchain.justsdk.bean.TLBlockInfo;
import io.tongchain.justsdk.util.DateTools;
import io.tongchain.justsdk.util.StringUtils;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @Author:wangkeke
 * @Date: 2019/5/23 0023下午 2:01
 */
public class JustBlockInfo {

    private static final Logger logger = LoggerFactory.getLogger(JustBlockInfo.class);

    public static Map<String,Object> getResultJson(JSONArray channelBlockInfos, int peerCount){
        if(StringUtils.isNotNull(channelBlockInfos)){
            Map<String,Object> map = new HashMap<>();
            List<TLBlockInfo> detailList = new ArrayList<>();
            int blockHeight=0;
            int txTotalCount=0;
            for(int i=0;i<channelBlockInfos.size();i++){
                JSONObject jsonObject = channelBlockInfos.getJSONObject(i);
                if(i==0){
                    blockHeight = jsonObject.getInt("blockHeight")+1;
                }
                if(jsonObject.getInt("txCount") == 0){
                    txTotalCount  = txTotalCount + 1;
                }else{
                    txTotalCount += jsonObject.getInt("txCount");
                }
                TLBlockInfo tlBlockInfo = new TLBlockInfo();
                tlBlockInfo.setBlockHeight(jsonObject.getInt("blockHeight"));
                tlBlockInfo.setTxCount(jsonObject.getInt("txCount"));
                JSONArray txDetail = jsonObject.getJSONArray("txDetail");
                for(int j=0;j<txDetail.size();j++){
                    JSONObject detailJSONObject = txDetail.getJSONObject(j);
                    String transactionID = detailJSONObject.getString("transactionID");
                    tlBlockInfo.setTxHash(transactionID);
                    tlBlockInfo.setDiffTime(transform(detailJSONObject.getString("timestamp")));
                    tlBlockInfo.setTime(detailJSONObject.getString("timestamp"));
                }
                detailList.add(tlBlockInfo);
            }
            map.put("blockHeight",blockHeight);
            map.put("txCount",txTotalCount);
            map.put("peerCount",peerCount);
            map.put("detailData",detailList);
            return map;
        }
        return null;
    }

    private static String transform(String historyDate){
        historyDate = historyDate.replaceAll("/","-");
        long second = DateTools.diffDateInSeconds(historyDate,DateTools.getNowTime());
        long day =  second / (24 * 60 * 60);
        long remainDay = second % (24 * 60 * 60);
        long hour=0;
        long min=0;
        String res = "";
        if(day > 0 ){
            res +=day+"天";
        }
        hour =  (remainDay)/(60*60);
        long remainHour = (remainDay) % (60 * 60);
        min = (remainHour)/60;
        if(hour > 0){
            res +=hour+"小时";
        }
        res +=min+"分钟前";
        return res;
    }

    public JSONArray queryPeerBlock(BlockchainInfo blockchainInfo, Channel channel,int peerCount) throws ProposalException, InvalidArgumentException, UnsupportedEncodingException, InvalidProtocolBufferException {
        if(StringUtils.isNotNull(blockchainInfo)){
            JSONArray channelBlockInfos = new JSONArray();
            byte[] currentBlockHash = blockchainInfo.getCurrentBlockHash();
            byte[] previousBlockHash = null;
            do {
                previousBlockHash = currentBlockHash;
                BlockInfo preBlockInfo = channel.queryBlockByHash(previousBlockHash);
                long blockNumber = preBlockInfo.getBlockNumber();
                JSONObject preJsonObject = parseBlockInfo(preBlockInfo, Hex.encodeHexString(previousBlockHash));
                JSONArray preJsonArray = parseEnvelopeInfo(preBlockInfo);
                preJsonObject.put("txDetail",preJsonArray);
                channelBlockInfos.add(preJsonObject);
                currentBlockHash = preBlockInfo.getPreviousHash();
            }while(StringUtils.isNotNull(currentBlockHash) && currentBlockHash.length > 0 );
            return channelBlockInfos;
        }
        return null;
    }

    public Set<Peer> peerCount(HFClient client,String channelName,JustProperty conf,List<OrdererUnit> orgList,List<PeerOrg> peerOrgsList) throws ProposalException, InvalidArgumentException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchMethodException, CryptoException, ClassNotFoundException, IOException {
        Set<Peer> channelPeerCount = new HashSet<>();
        for(int i=0;i<orgList.size();i++){
            String ordererName = orgList.get(i).getOrdererName();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            if(orgList.size() == peerOrgsList.size()){
                List<PeerUnit> peers = peerOrgsList.get(i).getPeers();
                channelPeerCount.addAll(judgeChannel(peers,conf,client,channelName));
            }else{
                for(int j=0;j<peerOrgsList.size();j++){
                    List<PeerUnit> peers = peerOrgsList.get(j).getPeers();
                    channelPeerCount.addAll(judgeChannel(peers,conf,client,channelName));
                }
            }
        }
        return channelPeerCount;
    }

    private Set<Peer> judgeChannel(List<PeerUnit> peers,JustProperty conf,HFClient client,String channelName) throws InvalidArgumentException {
        Set<Peer> channelPeerCount = new HashSet<>();
        for (PeerUnit peerUnit : peers) {
            Peer peer = client.newPeer(peerUnit.getPeerName(), peerUnit.getPeerLocation(), conf.getPeerProperties(peerUnit.getPeerName()));
            Set<String> channels = null;
            try{
                channels = client.queryChannels(peer);
                for(String chan : channels){
                    if(channelName.equals(chan)){
                        channelPeerCount.add(peer);
                    }
                }
            }catch (Exception e){
                logger.error(String.format("peer:%s has not yet joined the channel:%s",peer.getName(),channelName));
            }
        }
        return channelPeerCount;
    }

    private JSONObject parseBlockInfo(org.hyperledger.fabric.sdk.BlockInfo blockInfo, String blockHash){
        JSONObject blockObj = new JSONObject();
        blockObj.put("blockHash",blockHash);
        //每个块的交易数
        int transactionCount = blockInfo.getTransactionCount();
        blockObj.put("txCount",transactionCount);
        //当前块大小
        long blockNumber = blockInfo.getBlockNumber();
        blockObj.put("blockHeight",blockNumber);
        byte[] dataHash = blockInfo.getDataHash();
        blockObj.put("dataHash",Hex.encodeHexString(dataHash));
        byte[] previousHash = blockInfo.getPreviousHash();
        blockObj.put("previousHash",Hex.encodeHexString(previousHash));
        return blockObj;
    }

    private JSONArray parseEnvelopeInfo(org.hyperledger.fabric.sdk.BlockInfo blockInfo) throws UnsupportedEncodingException, InvalidProtocolBufferException {
        JSONArray envJsonArray = new JSONArray();
        for(org.hyperledger.fabric.sdk.BlockInfo.EnvelopeInfo info : blockInfo.getEnvelopeInfos()){
            JSONObject json = new JSONObject();

            json.put("channelId", info.getChannelId());
            json.put("transactionID", info.getTransactionID());
            json.put("validationCode", info.getValidationCode());
            json.put("timestamp", DateTools.parseDateFormat(new Date(info.getTimestamp().getTime())));
            json.put("type", info.getType());
            json.put("createMSPID", info.getCreator().getMspid());
            json.put("isValid", info.isValid());


            if (info.getType() == org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {
                org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo txeInfo = (org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo) info;
                JSONObject transactionEnvelopeInfoJson = new JSONObject();
                int txCount = txeInfo.getTransactionActionInfoCount();
                transactionEnvelopeInfoJson.put("txCount", txCount);
                transactionEnvelopeInfoJson.put("isValid", txeInfo.isValid());
                transactionEnvelopeInfoJson.put("validationCode", txeInfo.getValidationCode());
                transactionEnvelopeInfoJson.put("transactionActionInfoArray", getTransactionActionInfoJsonArray(txeInfo, txCount));
                json.put("transactionEnvelopeInfo", transactionEnvelopeInfoJson);
            }
            envJsonArray.add(json);
        }
        return envJsonArray;
    }

    private JSONArray getTransactionActionInfoJsonArray(org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo txeInfo, int txCount) throws UnsupportedEncodingException, InvalidProtocolBufferException {
        JSONArray transactionActionInfoJsonArray = new JSONArray();
        for (int i = 0; i < txCount; i++) {
            org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo txInfo = txeInfo.getTransactionActionInfo(i);
            int endorsementsCount = txInfo.getEndorsementsCount();
            int chaincodeInputArgsCount = txInfo.getChaincodeInputArgsCount();
            JSONObject transactionActionInfoJson = new JSONObject();
            transactionActionInfoJson.put("responseStatus", txInfo.getResponseStatus());
            transactionActionInfoJson.put("responseMessageString", printableString(new String(txInfo.getResponseMessageBytes(), "UTF-8")));
            transactionActionInfoJson.put("endorsementsCount", endorsementsCount);
            transactionActionInfoJson.put("chaincodeInputArgsCount", chaincodeInputArgsCount);
            transactionActionInfoJson.put("status", txInfo.getProposalResponseStatus());
            transactionActionInfoJson.put("payload", printableString(new String(txInfo.getProposalResponsePayload(), "UTF-8")));

            transactionActionInfoJson.put("endorserInfoArray", getEndorserInfoJsonArray(txInfo, endorsementsCount));


            TxReadWriteSetInfo rwsetInfo = txInfo.getTxReadWriteSet();
            JSONObject rwsetInfoJson = new JSONObject();
            if (null != rwsetInfo) {
                int nsRWsetCount = rwsetInfo.getNsRwsetCount();
                rwsetInfoJson.put("nsRWsetCount", nsRWsetCount);
                rwsetInfoJson.put("nsRwsetInfoArray", getNsRwsetInfoJsonArray(rwsetInfo));
            }
            transactionActionInfoJson.put("rwsetInfo", rwsetInfoJson);
            transactionActionInfoJsonArray.add(transactionActionInfoJson);
        }
        return transactionActionInfoJsonArray;
    }


    /** 解析背书信息 */
    private JSONArray getEndorserInfoJsonArray(org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo txInfo, int endorsementsCount) {
        JSONArray endorserInfoJsonArray = new JSONArray();
        for (int n = 0; n < endorsementsCount; ++n) {
            org.hyperledger.fabric.sdk.BlockInfo.EndorserInfo endorserInfo = txInfo.getEndorsementInfo(n);
            String signature = Hex.encodeHexString(endorserInfo.getSignature());
            String id = endorserInfo.getId();
            String mspId = endorserInfo.getMspid();
            JSONObject endorserInfoJson = new JSONObject();
            endorserInfoJson.put("signature", signature);
            endorserInfoJson.put("id", id);
            endorserInfoJson.put("mspId", mspId);

            endorserInfoJsonArray.add(endorserInfoJson);
        }
        return endorserInfoJsonArray;
    }

    /** 解析读写集集合 */
    private JSONArray getNsRwsetInfoJsonArray(TxReadWriteSetInfo rwsetInfo) throws InvalidProtocolBufferException, UnsupportedEncodingException {
        JSONArray nsRwsetInfoJsonArray = new JSONArray();
        for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : rwsetInfo.getNsRwsetInfos()) {
            final String namespace = nsRwsetInfo.getNamespace();
            KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();
            JSONObject nsRwsetInfoJson = new JSONObject();

            nsRwsetInfoJson.put("readSet", getReadSetJSONArray(rws, namespace));
            nsRwsetInfoJson.put("writeSet", getWriteSetJSONArray(rws, namespace));
            nsRwsetInfoJsonArray.add(nsRwsetInfoJson);
        }
        return nsRwsetInfoJsonArray;
    }

    /** 解析读集 */
    private JSONArray getReadSetJSONArray(KvRwset.KVRWSet rws, String namespace) {
        JSONArray readJsonArray = new JSONArray();
        int rs = -1;
        for (KvRwset.KVRead readList : rws.getReadsList()) {
            rs++;
            String key = readList.getKey();
            long readVersionBlockNum = readList.getVersion().getBlockNum();
            long readVersionTxNum = readList.getVersion().getTxNum();
            JSONObject readInfoJson = new JSONObject();
            readInfoJson.put("namespace", namespace);
            readInfoJson.put("readSetIndex", rs);
            readInfoJson.put("key", key);
            readInfoJson.put("readVersionBlockNum", readVersionBlockNum);
            readInfoJson.put("readVersionTxNum", readVersionTxNum);
            readInfoJson.put("chaincode_version", String.format("[%s : %s]", readVersionBlockNum, readVersionTxNum));
            readJsonArray.add(readInfoJson);
        }
        return readJsonArray;
    }

    /** 解析写集 */
    private JSONArray getWriteSetJSONArray(KvRwset.KVRWSet rws, String namespace) throws UnsupportedEncodingException {
        JSONArray writeJsonArray = new JSONArray();
        int rs = -1;
        for (KvRwset.KVWrite writeList : rws.getWritesList()) {
            rs++;
            String key = writeList.getKey();
            String writeContent = new String(writeList.getValue().toByteArray(), "UTF-8");
            String valAsString = printableString(new String(writeList.getValue().toByteArray(), "UTF-8"));
            JSONObject writeInfoJson = new JSONObject();
            writeInfoJson.put("namespace", namespace);
            writeInfoJson.put("writeSetIndex", rs);
            writeInfoJson.put("key", key);
            writeInfoJson.put("value", writeContent);
            writeJsonArray.add(writeInfoJson);
        }
        return writeJsonArray;
    }

    private String printableString(final String string) {
        int maxLogStringLength = 64;
        if (string == null || string.length() == 0) {
            return string;
        }
        String ret = string.replaceAll("[^\\p{Print}]", "?");
        ret = ret.substring(0, Math.min(ret.length(), maxLogStringLength)) + (ret.length() > maxLogStringLength ? "..." : "");
        return ret;
    }
}
