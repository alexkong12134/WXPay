package com.flyer.nc.wxpay;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.flyer.nc.controller.base.BaseController;
import com.flyer.nc.util.JsonHelper;
import com.flyer.nc.util.PageData;
import com.flyer.nc.util.UuidUtil;

/**
 * 类名称：AppZcdl 创建人：NC 创建时间：2019-04-29
 */
@Controller
@RequestMapping(value = "app/Pay")
public class PayController extends BaseController {
	public static JsonHelper util = new JsonHelper();
	public static final String Appid = "wxbef5552b9f900067";// 小程序ID,一期
	public static final String spbill_create_ip = "127.0.0.1";// 终端IP,支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
	public static final String notify_url = "localhost";// 通知地址,异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
	public static final String trade_type = "JSAPI";// 交易类型,小程序固定JSAPI
	public static final String mch_id = "1571094891";// （商铺号）
	public static final String key = "1968a912403e42729e316132f87ca5e5";// （商户平台设置的密钥key）
	// 商户号:1571094891 api密钥:1968a912403e42729e316132f87ca5e5,
	// 小程序id:wxbef5552b9f900067

	/**
	 * 支付
	 *
	 * @param
	 * @param
	 * @param
	 * @return
	 */
	@RequestMapping("/pay")
	public void pay(HttpServletRequest request, HttpServletResponse response) {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		PageData pd = new PageData();
		pd = this.getPageData();
		// 测试用
		// pd.put("body", "10000100");// 商品描述
		// pd.put("total_fee", "1");// 标价金额，单位是分,前台出过来的是元
		// pd.put("openid", "10000100");// 用户标识
		pd.put("body", pd.getString("body"));// 商品描述
		pd.put("total_fee", pd.getString("total_fee"));// 标价金额，单位是分,前台出过来的是元
		pd.put("openid", pd.getString("openid"));// 用户标识
		try {
			String nonce_str = UuidUtil.get32UUID();
			TreeMap<Object, Object> tm = new TreeMap<Object, Object>(); // 签名生成算法需要按ASCII码排序， TreeMap默认按键的自然顺序升序进行排序
			tm.put("appid", Appid);// 小程序ID
			tm.put("mch_id", mch_id);// 商铺号
			// tm.put("device_info", "1000");// 设备号,可以不用
			tm.put("body", pd.getString("body"));// 商品描述
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			tm.put("out_trade_no", sdf.format(new Date()));// 商品订单号,自定义生成，保证唯一
			tm.put("nonce_str", nonce_str);// 随机字符串
			tm.put("total_fee", String.format("%.0f", Double.parseDouble(pd.getString("total_fee")) * 100));// 标价金额，单位是分
			tm.put("spbill_create_ip", spbill_create_ip);// 终端IP,支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
			tm.put("notify_url", notify_url);// 通知地址,异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
			tm.put("trade_type", trade_type);// 交易类型,小程序固定JSAPI
			tm.put("openid", pd.getString("openid"));// 用户标识
			// 把tm拼接字符串
			String str = tm.toString().substring(1, tm.toString().length() - 1).replaceAll(", ", "&");
			// 拼秘钥，
			str = str + "&key=" + key;
			// MD5加密，签名
			String sign = MD5Util.md5Encrypt32Upper(str);// 这里是第一次签名，用于调用统一下单接口
			tm.put("sign", sign);// 签名
			// tm.put("key", "xxxx0881XXXX0881xxxx0881XXXX0881");// key为商户平台设置的密钥key
			// map转成要发送的xml
			String sendXml = MapToXMLString.converter(tm).replaceAll("xmlroot", "xml").replaceAll("\\s+", "");
			// 调用统一下单接口，并接受返回的结果
			URL url = new URL("https://api.mch.weixin.qq.com/pay/unifiedorder");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.getOutputStream().write(sendXml.getBytes());
			con.getOutputStream().flush();
			con.getOutputStream().close();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
			String s;
			// 收到的xml，主要为了预支付id，prepay_id
			String receiveXml = "";
			while ((s = br.readLine()) != null) {
				receiveXml += s;
			}
			br.close();
			con.getInputStream().close();
//			PayParam p = new PayParam();
//			p.setNonceStr(UuidUtil.get32UUID());
//			int start = receiveXml.indexOf("<prepay_id><![CDATA[") + "<prepay_id><![CDATA[".length();
//			int end = receiveXml.indexOf("]", start);
//			p.setPackAge("prepay_id=" + receiveXml.substring(start, end));// 预支付id
//			p.setTimeStamp(Long.toString(new Date().getTime()));
//			// 要发送给小程序端的东西
//			TreeMap<Object, Object> clientTm = new TreeMap<Object, Object>();
//			clientTm.put("nonceStr", p.getNonceStr());
//			clientTm.put("package", p.getPackAge());
//			clientTm.put("timeStamp", p.getTimeStamp());
//			String clientStr = clientTm.toString().substring(1, clientTm.toString().length() - 1).replaceAll(", ", "&");
//			clientStr = clientStr + "&key=1968a912403e42729e316132f87ca5e5";
//			String clientSign = MD5Util.md5Encrypt32Upper(clientStr);// 再次签名，这个签名用于小程序端调用wx.requesetPayment方法
//			p.setPaySign(clientSign);
			// 将解析结果存储在HashMap中
			Map map = PayUtil.doXMLParse(receiveXml);
			String return_code = (String) map.get("return_code");// 返回状态码
			String result_code = (String) map.get("result_code");// 返回状态码
			PageData p = new PageData();// 返回给小程序端需要的参数
			if ("SUCCESS".equals(return_code)) {// (return_code == "SUCCESS" && return_code.equals(result_code))
				String prepay_id = (String) map.get("prepay_id");// 返回的预付单信息
				p.put("nonceStr", nonce_str);
				p.put("package", "prepay_id=" + prepay_id);
				Long timeStamp = System.currentTimeMillis() / 1000;
				p.put("timeStamp", timeStamp + "");// 这边要将返回的时间戳转化成字符串，不然小程序端调用wx.requestPayment方法会报签名错误
				// 拼接签名需要的参数
				String stringSignTemp = "appId=" + Appid + "&nonceStr=" + nonce_str + "&package=prepay_id=" + prepay_id
						+ "&signType=MD5&timeStamp=" + timeStamp;
				// 再次签名，这个签名用于小程序端调用wx.requesetPayment方法
//				String paySign = PayUtil.sign(stringSignTemp, "1968a912403e42729e316132f87ca5e5", "utf-8")
//						.toUpperCase();
				String paySign = MD5Util.md5Encrypt32Upper(stringSignTemp + key);
				p.put("paySign", paySign);
			}
			System.out.println(p);
			util.toAppJsonMsg(response, 1, p);
		} catch (Exception e) {
			e.printStackTrace();
			util.toAppJsonMsg(response, 0, null);
		}
	}

