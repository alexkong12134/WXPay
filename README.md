# WXPay
微信支付接口
将文件导入工程项目（SSM框架）
前端（vue）通过request访问PayController/Pay
附带参数
"body": "商品描述（需转为utf-8编码 例如 \u8d26\u7684）",
"total_fee": "标价金额单位是分",
"openid": "用户标识"
请求成功后返回 预支付Id等信息，将信息封装进wx.requestPayment即可


例：
uni.request({
    url: that.$baseUrl + '/weixin/client/WXPay',
    header: {
      'content-type': 'application/x-www-form-urlencoded'
    },
    data: {
      "body": "wxpay",
      "total_fee": "888",
      "openid": "oDbmL5TES******OxbWWJDVE"
    },
    method: 'POST',
    dataType: 'JSON',
    success: function(res) {
      //请求后回调
      let msg = JSON.parse(res.data);
      wx.requestPayment({
        'timeStamp': msg.data.timeStamp,
        'nonceStr': msg.data.nonceStr,
        'package': msg.data.package,
        'signType': 'MD5',
        'paySign': msg.data.paySign,
        'success': function(res) {},
        'fail': function(res) {},
        'complete': function(res) {}
    },
)}

