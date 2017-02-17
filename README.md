# WXPayDemo
Android集成微信支付
​	最近在项目中使用为微信支付，因为微信支付的官方文档没有进行太多的说明，so特此写篇博客记录下

​	下面是我按照微信官方提供的案例所修改的一个微信支付的[Demo](https://github.com/sunzq19931016/WXPayDemo)(Eclipse版并且包含服务端实例)

这里说明下我在Demo中模拟了生成预支付的信息在[GetPrepayIdTask.java](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/GetPrepayIdTask.java)中，服务器的同学可以仿照进行写下

主要的项目结构如下：

![Android集成微信支付](https://ws1.sinaimg.cn/large/e8f64008ly1fct7oswur7j207z0bwdfu)

### 准备工作

> 1.首先导入微信提供的jar包(libammsdk.jar)[微信支付](https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=11_1)

> 2.[Demo](https://github.com/sunzq19931016/WXPayDemo)

### 进行配置

>  1.在AndroidManifest.xml添加权限和基本的配置说明

```xml
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.MODIFY_AUDIO_SETTINGS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

```xml
 	<activity
            android:name=".wxapi.WXPayEntryActivity"
            android:exported="true"
            android:launchMode="singleTop" />
```

```xml
 <receiver android:name=".AppRegister" >
     <intent-filter>
          <action android:name="com.tencent.mm.plugin.openapi.Intent.ACTION_REFRESH_WXAPP" />
      </intent-filter>
 </receiver>
```

​	（一定不要忘记配置receiver，否则支付成功之后会崩溃）

​	支付[Activity](https://github.com/sunzq19931016/WXPayDemo)(当前调用支付Activity)的参数配置

```xml
 			<intent-filter>
                  <action android:name="android.intent.action.VIEW" />

                  <category android:name="android.intent.category.DEFAULT" />

                  <data android:scheme="xxx" /><!-- appid 微信分配的公众账号ID -->
            </intent-filter>
```

​	(详细看Demo)

> 2.之后导入[WXPayEntryActivity.java](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/wxapi/WXPayEntryActivity.java)文件，因为我在项目中需要把当前的支付结果返回给[Activity](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/PayActivity.java)所以用到了动态广播(后面会说明)，一定要记住要在wxapi包下，一定要记住要在wxapi包下，一定要记住要在wxapi包下。




> 3.导入[AppRegister.java](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/AppRegister.java)文件注册APP到微信中。


```java
public class AppRegister extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);

		// 注册app到微信
		msgApi.registerApp(Constants.APP_ID);
	}
}
```

> 4.建立Constants.java，放置配置参数（因为我在Demo 模拟了服务端一些信息会在其中）



```java
public class Constants {
	// appid 微信分配的公众账号ID
	public static final String APP_ID = "xxxxx";

	// 商户号 微信分配的公众账号ID
	public static final String MCH_ID = "xxxx";

	// API密钥，在商户平台设置
	public static final String API_KEY = "xxxxx";
}
```



> 5.  整体的结构是这样的我在Demo 中首先模拟了预支付的订单，之后调用位置支付进行支付，当然预支付订单信息都会有服务器给你生成之后你调用就可以了，这里我们只是模拟



![Android集成微信支付](https://ws1.sinaimg.cn/large/e8f64008ly1fct8dtrgh7j20g1083q4l)



> 6.  调用 app预支付订单我给封装到[GerPrepayIdTask.java](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/GetPrepayIdTask.java)中 代码中都写了注释可以进行观看

​	这里着重一点说明的是同时也是服务端的事情，要注意下面的参数信息

![Android集成微信支付](https://ws1.sinaimg.cn/large/e8f64008ly1fct8lzkk8rj20zx0bvwez)



> 7.  正式调用微信支付我封装在了[WxPayUtils.java](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/GetPrepayIdTask.java)中

​	因为我们需要的参数都是在GerPrepayIdTask的resultunifiedorder和req中，所以把这两个参数传递WxPayUtils中

![Android集成微信支付](https://ws1.sinaimg.cn/large/e8f64008ly1fctbgj9wg9j20p80ctgly)



​	为了安全，APP端调起支付的参数需要商户后台系统提供接口返回，参数说明文档：[https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2](https://pay.weixin.qq.com/wiki/doc/api/app/app.php?chapter=9_12&index=2)

​	主要从服务器接回来的参数就是我用红框圈起来的参数，而用大括号标书的参数就是模拟生成签名算法官方上都有给出



> 8. 这里面就用到我刚才说到的广播了，返回的结果都会返回到[WXPayEntryActivity](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/wxapi/WXPayEntryActivity.java)中，这里面我使用了动态注册广播

```java
@Override
	public void onResp(BaseResp resp) {
		Log.d(TAG, "onPayFinish, errCode = " + resp.errCode);
		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
			Intent intent = new Intent();
			intent.setAction(PayActivity.DYNAMICACTION);
			intent.putExtra("wechatResult", resp.errCode);
			sendBroadcast(intent);
			finish();
		}
	}
```



​	返回的结果在onResp方法中，resp.errCode就是返回的说明，这里面我用广播发送出去在支付的[Activity](https://github.com/sunzq19931016/WXPayDemo/blob/master/src/com/dentistshow/PayActivity.java)中进行注册和监听.详细请看[Demo](https://github.com/sunzq19931016/WXPayDemo)

```java
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
	protected void onStart() {
		super.onStart();
		dynamic_filter = new IntentFilter();
		dynamic_filter.addAction(DYNAMICACTION); // 添加动态广播的Action
		registerReceiver(dynamicReceiver, dynamic_filter); // 注册自定义动态广播消息
	}
```

​	在onDestory()中进行取消监听

```
@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(dynamicReceiver);
	}	
```

​	整理完成。



### 说明

> 如果你想用Demo跑通的时候一定要替换包名还有一些参数的添加
>
> 1.最重要的 参数信息一定要正确和服务器的人员进行确认和沟通，否则会出现签名不正确的错误
>
> ​		// appid 微信分配的公众账号ID
>
> ​		// 商户号 微信分配的公众账号ID
>
> ​		// API密钥，在商户平台设置
>
> 2.运行Demo的时候一定要注意包名的是否和你申请时候的包名是否是一致的
>
> 3.订单号不能重复使用，否则只能支付一次
>
> 4.因为微信支付的钱数 是不能有小数点的，并且会给钱数缩小100（Demo中有说明）
>
> 5.这里面一定要用正式的签名文件，就是你申请的时候所传的签名文件

### 参考

> [Android快速实现微信支付](http://www.jianshu.com/p/c97639279d2e)
>
> [Android 接入微信支付宝支付](http://wuxiaolong.me/2016/11/22/AndroidPay/)
