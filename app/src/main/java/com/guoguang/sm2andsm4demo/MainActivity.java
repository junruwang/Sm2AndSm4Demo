package com.guoguang.sm2andsm4demo;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.sm.api.SmExec;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.guoguang.sm.SM2Utils;
import com.guoguang.sm.SMS4;
import com.guoguang.sm.Util;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private static final String TAG="SmDemo";
    private Button open,genSm2Pair,getPubKey,setMainKey,decrypt,setWorkKey,openCursor,getCursor;
    private Button allowGetCursor,notAllowGetCursor,decryptCursor,jump;
    private TextView showResult,showResult2,showCursor;
    private SmExec smExec;
    private SMS4 sms4;

    private GetCursorInfoThread getCursorInfoThread;

    private String sm2EncryptData="";
    private String san="F921B578FC8928CBCAC64C19577955C2B8C070F55BEE76677DB2711449532EC7D0E394B7FB6028FB3B24441D013D8C99CBE5577C61504A658D12D6F4471B9CC5";
    private String sm2PrimaryKey="000D2B62ADC83EC7671EE8827A835E94DEFB6FB658C7932FDFE9C7B8031F4EDB";

    private String mainKey="11223344556677888877665544332211";

    private String workKey="00000000000000008888888888888888";
    private String sm4Key="11223344556677888877665544332211";

    private volatile byte[] cursorInfo=new byte[4096];

    private volatile int abcde;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            String data=(String )msg.obj;
            showCursor.setText(data);
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showResult=(TextView)findViewById(R.id.showResult);
        showResult2=(TextView)findViewById(R.id.showResult2);
        showCursor=(TextView)findViewById(R.id.showCursor);

        open=(Button)findViewById(R.id.open);
        genSm2Pair=(Button)findViewById(R.id.genSm2Pair);
        getPubKey=(Button)findViewById(R.id.getPubKey);
        setMainKey=(Button)findViewById(R.id.setMainKey);
        decrypt=(Button)findViewById(R.id.decrypt);
        setWorkKey=(Button)findViewById(R.id.setWorkKey);
        openCursor=(Button)findViewById(R.id.openCursor);
        getCursor=(Button)findViewById(R.id.getCursor);
        allowGetCursor=(Button)findViewById(R.id.allowGetCursor);
        notAllowGetCursor=(Button)findViewById(R.id.notAllowGetCursor);
        decryptCursor=(Button)findViewById(R.id.decryptCursor);


        open.setOnClickListener(this);
        genSm2Pair.setOnClickListener(this);
        getPubKey.setOnClickListener(this);
        setMainKey.setOnClickListener(this);
        decrypt.setOnClickListener(this);
        setWorkKey.setOnClickListener(this);
        openCursor.setOnClickListener(this);
        getCursor.setOnClickListener(this);
        allowGetCursor.setOnClickListener(this);
        notAllowGetCursor.setOnClickListener(this);
        decryptCursor.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            //开端口
            case R.id.open:
                if(smExec==null){
                    smExec=new SmExec();
                }
                showResult.setText(""+smExec.openCom());
                break;

            //生成SM2密钥对
            case R.id.genSm2Pair:
                if(smExec==null){
                    smExec=new SmExec();
                }
                smExec.openCom();
                showResult.setText(""+smExec.genSm2PairKey());
                break;

            //获取公钥
            case R.id.getPubKey:
                if(smExec==null){
                    smExec=new SmExec();
                }
                //拿公钥
                byte[] pubKey=smExec.getSm2PubKey();
                //byte[] pubKey=Util.hexToByte(san);
                showResult2.setText(Util.byteToHex(pubKey));
                Log.d(TAG,"数据pubkey:"+Util.byteToHex(pubKey));

                //加头字段04
                byte[] dataHead=Util.hexToByte("04");
                byte[] wholeData=new byte[pubKey.length+dataHead.length];

                System.arraycopy(dataHead,0,wholeData,0,dataHead.length);
                System.arraycopy(pubKey,0,wholeData,dataHead.length,pubKey.length);

               // byte[] wholeData=Util.hexToByte("04"+Util.byteToHex(pubKey));
                try {
                    //加密
                    sm2EncryptData= SM2Utils.encrypt(wholeData,Util.hexToByte(mainKey));
                    //去04
                    sm2EncryptData=sm2EncryptData.substring(2,sm2EncryptData.length());
                    Log.d(TAG,"数据:"+sm2EncryptData);
                    showResult.setText(sm2EncryptData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            //设置主密钥
            case R.id.setMainKey:
                if(smExec==null){
                    smExec=new SmExec();
                }
                smExec.setMainKey(Util.hexToByte(sm2EncryptData));
                break;

            //解密主密钥
            case R.id.decrypt:
                byte[] data;
                if(smExec==null){
                    smExec=new SmExec();
                }
                try {
                    data=SM2Utils.decrypt(Util.hexToByte(sm2PrimaryKey),Util.hexToByte(sm2EncryptData));
                    showResult.setText(Util.byteToHex(data));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            //设置工作秘钥
            case R.id.setWorkKey:
                if(smExec==null){
                    smExec=new SmExec();
                }
                if(sms4==null){
                    sms4=new SMS4();
                }
                byte[] cipherWorkKey = new byte[16];
                int inLen=16,ENCRYPT=1;
                //workKey="14725800000000000000000000000000";
                //sm4Key="088152011C761137EF9E5B4081222FBA";
                byte[] workKey_byte=Util.hexToByte(workKey);
                byte[] sm4Key_byte=Util.hexToByte(sm4Key);
                sms4.sms4(workKey_byte, inLen, sm4Key_byte, cipherWorkKey, ENCRYPT);
                Log.v(TAG, "主密钥"+Util.byteToHex(sm4Key_byte));
                Log.v(TAG, "工作密钥"+Util.byteToHex(workKey_byte));
                Log.v(TAG, "加密后的工作密钥"+Util.byteToHex(cipherWorkKey));

                smExec.setWorkKey(cipherWorkKey);
                break;
            //打开curosr端口
            case R.id.openCursor:
                if(smExec==null){
                    smExec=new SmExec();
                }
                int a=smExec.openCursorCom();
                showResult.setText(""+a);
                break;

            //允许读取光标
            case R.id.allowGetCursor:
                if(smExec==null){
                    smExec=new SmExec();
                }
                smExec.allowGetCursorInfo(1);
                break;

            //禁止读取光标
            case R.id.notAllowGetCursor:
                if(smExec==null){
                    smExec=new SmExec();
                }
                smExec.allowGetCursorInfo(0);
                break;

            //读取光标信息
            case R.id.getCursor:
                if(getCursorInfoThread==null){
                    getCursorInfoThread=new GetCursorInfoThread();
               }
                getCursorInfoThread.start();
                abcde=0;
                break;
            //解密sm4
            case R.id.decryptCursor:
                if(smExec==null){
                    smExec=new SmExec();
                }
                if(sms4==null){
                    sms4=new SMS4();
                }
                byte[] dataIn =new byte[16];
                int i;
                for (i=0;i<16;i++){
                    dataIn[i]=cursorInfo[i];
                }
                int inLen2=16,ENCRYPT2=0;
                byte[] cipherWorkKey2=new byte[16];
                byte[] workKey_byte2=Util.hexToByte(workKey);
                sms4.sms4(dataIn, inLen2, workKey_byte2, cipherWorkKey2, ENCRYPT2);
                Log.v(TAG, "工作密钥"+Util.byteToHex(workKey_byte2));
                Log.v(TAG, "解密后的明文"+Util.byteToHex(cipherWorkKey2));

                showResult2.setText(Util.byteToHex(cipherWorkKey2));
                break;
            case R.id.jump:
                Intent intent=new Intent(MainActivity.this,SignatureActivity.class);
                startActivity(intent);
                break;
        }
    }

    public class GetCursorInfoThread extends Thread{
        @Override
        public void run() {
            if(smExec==null){
                smExec=new SmExec();
            }
                cursorInfo = smExec.getCursorInfo();
                Message message = new Message();
                message.obj = Util.byteToHex(cursorInfo);
                handler.sendMessage(message);
            }
    }
}
