package com.example.emqtdemo.controller;

import com.alibaba.fastjson.JSONObject;
import com.example.emqtdemo.emqt.EmqtConfig;
import org.fusesource.mqtt.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author yangxin
 * @date 2019/4/23
 */
@Controller
public class IndexController {

    @Autowired
    private HttpServletRequest request;

    @Resource
    private CallbackConnection callbackConnection;

    @Resource
    private EmqtConfig emqtConfig;

    @RequestMapping("/")
    public String index(Long liveId,String username){
        request.setAttribute("liveId", liveId);
        request.setAttribute("username", username);
        request.setAttribute("clientId","liveroom" + liveId + username);
        request.setAttribute("topic","/live_test/" + liveId);
        return "index";
    }

    @RequestMapping("/send")
    @ResponseBody
    public Object send(String topic, String clientId,String msg) {
        JSONObject content = new JSONObject();
        content.put("clientId", clientId);
        content.put("msg", msg);
        callbackConnection.publish(topic, content.toJSONString().getBytes(), QoS.EXACTLY_ONCE, false,new Callback<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

            }

            @Override
            public void onFailure(Throwable throwable) {

            }
        });
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("content", msg);
        return jsonObject;
    }

    @RequestMapping("/getOnlineCount")
    @ResponseBody
    public Object getOnlineCount(Long liveId) {
        int onlineCount = emqtConfig.getOnlineCount(liveId);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("success", true);
        jsonObject.put("content", onlineCount);
        return jsonObject;
    }


}