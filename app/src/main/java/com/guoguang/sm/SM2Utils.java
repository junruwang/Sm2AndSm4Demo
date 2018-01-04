package com.guoguang.sm;

import android.util.Log;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECPoint;

import java.io.IOException;
import java.math.BigInteger;

public class SM2Utils 
{
	private static final String TAG="SM2Utils";
	public static String prik="0F60EBDA337019D38B903C81D21BD3E4B69CC4AAB82972599F8173863E99A3A2" ;
	public static String pubk ;
	//生成随机秘钥对
	public static void generateKeyPair(){
		SM2 sm2 = SM2.Instance();
		AsymmetricCipherKeyPair key = sm2.ecc_key_pair_generator.generateKeyPair();
		ECPrivateKeyParameters ecpriv = (ECPrivateKeyParameters) key.getPrivate();
		ECPublicKeyParameters ecpub = (ECPublicKeyParameters) key.getPublic();
		BigInteger privateKey = ecpriv.getD();
		ECPoint publicKey = ecpub.getQ();
	/*	ecpub.getQ().getX().toBigInteger();
		ecpub.getQ().getY();*/
		
		prik= Util.byteToHex(privateKey.toByteArray());
		pubk=Util.byteToHex(publicKey.getEncoded());
		Log.d(TAG,"公钥: " + Util.byteToHex(publicKey.getEncoded()));
		Log.d(TAG,"私钥: " + Util.byteToHex(privateKey.toByteArray()));
		/*System.out.println("公钥X: " + Util.byteToHex(ecpub.getQ().getX().toBigInteger().toByteArray()));
		System.out.println("公钥Y: " + Util.byteToHex(ecpub.getQ().getY().toBigInteger().toByteArray()));
		System.out.println("公钥长度: " + Util.byteToHex(publicKey.getEncoded()).length()+
				"publicKey.getEncoded():"+publicKey.getEncoded().length);
		System.out.println("私钥: " + Util.byteToHex(privateKey.toByteArray()));
		System.out.println("私钥长度: " + Util.byteToHex(privateKey.toByteArray()).length());*/
	}
	
	//数据加密
	public static String encrypt(byte[] publicKey, byte[] data) throws IOException
	{
		if (publicKey == null || publicKey.length == 0)
		{
			return null;
		}
		
		if (data == null || data.length == 0)
		{
			return null;
		}
		
		byte[] source = new byte[data.length];
		System.arraycopy(data, 0, source, 0, data.length);
		
		Cipher cipher = new Cipher();
		SM2 sm2 = SM2.Instance();
		ECPoint userKey = sm2.ecc_curve.decodePoint(publicKey);
		
		ECPoint c1 = cipher.Init_enc(sm2, userKey);
		cipher.Encrypt(source);
		byte[] c3 = new byte[32];
		cipher.Dofinal(c3);
		
		System.out.println("C1 " + Util.byteToHex(c1.getEncoded()));
		System.out.println("C1长度 " + Util.byteToHex(c1.getEncoded()).length());
		/*System.out.println("C2 " + Util.byteToHex(source));
		System.out.println("C3 " + Util.byteToHex(c3));
		System.out.println("C3 " + Util.byteToHex(c3).length());*/
		//C1 C2 C3拼装成加密字串
		//return Util.byteToHex(c1.getEncoded()) + Util.byteToHex(source) + Util.byteToHex(c3);
		return Util.byteToHex(c1.getEncoded()) +Util.byteToHex(source)+Util.byteToHex(c3);

	}
	
	//数据解密
	public static byte[] decrypt(byte[] privateKey, byte[] encryptedData) throws IOException
	{
		if (privateKey == null || privateKey.length == 0)
		{
			return null;
		}
		
		if (encryptedData == null || encryptedData.length == 0)
		{
			return null;
		}
		//加密字节数组转换为十六进制的字符串 长度变为encryptedData.length * 2
		String data = Util.byteToHex(encryptedData);
		/***分解加密字串
		 * （C1 = C1标志位2位 + C1实体部分128位 = 130）
		 * （C3 = C3实体部分64位  = 64）
		 * （C2 = encryptedData.length * 2 - C1长度  - C2长度）
		 */
		byte[] c1Bytes = Util.hexToByte(data.substring(0,130));
		int c2Len = encryptedData.length - 97;
		byte[] c2 = Util.hexToByte(data.substring(130,130 + 2 * c2Len));
		byte[] c3 = Util.hexToByte(data.substring(130 + 2 * c2Len,194 + 2 * c2Len));
		
		SM2 sm2 = SM2.Instance();
		BigInteger userD = new BigInteger(1, privateKey);
		
		//通过C1实体字节来生成ECPoint
		ECPoint c1 = sm2.ecc_curve.decodePoint(c1Bytes);
		Cipher cipher = new Cipher();
		cipher.Init_dec(userD, c1);
		cipher.Decrypt(c2);
		cipher.Dofinal(c3);
		
		//返回解密结果
		return c2;
	}
	
	public static void main(String[] args) throws Exception
	{
		//生成密钥对
		generateKeyPair();		
		String plainText = "624C";
		byte[] sourceData = plainText.getBytes();		
		//下面的秘钥可以使用generateKeyPair()生成的秘钥内容
		// 国密规范正式私钥
		String prik = "717CC22ADC7240DAF213F2D39799B4B1BC6B983D0184BFFBEE00EE3534762415";
		// 国密规范正式公钥
	/*	String pubk = "04b53a944e4dde697388e330702273288a09228ab408b71123b5c4930ace9152dd" +
				"a199ce9758c744537cc3b134d2961e92e1e13805bea5b5611de06717d920953b";*/
		//		
		System.out.println("加密: "+pubk.length());
		//String cipherText = SM2Utils.encrypt(Util.hexToByte(pubk), sourceData);
		String cipherText="0439A03D1ED9609C5902930A05E528C939873A8406865843C125D0C777231C7B609A4F2A2DED55F219172F7FDDC5AEF1D32D527E5E1C395F86AE666166F28699C28F80489DB34A01F62330D198F21C2B33A92D3944FA4C2D6E7B7E8BFD253C08A2668D";
		System.out.println(cipherText);
		System.out.println("解密: ");
		
		plainText = new String(SM2Utils.decrypt(Util.hexToByte(prik), Util.hexToByte(cipherText)));
		plainText=Util.byteToHex(SM2Utils.decrypt(Util.hexToByte(prik), Util.hexToByte(cipherText)));
		System.out.println(plainText);
		
	}
}

