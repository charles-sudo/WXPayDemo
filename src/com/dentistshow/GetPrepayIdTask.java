package com.dentistshow;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.xmlpull.v1.XmlPullParser;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.util.Xml;

import com.tencent.mm.sdk.modelpay.PayReq;


/*
 * 假数据，一般情况下服务器由生成支付订单  这里是生成预支付的订单
 */

public class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String, String>> {
	private static final String TAG = "sunzq";
	private Activity activity;

	public PayReq req;
	public Map<String, String> resultunifiedorder;
	private StringBuffer sb;
	private ProgressDialog dialog;
	private int number;
	private String title;

	public GetPrepayIdTask(Activity activity, String number, String title) {
		this.activity = activity;
		this.title = title;
		this.number = mul(Double.parseDouble(number), 100); // 因为微信支付的钱数 是不能有小数点的，并且会给钱数缩小100
		sb = new StringBuffer();
		req = new PayReq();
	}

	@Override
	protected void onPreExecute() {
		dialog = ProgressDialog.show(activity, "提示", "正在获取预支付订单...");
	}
	
	/**
	 * 微信返回的数据 进行处理
	 */
	@Override
	protected void onPostExecute(Map<String, String> result) {
		if (dialog != null) {
			dialog.dismiss();
		}
		
		sb.append("prepay_id\n" + result.get("prepay_id") + "\n\n");

		resultunifiedorder = result;
		Log.e("resultunifiedorder", resultunifiedorder + "");
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}
	
	/**
	 * 向微信发送数据   返回所需要的 prepay_id
	 */
	@Override
	protected Map<String, String> doInBackground(Void... params) {

		String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
		String entity = genProductArgs();
		Log.e("orion1", entity);
		byte[] buf = Util.httpPost(url, entity);

		String content = new String(buf);
		Log.e("orion2", content);
		Map<String, String> xml = decodeXml(content);

		return xml;
	}

	/**
	 * https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_1
	 * 
	 * 统一下单（正常有服务器返回数据），这里只做了假处理
	 */
	private String genProductArgs() {
		StringBuffer xml = new StringBuffer();
		try {
			String nonceStr = genNonceStr();
			xml.append("</xml>");
			List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
			packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));  // 应用ID
			packageParams.add(new BasicNameValuePair("body", title)); 			   // 描述
			packageParams.add(new BasicNameValuePair("mch_id", Constants.MCH_ID)); //商户号
			packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));      //随机字符串  --->官方有随机算法
			packageParams.add(new BasicNameValuePair("notify_url", "127.0.0.1"));  //通知地址
			packageParams.add(new BasicNameValuePair("out_trade_no", genOutTradNo())); //商户订单号 这个因为是假数据所以要随机成功
			packageParams.add(new BasicNameValuePair("spbill_create_ip", "127.0.0.1")); //终端IP
			packageParams.add(new BasicNameValuePair("total_fee", number + ""));// 总金额
			packageParams.add(new BasicNameValuePair("trade_type", "APP"));   //交易类型 写死App

			String sign = genPackageSign(packageParams);  //签名，签名生成算法  https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=4_3
			packageParams.add(new BasicNameValuePair("sign", sign));  // 签名

			String xmlstring = toXml(packageParams);
			//这里使用的是ISO8859-1 编码，是因为title中如果使用中文 默认是utf-8的  所以要用ISO8859-1编码，就能正常显示中文
			return new String(xmlstring.toString().getBytes(), "ISO8859-1"); 

		} catch (Exception e) {
			Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
			return null;
		}
	}

	/**
	 * 解析生成的xml格式数据
	 */
	public Map<String, String> decodeXml(String content) {
		try {
			Map<String, String> xml = new HashMap<String, String>();
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(new StringReader(content));
			int event = parser.getEventType();
			while (event != XmlPullParser.END_DOCUMENT) {
				String nodeName = parser.getName();
				switch (event) {
				case XmlPullParser.START_DOCUMENT:
					break;
				case XmlPullParser.START_TAG:

					if ("xml".equals(nodeName) == false) {
						xml.put(nodeName, parser.nextText());
					}
					break;
				case XmlPullParser.END_TAG:
					break;
				}
				event = parser.next();
			}

			return xml;
		} catch (Exception e) {
			Log.e("orion----------", e.toString());
		}
		return null;
	}
	
	
	/**
	 * 随机生成字符串
	 */
	public static String genNonceStr() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}

	/**
	 * 生成签名
	 */
	private String genPackageSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
		Log.e("orion3", packageSign);
		return packageSign;
	}
	
	/**
	 * 转化为xml
	 */
	private String toXml(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();
		sb.append("<xml>");
		for (int i = 0; i < params.size(); i++) {
			sb.append("<" + params.get(i).getName() + ">");

			sb.append(params.get(i).getValue());
			sb.append("</" + params.get(i).getName() + ">");
		}
		sb.append("</xml>");

		Log.e("orion4", sb.toString());
		return sb.toString();
	}
	
	/**
	 * 商户订单号 这个因为是假数据所以要随机成功
	 * 并且订单号不能重复使用否则就会只能支付一次
	 */
	private String genOutTradNo() {
		Random random = new Random();
		return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
	}

	/**
	 * 精准转化
	 */
	public static int mul(double d, int v2) {
		BigDecimal b1 = new BigDecimal(Double.toString(d));
		BigDecimal b2 = new BigDecimal(Integer.toString(v2));
		return b1.multiply(b2).intValueExact();
	}

}
