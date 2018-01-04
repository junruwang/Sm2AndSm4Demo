package android.sm.api;

/**
 * Created by allen on 2017/9/6.
 */

public class SmExec {
    static
    {
        System.loadLibrary("smjni");
    }

    private native int open();

    private native int openCursor();

    /**
     * 生成Sm2密钥对
     * @return
     */
    private native int genSM2PairFromJNI();

    /**
     * 获取Sm2公钥
     * @return
     */
    private native byte[] getSM2PubFromJNI();

    /**
     * 设置主密钥
     * @param mainKey
     * @return
     */
    private native int setMainKeyFromJNI(byte[] mainKey);

    /**
     * 设置工作密钥
     * @param
     * @return
     */
    private native int setWorkKeyFromJNI(byte[] workKey);

    /**
     * 比较密钥
     * @param keyType
     * @param verifiedData
     * @return
     */
    private native int vertifyKeyFromJNI(int keyType,String verifiedData);

    private native byte[] getCursor();

    private native int allowGetCursor(int mode);




    public int openCom(){
        return open();
    }

    public int openCursorCom(){
        return openCursor();
    }

    public int genSm2PairKey(){
        return genSM2PairFromJNI();
    }

    public byte[]  getSm2PubKey(){
        return getSM2PubFromJNI();
    }

    public int setMainKey(byte[] mainKey){
        return setMainKeyFromJNI(mainKey);
    }

    public int setWorkKey(byte[] workKey) {
        return setWorkKeyFromJNI(workKey);
    }

    public byte[] getCursorInfo(){
        return getCursor();
    }

    public int allowGetCursorInfo(int mode){
        return allowGetCursor(mode);
    }


}