	/**
	 * 通知
	 *
	 * @param
	 * @param
	 * @param
	 * @return
	 */
	@RequestMapping("/notify")
	public void notify(HttpServletRequest request, HttpServletResponse response) throws Exception {
		response.setHeader("Access-Control-Allow-Origin", "*");
		response.setContentType("application/json");
		response.setStatus(HttpServletResponse.SC_OK);
		PageData pd = new PageData();
		pd = this.getPageData();
		String key = "";
		BufferedReader br = new BufferedReader(new InputStreamReader((ServletInputStream) request.getInputStream()));
		String line = null;
		StringBuilder sb = new StringBuilder();
		while ((line = br.readLine()) != null) {
			sb.append(line);
		}
		br.close();
		// sb为微信返回的xml
		String notityXml = sb.toString();
		String resXml = "";
		System.out.println("接收到的报文：" + notityXml);
		Map map = PayUtil.doXMLParse(notityXml);
		String returnCode = (String) map.get("return_code");
		if ("SUCCESS".equals(returnCode)) {
			// 验证签名是否正确
			if (PayUtil.verify(PayUtil.createLinkString(map), (String) map.get("sign"), key, "utf-8")) {
				// 通知微信服务器已经支付成功
				resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
						+ "<return_msg><![CDATA[OK]]></return_msg>" + "</xml> ";
			}
		} else {
			resXml = "<xml>" + "<return_code><![CDATA[FAIL]]></return_code>"
					+ "<return_msg><![CDATA[报文为空]]></return_msg>" + "</xml> ";
		}
		System.out.println(resXml);
		System.out.println("微信支付回调数据结束");
		BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
		out.write(resXml.getBytes());
		out.flush();
		out.close();
	}

