package io.tongchain.justsdk;

import io.tongchain.justsdk.model.*;
import io.tongchain.justsdk.util.StringUtils;
import net.sf.json.JSONArray;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;

/**
 * @Author:wangkeke
 * @Date: 2019/5/20 0020上午 10:45
 */
public class Handlers {

    private static final Logger logger = LoggerFactory.getLogger(Handlers.class);

    private JustProperty conf;
    private String profiles;
    private List<OrdererUnit> orgList;
    private List<PeerOrg> peerOrgsList;
    private String configPath;
    private HFClient client = HFClient.createNewInstance();

    /**
     * path ../../resources
     * @param path
     * @throws IOException
     */
    public Handlers(String path) throws IOException {
        this.configPath = path;
        conf = new JustProperty(path);
        profiles = conf.getProfiles();
        orgList = conf.getOrgList();
        peerOrgsList = conf.getPeerOrgsList();
    }

    public JustProperty getConf(){
        return conf;
    }

    public String getProfiles(){
        return  profiles;
    }

    private String getDomainName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(dot + 1);
        }
    }

    private String getName(final String name) {
        int dot = name.indexOf(".");
        if (-1 == dot) {
            return null;
        } else {
            return name.substring(0,dot);
        }
    }

    public Channel reconstructChannel(String channelName,String ordererName,String peerName) throws Exception {
        Channel channel = null;
        logger.info(format("Running reconstruct channel:%s",channelName));
        try{
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            Channel tempChannel = client.getChannel(channelName);
            if(StringUtils.isNotNull(tempChannel)){
                channel = tempChannel;
            }else{
                channel = client.newChannel(channelName);
            }
            String domainName = getName(ordererName);
            for(OrdererUnit orderer : orgList){
                if(orderer.getOrdererName().contains(domainName)){
                    channel.addOrderer(client.newOrderer(orderer.getOrdererName(),orderer.getOrdererLocation(),conf.getOrdererProperties(orderer.getOrdererName())));
                }
            }
            for(PeerOrg peerOrg : peerOrgsList){
                List<PeerUnit> peers = peerOrg.getPeers();
                for(PeerUnit peerUnit : peers){
                    if(peerName.equals(peerUnit.getPeerName())){
                        Peer peer = client.newPeer(peerUnit.getPeerName(), peerUnit.getPeerLocation(), conf.getPeerProperties(peerUnit.getPeerName()));
                        Set<String> channels = null;
                        try{
                            channels = client.queryChannels(peer);
                        }catch (Exception e){
                            logger.error(e.getMessage());
                        }
                        if(StringUtils.isNotNull(channels)){
                            for(String str : channels){
                                if(str.equals(channelName)){
                                    channel.addPeer(peer);
                                    if(!channel.isInitialized()){
                                        channel.initialize();
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return channel;
        }catch (Exception e){
            String errMsg = "Handlers | recontructChannel "+e;
            logger.error(errMsg);
        }
        return null;
    }

    public Map<String,Object> queryBlock(String channelName,String ordererName,String peerName) throws Exception {
        try{
            Channel channel = reconstructChannel(channelName,ordererName,peerName);
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();
            JustBlockInfo justBlockInfo = new JustBlockInfo();
            int peerSize = 0;
            String joinedPeer = conf.getJoinedPeer();
            if(StringUtils.isNotNull(joinedPeer)){
                peerSize = joinedPeer.split(",").length;
            }
            JSONArray jsonArray = justBlockInfo.queryPeerBlock(blockchainInfo, channel, peerSize);
            return justBlockInfo.getResultJson(jsonArray,peerSize);
        }catch (Exception e){
            String errMsg = "Handlers | QueryBlock Error";
            logger.error(errMsg);
        }
        return null;
    }

    public JSONArray queryBlockBaseInfo(String channelName,String ordererName,String peerName) throws Exception {
        try{
            Channel channel = reconstructChannel(channelName,ordererName,peerName);
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            BlockchainInfo blockchainInfo = channel.queryBlockchainInfo();
            JustBlockInfo justBlockInfo = new JustBlockInfo();
            int peerSize = 0;
            String joinedPeer = conf.getJoinedPeer();
            if(StringUtils.isNotNull(joinedPeer)){
                peerSize = joinedPeer.split(",").length;
            }
            return justBlockInfo.queryPeerBlock(blockchainInfo, channel, peerSize);
        }catch (Exception e){
            String errMsg = "Handlers | QueryBlockBaseInfo Error";
            logger.error(errMsg);
        }
        return null;
    }


    public Set<String> queryAllChannels() throws Exception {
        Set<String> allChannel = new HashSet<>();
        try{
            for(int i=0;i<orgList.size();i++){
                String ordererName = orgList.get(i).getOrdererName();
                client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
                client.setUserContext(conf.enrollUser(ordererName));
                if(orgList.size() == peerOrgsList.size()){
                    List<PeerUnit> peers = peerOrgsList.get(i).getPeers();
                    allChannel.addAll(queryPeerChannel(peers,conf,client));
                }else{
                    for(int j=0;j<peerOrgsList.size();j++){
                        List<PeerUnit> peers = peerOrgsList.get(j).getPeers();
                        allChannel.addAll(queryPeerChannel(peers,conf,client));
                    }
                }
            }
            return allChannel;
        }catch (Exception e){
            String errMsg = "Handlers | QueryAllChannels "+e.getMessage();
            logger.error(errMsg);
            throw new Exception(e);
        }
    }

    private Set<String> queryPeerChannel(List<PeerUnit> peers,JustProperty conf,HFClient client) throws InvalidArgumentException {
        Set<String> allChannel = new HashSet<>();
        for (PeerUnit peerUnit : peers) {
            Peer peer = client.newPeer(peerUnit.getPeerName(), peerUnit.getPeerLocation(), conf.getPeerProperties(peerUnit.getPeerName()));
            Set<String> channels = null;
            try{
                channels = client.queryChannels(peer);
                allChannel.addAll(channels);
            }catch (Exception e){
                logger.error(String.format("peer:%s has not yet joined the any channel",peer.getName()));
            }
        }
        return allChannel;
    }

    /**
     * create a new channel.not construct a configed channel
     */
    public Channel createChannel(String channelName,String ordererName) throws Exception {
        String path = configPath+"/fixture/conf/";
        logger.info(format("Running create new channel:%s",channelName));
        try{
            genChannelTx(path,channelName);
            Channel newChannel = null;
            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            Orderer ordererVal = null;
            for(OrdererUnit orderer : orgList){
                if(orderer.getOrdererName().contains(ordererName)){
                    ordererVal = client.newOrderer(orderer.getOrdererName(),orderer.getOrdererLocation(),conf.getOrdererProperties(orderer.getOrdererName()));
                }
            }
            String channelConfigurationPath = path + "/"+channelName+".tx";
            ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(channelConfigurationPath));
            newChannel = client.newChannel(channelName,ordererVal,channelConfiguration,client.getChannelConfigurationSignature(channelConfiguration,conf.enrollUser(ordererName)));
            return newChannel;
        }catch (Exception e){
            String errMsg = "Handlers | CreateChannel "+e.getMessage();
            logger.error(errMsg);
            throw new Exception(e);
        }
    }

    public void peerJoinChannel(String ordererName,String peerName,String channelName) throws Exception {
        logger.info(format("Running peer: %s join channel:%s",peerName,channelName));
        try{
            Channel channel = reconstructChannel(channelName,ordererName,peerName);
            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            Collection<Orderer> orderers = channel.getOrderers();
            Orderer orderer = orderers.iterator().next();
            for(PeerOrg peerOrg : peerOrgsList){
                List<PeerUnit> peers = peerOrg.getPeers();
                for(PeerUnit peerUnit : peers){
                    if(peerName.equals(peerUnit.getPeerName())){
                        Peer peer = client.newPeer(peerUnit.getPeerName(), peerUnit.getPeerLocation(), conf.getPeerProperties(peerUnit.getPeerName()));
                        channel.joinPeer(orderer,peer,createPeerOptions());
                        if(!channel.isInitialized()){
                            try{
                                channel.initialize();
                            }catch (Exception e){
                                logger.error(e.getMessage());
                            }
                        }
                    }
                }
            }
        }catch (Exception e){
            String errMsg = "Handlers | peerJoinChannel "+e;
            logger.error(errMsg);
            throw new Exception(e);
        }
    }

    public String installChaincode(String ordererName,String peerName,String chaincodeName, String chaincodeType, String version, String chaincodePath) throws Exception {
        logger.info(format("Running install chaincode on %s",peerName));
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        int numInstallProposal = 0;
        try{
            String fixturePath = configPath + "/fixture/";
            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            InstallProposalRequest installProposalRequest = getInstallProposalRequest(fixturePath,client, chaincodeName, chaincodeType,version, chaincodePath);
            Set<Peer> peerSet = new HashSet<>();
            for(PeerOrg peerOrg : peerOrgsList){
                List<PeerUnit> peers = peerOrg.getPeers();
                for(PeerUnit peerUnit : peers){
                    if(peerName.equals(peerUnit.getPeerName())){
                        Peer peer = client.newPeer(peerUnit.getPeerName(), peerUnit.getPeerLocation(), conf.getPeerProperties(peerUnit.getPeerName()));
                        peerSet.add(peer);
                    }
                }
            }
            Collection<ProposalResponse> proposalResponses = client.sendInstallProposal(installProposalRequest, peerSet);
            for(ProposalResponse pr : proposalResponses){
                if(pr.getStatus() == ProposalResponse.Status.SUCCESS){
                    logger.debug(format("Successful install proposal response Txid: %s from peer %s",
                            pr.getTransactionID(), pr.getPeer().getName()));
                    successful.add(pr);
                }else{
                    failed.add(pr);
                }
            }
            if (failed.size() > 0){
                ProposalResponse next = failed.iterator().next();
                String errMsg = "Not enough endorsers for install:"+successful.size()+". "+next.getMessage();
                throw new Exception(errMsg);
            }
            logger.info(format("Received %d install proposal response.Successful+Verified: %d.Failed: %d",numInstallProposal,successful.size(),failed.size()));
            return "Chaincode installed Successfully";
        }catch (Exception e){
            String errMsg = "Handlers | installChaincode |"+e;
            logger.error(errMsg);
            throw new Exception("chaincode installtion failed",e);
        }
    }


    public Map<String, String> instantieteChaincode(String channelName,String ordererName,String peerName,String chaincodeName,String chaincodePath, String version,String[] args, String endorsePath) throws Exception {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        Map<String, String> callback = new HashMap<>();
        logger.info(format("Running instantiate chaincode on chanel:%s",channelName));
        try{
            Channel channel = reconstructChannel(channelName,ordererName,peerName);
            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            Collection<Orderer> orderers = channel.getOrderers();
            InstantiateProposalRequest instantiateProposalRequest = getInstantiateProposalRequest(client, chaincodeName,chaincodePath, version, args, endorsePath);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
            tm.put("result", ":)".getBytes(UTF_8));
            instantiateProposalRequest.setTransientMap(tm);
            Collection<ProposalResponse> proposalResponses = channel.sendInstantiationProposal(instantiateProposalRequest);
            for (ProposalResponse response : proposalResponses) {
                if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                    callback.put("status", "success");
                    callback.put("data", "");
                    channel.sendTransaction(successful).get(100000, TimeUnit.SECONDS);
                    logger.info(format("Succesful instantiate proposal response Txid: %s from peer %s",
                            response.getTransactionID(), response.getPeer().getName()));
                } else {
                    failed.add(response);
                    ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                    callback.put("status", "error");
                    callback.put("data", firstTransactionProposalResponse.getMessage());
                    logger.info("Instantiate chaincode failed");
                }
            }
            logger.info(format("Received %d instantiate proposal responses. Successful+verified: %d . Failed: %d",
                    proposalResponses.size(), successful.size(), failed.size()));
            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                String errMsg = "Chaincode instantiation failed , reason " + "Not enough endorsers for instantiate :"
                        + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:"
                        + first.isVerified();
                throw new Exception(errMsg);
            }
            logger.info("Sending instantiateTransaction to orderer");
            return callback;
        }catch (Exception e){
            String errMsg = "Handlers | instantiateChaincode |" + e;
            logger.error(errMsg);
            throw new Exception(errMsg, e);
        }
    }

    public Map<String, String> upgradeChaincode(String channelName,String ordererName,String peerName,String chaincodeName,String chaincodePath, String version,String[] args, String endorsePath) throws Exception {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        Map<String, String> callback = new HashMap<>();
        logger.info(format("Running upgrade chaincode on channel:%s",channelName));
        try{
            Channel channel = reconstructChannel(channelName,ordererName,peerName);
            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            UpgradeProposalRequest upgradeProposalRequest = getUpgradeProposalRequest(client, chaincodeName, chaincodePath, version, args, endorsePath);
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
            tm.put("result", ":)".getBytes(UTF_8));
            upgradeProposalRequest.setTransientMap(tm);
            Collection<ProposalResponse> proposalResponses = channel.sendUpgradeProposal(upgradeProposalRequest);
            for (ProposalResponse response : proposalResponses) {
                if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                    ProposalResponse resp = proposalResponses.iterator().next();
                    callback.put("status", "success");
                    callback.put("data", "");
                    channel.sendTransaction(successful).get(100000, TimeUnit.SECONDS);
                    logger.info(format("Succesful upgrade proposal response Txid: %s from peer %s",
                            response.getTransactionID(), response.getPeer().getName()));
                } else {
                    failed.add(response);
                    ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                    callback.put("status", "error");
                    callback.put("data", firstTransactionProposalResponse.getMessage());
                    logger.info("Upgrade chaincode failed");
                }
            }
            logger.info(format("Received %d upgrade proposal responses. Successful+verified: %d . Failed: %d",
                    proposalResponses.size(), successful.size(), failed.size()));
            if (failed.size() > 0) {
                ProposalResponse first = failed.iterator().next();
                String errMsg = "Chaincode Upgrade failed , reason " + "Not enough endorsers for upgrade :"
                        + successful.size() + "endorser failed with " + first.getMessage() + ". Was verified:"
                        + first.isVerified();
                throw new Exception(errMsg);
            }
            logger.info("Sending upgradeTransaction to orderer");
            return callback;
        }catch (Exception e){
            String errMsg = "Handlers | Upgrade chaincode |" + e;
            logger.error(errMsg);
            throw new Exception(errMsg, e);
        }
    }



    private InstallProposalRequest getInstallProposalRequest(String fixturePath, HFClient client,String chaincodeName, String chaincodeType, String version, String chaincodePath) throws InvalidArgumentException {
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(version)
                .setPath(chaincodePath).build();
        logger.info("Creating install proposal");
        InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
        installProposalRequest.setChaincodeID(chaincodeID);
        String meta_inf_path = fixturePath+"/src/"+chaincodeID.getPath();
        boolean existMetaFile = isExistMetaFile(meta_inf_path);
        if (existMetaFile){
            File metaFile = new File(meta_inf_path);
            if(metaFile.exists() && metaFile.isDirectory()){
                //add indexes,if the directory exists the META-INF then add ,else no add
                installProposalRequest.setChaincodeMetaInfLocation(metaFile);
            }
        }
        installProposalRequest.setChaincodeVersion(chaincodeID.getVersion());
        if (chaincodeType.equals("GO") || chaincodeType.equals("Go") || chaincodeType.equals("go")){
            installProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            installProposalRequest.setChaincodeSourceLocation(Paths.get(fixturePath).toFile());
        }
        if (chaincodeType.equals("JAVA") || chaincodeType.equals("Java") || chaincodeType.equals("java")){
            installProposalRequest.setChaincodeLanguage(TransactionRequest.Type.JAVA);
            String ccPath = fixturePath+"/src/"+chaincodePath;
            installProposalRequest.setChaincodeSourceLocation(Paths.get(ccPath).toFile());
        }
        return installProposalRequest;
    }

    private boolean isExistMetaFile(String path){
        boolean flag = false;
        File file = new File(path);
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            for(File everyFile : files){
                if("META-INF".equals(everyFile.getName())){
                    flag = true;
                }
            }
        }
        return flag;
    }

    private InstantiateProposalRequest getInstantiateProposalRequest(HFClient client,String chaincodeName, String chaincodePath, String version, String[] chaincodeArgs,String endorsePath) throws IOException, ChaincodeEndorsementPolicyParseException {
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setPath(chaincodePath).setVersion(version).build();
        InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
        instantiateProposalRequest.setChaincodeID(chaincodeID);
        instantiateProposalRequest.setArgs(chaincodeArgs);
        instantiateProposalRequest.setProposalWaitTime(100000);
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(endorsePath));
        instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
        return instantiateProposalRequest;
    }

    private UpgradeProposalRequest getUpgradeProposalRequest(HFClient client,String chaincodeName, String chaincodePath, String version, String[] chaincodeArgs,String endorsePath) throws IOException, ChaincodeEndorsementPolicyParseException {
        ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setPath(chaincodePath).setVersion(version).build();
        UpgradeProposalRequest upgradeProposalRequest = client.newUpgradeProposalRequest();
        upgradeProposalRequest.setChaincodeID(chaincodeID);
        upgradeProposalRequest.setArgs(chaincodeArgs);
        upgradeProposalRequest.setProposalWaitTime(100000);
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        chaincodeEndorsementPolicy.fromYamlFile(new File(endorsePath));
        upgradeProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
        return upgradeProposalRequest;
    }

    public Map<String,Object> invoke(String channelName,String ordererName,String peerName,String chaincodename, String chaincodeFunction, String[] chaincodeArgs) throws Exception {
        Collection<ProposalResponse> successful = new ArrayList<>();
        Collection<ProposalResponse> failed = new ArrayList<>();
        Map<String, Object> callback = new HashMap<>();
        try{
            Channel channel = reconstructChannel(channelName,ordererName,peerName);
            HFClient client = HFClient.createNewInstance();
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodename).build();
            logger.info("Channel Name is " + channel.getName());
            logger.debug(format("Querying chaincode %s and function %s with arguments %s", chaincodename,
                    chaincodeFunction, Arrays.asList(chaincodeArgs).toString()));
            TransactionProposalRequest transactionProposalRequest = client.newTransactionProposalRequest();
            transactionProposalRequest.setArgs(chaincodeArgs);
            transactionProposalRequest.setFcn(chaincodeFunction);
            transactionProposalRequest.setChaincodeID(chaincodeID);
            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
            transactionProposalRequest.setTransientMap(tm2);
            logger.debug("Chaincode request args:- " + transactionProposalRequest.getArgs().toString());
            Collection<ProposalResponse> proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
            for (ProposalResponse proposalResponse : proposalResponses) {
                if (proposalResponse.isVerified() && proposalResponse.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(proposalResponse);
                } else {
                    failed.add(proposalResponse);
                }
            }
            if (failed.size() != 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                logger.error("Not enough endorsers for inspect:" + failed.size() + " endorser error: " + firstTransactionProposalResponse.getMessage() + ". Was verified: "
                        + firstTransactionProposalResponse.isVerified());
                callback.put("state", 10010);
                callback.put("payload", firstTransactionProposalResponse.getMessage());
                logger.info("Return result : {}",callback);
                return callback;
            } else {
                logger.info("Successfully received transaction proposal responses.");
                ProposalResponse resp = proposalResponses.iterator().next();
                byte[] x = resp.getChaincodeActionResponsePayload();
                String resultAsString = null;
                if (x != null) {
                    resultAsString = new String(x, "UTF-8");
                }
                CompletableFuture<BlockEvent.TransactionEvent> transactionEventCompletableFuture = channel.sendTransaction(successful);
                boolean completedExceptionally = transactionEventCompletableFuture.isCompletedExceptionally();
                String transactionID = resp.getTransactionID();
                if (completedExceptionally) {
                    callback.put("state", 10010);
                    callback.put("message", "failed");
                    callback.put("payload", "");
                } else {
                    callback.put("state", 10000);
                    callback.put("message", "success");
                    callback.put("payload", transactionID);
                }
            }
        }catch (Exception e){
            callback.put("state", 10010);
            callback.put("message",e.getMessage());
            return callback;
        }
        return callback;
    }


    public Map<String,Object> query(String channelName,String ordererName,String peerName,String chaincodename, String chaincodeFunction, String[] chaincodeArgs) throws Exception {
        Map<String,Object> callback = new HashMap<>();
        try{
            Channel channel = reconstructChannel(channelName,ordererName,peerName);
            client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            client.setUserContext(conf.enrollUser(ordererName));
            ChaincodeID chaincodeID = ChaincodeID.newBuilder().setName(chaincodename).build();
            logger.info("Channel Name is " + channel.getName());
            logger.debug(format("Querying chaincode %s and function %s with arguments %s", chaincodename,
                    chaincodeFunction, Arrays.asList(chaincodeArgs).toString()));
            QueryByChaincodeRequest queryByChaincodeRequest = client.newQueryProposalRequest();
            queryByChaincodeRequest.setArgs(chaincodeArgs);
            queryByChaincodeRequest.setFcn(chaincodeFunction);
            queryByChaincodeRequest.setChaincodeID(chaincodeID);
            Map<String, byte[]> tm2 = new HashMap<>();
            tm2.put("HyperLedgerFabric", "QueryByChaincodeRequest:JavaSDK".getBytes(UTF_8));
            tm2.put("method", "QueryByChaincodeRequest".getBytes(UTF_8));
            queryByChaincodeRequest.setTransientMap(tm2);
            logger.debug("Chaincode request args:- " + queryByChaincodeRequest.getArgs().toString());
            Collection<ProposalResponse> queryProposals = channel.queryByChaincode(queryByChaincodeRequest, channel.getPeers());
            for (ProposalResponse proposalResponse : queryProposals) {
                if (!proposalResponse.isVerified() || proposalResponse.getStatus() != ProposalResponse.Status.SUCCESS) {
                    String errorMsg = "Failed query proposal from peer " + proposalResponse.getPeer().getName()
                            + " status: " + proposalResponse.getStatus() + ". Messages: "
                            + proposalResponse.getMessage() + ". Was verified : " + proposalResponse.isVerified();
                    logger.debug(errorMsg);
                    callback.put("state", 10010);
                    callback.put("payload", 0);
                    callback.put("message", "Failed query proposal from peer " + proposalResponse.getPeer().getName() + " status: " + proposalResponse.getStatus() + ". Messages: "
                            + proposalResponse.getMessage() + ". Was verified : " + proposalResponse.isVerified());
                    throw new Exception(errorMsg);
                } else {
                    String payload = proposalResponse.getProposalResponse().getResponse().getPayload().toStringUtf8();
                    callback.put("state", 10000);
                    callback.put("payload",payload);
                    callback.put("message", "success");
                }
            }
        }catch (Exception e){
            callback.put("state", 10010);
            callback.put("message",e.getMessage());
            return callback;
        }
        return callback;
    }

    private void genChannelTx(String path,String channelName) throws IOException, InterruptedException {
            String os = System.getProperty("os.name");
            Process process;
            if (os.startsWith("win") || os.startsWith("Win")) {
                File file = new File(path.replace("/","\\"));
                String[] args = {"cmd","/c","configtxgen","-profile",profiles,"-outputCreateChannelTx",path.replace("/","\\")+channelName+".tx","-channelID",channelName};
                process = Runtime.getRuntime().exec(args,null,file);
            } else {
                File file = new File(path);
                String[] args = {path+"/configtxgen","-profile",profiles,"-outputCreateChannelTx",channelName+".tx","-channelID",channelName};
                process = Runtime.getRuntime().exec(args,null,file);
            }
            int value = process.waitFor();
            if(value == 0){//0:代表正常退出
                logger.info("Create {}.tx success",channelName);
            }else{
                logger.info("Create {}.tx failed",channelName);
                process.destroy();
            }

        }

}
