<html>
<head>
    <title>title</title>
    <script src="/mqtt.js"></script>
    　
    <script src="/jquery.js"></script>
    <script src="/index.js"></script>
</head>

<body>
<div>
    <span>${username!}成功进入了${liveId!}直播间</span><br/>
    <span id="connectionTip"></span><br/>
    <span>当前在线人数：<span id="onlineCount">0</span></span><br/>

    <hr/>
    输入消息：<input name="content" placeholder="请输入消息"/>
    <button name="send" id="send">发送</button>
    <hr/>

    <div id="msgContent">

    </div>

    <button name="cancel" id="cancel">取消</button>

</div>
</body>
</html>

<script>
    var clientId = '${clientId!}';
    var username = '${username!}';
    var topic = '${topic!}';
    var liveId = '${liveId!}';
</script>