	public static void main(String[] args) {
		PageData pd = new PageData();
		// 测试用
		pd.put("body", "666");// 商品描述
		pd.put("total_fee", "1");// 标价金额，单位是分
		pd.put("openid", "ONSoFObshubxgK-d5E2UXC4E56L0");// 用户标识
		try {
			String nonce_str = UuidUtil.get32UUID();
			TreeMap<Object, Object> tm = new TreeMap<Object, Object>(); // 签名生成算法需要按ASCII码排序， TreeMap默认按键的自然顺序升序进行排序
			tm.put("appid", Appid);// 小程序ID
			tm.put("mch_id", mch_id);// 商铺号
			tm.put("device_info", "1000");// 设备号,可以不用
			tm.put("body", pd.getString("body"));// 商品描述
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
			tm.put("out_trade_no", sdf.format(new Date()));// 商品订单号,自定义生成，保证唯一
			tm.put("nonce_str", nonce_str);// 随机字符串
			tm.put("total_fee", pd.getString("total_fee"));// 标价金额，单位是分
			tm.put("spbill_create_ip", spbill_create_ip);// 终端IP,支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
			tm.put("notify_url", notify_url);// 通知地址,异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
			tm.put("trade_type", trade_type);// 交易类型,小程序固定JSAPI
			tm.put("openid", pd.getString("openid"));// 用户标识
			// 把tm拼接字符串
			String str = tm.toString().substring(1, tm.toString().length() - 1).replaceAll(", ", "&");
			// 拼秘钥，
			str = str + "&key=" + key;
			System.out.println("str=" + str);
			// MD5加密，签名
			String sign = MD5Util.md5Encrypt32Upper(str);// 这里是第一次签名，用于调用统一下单接口
			System.out.println("第一次签名=" + sign);
			tm.put("sign", sign);// 签名
			// tm.put("key", "192006250b4c09247ec02edce69f6a2d");// key为商户平台设置的密钥key
			// map转成要发送的xml
			String sendXml = MapToXMLString.converter(tm).replaceAll("xmlroot", "xml").replaceAll("\\s+", "");
			System.out.println("sendXml=" + sendXml);
			// 调用统一下单接口，并接受返回的结果
			URL url = new URL("https://api.mch.weixin.qq.com/pay/unifiedorder");
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			con.getOutputStream().write(sendXml.getBytes());
			con.getOutputStream().flush();
			con.getOutputStream().close();
			BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf-8"));
			String s;
			// 收到的xml，主要为了预支付id，prepay_id
			String receiveXml = "";
			while ((s = br.readLine()) != null) {
				receiveXml += s;
			}
			br.close();
			con.getInputStream().close();
			System.out.println("receiveXml=" + receiveXml);
//			PayParam p = new PayParam();
//			p.setNonceStr(UuidUtil.get32UUID());
//			int start = receiveXml.indexOf("<prepay_id><![CDATA[") + "<prepay_id><![CDATA[".length();
//			int end = receiveXml.indexOf("]", start);
//			p.setPackAge("prepay_id=" + receiveXml.substring(start, end));// 预支付id
//			p.setTimeStamp(Long.toString(new Date().getTime()));
//			// 要发送给小程序端的东西
//			TreeMap<Object, Object> clientTm = new TreeMap<Object, Object>();
//			clientTm.put("nonceStr", p.getNonceStr());
//			clientTm.put("package", p.getPackAge());
//			clientTm.put("timeStamp", p.getTimeStamp());
//			String clientStr = clientTm.toString().substring(1, clientTm.toString().length() - 1).replaceAll(", ", "&");
//			clientStr = clientStr + "&key=1968a912403e42729e316132f87ca5e5";
//			String clientSign = MD5Util.md5Encrypt32Upper(clientStr);// 再次签名，这个签名用于小程序端调用wx.requesetPayment方法
//			p.setPaySign(clientSign);
//			System.out.println("p=" + p);
			// 将解析结果存储在HashMap中
			Map map = PayUtil.doXMLParse(receiveXml);

			String return_code = (String) map.get("return_code");// 返回状态码
			String result_code = (String) map.get("result_code");// 返回状态码

			PageData p = new PageData();// 返回给小程序端需要的参数
			if ("SUCCESS".equals(return_code)) {// (return_code == "SUCCESS" && return_code.equals(result_code))
				String prepay_id = (String) map.get("prepay_id");// 返回的预付单信息
				p.put("nonceStr", nonce_str);
				p.put("package", "prepay_id=" + prepay_id);
				Long timeStamp = System.currentTimeMillis() / 1000;
				p.put("timeStamp", timeStamp + "");// 这边要将返回的时间戳转化成字符串，不然小程序端调用wx.requestPayment方法会报签名错误
				// 拼接签名需要的参数
				String stringSignTemp = "appId=" + Appid + "&nonceStr=" + nonce_str + "&package=prepay_id=" + prepay_id
						+ "&signType=MD5&timeStamp=" + timeStamp;
				// 再次签名，这个签名用于小程序端调用wx.requesetPayment方法
//				String paySign = PayUtil.sign(stringSignTemp, "1968a912403e42729e316132f87ca5e5", "utf-8")
//						.toUpperCase();8E7298958CB0E5AFE5A586B507DA1FF8
				String paySign = MD5Util.md5Encrypt32Upper(stringSignTemp + key);
				p.put("paySign", paySign);
			}
			System.out.println("p=" + p);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
//p=PayParam [timeStamp=1577159593014, nonceStr=d23ae41a42a14ac2ac19f6da6a3603b2, packAge=prepay_id=![CDATA[SUCCESS, paySign=1D1CEBAC9EE18CECAC89F540B81070ED]