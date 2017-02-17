package com.dentistshow;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WxPayUtils {
	
	private IWXAPI msgApi;
	private StringBuffer sb;

	public WxPayUtils(Context context) {
		msgApi = WXAPIFactory.createWXAPI(context, null);
		sb = new StringBuffer();
	}

	public void genPayReq(PayReq req, Map<String, String> resultunifiedorder) {

		req.appId = Constants.APP_ID;
		req.partnerId = Constants.MCH_ID;
		req.prepayId = resultunifiedorder.get("prepay_id");
		req.nonceStr = GetPrepayIdTask.genNonceStr();
		req.timeStamp = String.valueOf(genTimeStamp());
		req.packageValue = "Sign=WXPay";

		List<NameValuePair> signParams = new LinkedList<NameValuePair>();
		signParams.add(new BasicNameValuePair("appid", req.appId));
		signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
		signParams.add(new BasicNameValuePair("package", req.packageValue));
		signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
		signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
		signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));
		
		req.sign = genAppSign(signParams);
		
		sb.append("sign\n" + req.sign + "\n\n");
		msgApi.registerApp(Constants.APP_ID);
		msgApi.sendReq(req);

		Log.e("orion", signParams.toString());
	}

	private String genAppSign(List<NameValuePair> params) {
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < params.size(); i++) {
			sb.append(params.get(i).getName());
			sb.append('=');
			sb.append(params.get(i).getValue());
			sb.append('&');
		}
		sb.append("key=");
		sb.append(Constants.API_KEY);

		this.sb.append("sign str\n" + sb.toString() + "\n\n");
		String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
		
		Log.e("orion", appSign);
		return appSign;
	}

	private long genTimeStamp() {
		return System.currentTimeMillis() / 1000;
	}

}
