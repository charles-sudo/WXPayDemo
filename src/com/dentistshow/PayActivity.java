package com.dentistshow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class PayActivity extends Activity {
	
	public static final String DYNAMICACTION = "com.dentistshow"; // 动态广播的Action字符串

	private static final String TAG = "sunzq";
	private IntentFilter dynamic_filter;
	private IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
	private GetPrepayIdTask getPrepayIdTask;
	private WxPayUtils wxpayUtils;
	
	private BroadcastReceiver dynamicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(DYNAMICACTION)) { // 动作检测
				int msg = intent.getIntExtra("wechatResult", 0);
				if (msg == 0) {
					Toast.makeText(context, "支付成功", Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(context, "支付失败", Toast.LENGTH_LONG).show();
				}
			}
		}
	};
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pay);
		wxpayUtils = new WxPayUtils(this);
		msgApi.registerApp(Constants.APP_ID);

		//生成  prepay_id  假数据  用于生产预支付的订单
		Button payBtn = (Button) findViewById(R.id.unifiedorder_btn);
		payBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getPrepayIdTask = new GetPrepayIdTask(PayActivity.this, "10", "test");
				getPrepayIdTask.execute();
			}
		});
		
		
		Button appayBtn = (Button) findViewById(R.id.appay_btn);
		appayBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				wxpayUtils.genPayReq(getPrepayIdTask.req, getPrepayIdTask.resultunifiedorder); // 微信支付成功
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		dynamic_filter = new IntentFilter();
		dynamic_filter.addAction(DYNAMICACTION); // 添加动态广播的Action
		registerReceiver(dynamicReceiver, dynamic_filter); // 注册自定义动态广播消息
	}


	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(dynamicReceiver);
	}
}
