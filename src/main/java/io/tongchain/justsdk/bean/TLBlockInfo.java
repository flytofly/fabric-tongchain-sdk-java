package io.tongchain.justsdk.bean;

/**
 * @Author:wangkeke
 * @Date: 2019/6/5 0005下午 1:56
 */
public class TLBlockInfo {
    /**
     * 块高度
     */
    private int blockHeight;
    /**
     * 时间
     */
    private String time;

    /**
     * 时间差
     */
    private String diffTime;

    /**
     * 交易数
     */
    private int txCount;
    /**
     * 块大小
     */
    private Long blockSize;
    /**
     * 交易哈希
     */
    private String txHash;

    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDiffTime() {
        return diffTime;
    }

    public void setDiffTime(String diffTime) {
        this.diffTime = diffTime;
    }

    public int getTxCount() {
        return txCount;
    }

    public void setTxCount(int txCount) {
        this.txCount = txCount;
    }

    public Long getBlockSize() {
        return blockSize;
    }

    public void setBlockSize(Long blockSize) {
        this.blockSize = blockSize;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}
