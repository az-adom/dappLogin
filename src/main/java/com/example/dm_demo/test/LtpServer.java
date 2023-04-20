package com.example.dm_demo.test;

import com.alibaba.fastjson.JSONObject;
import com.ospn.command.CmdDappLogin;
import com.ospn.command.CmdReDappLogin;
import com.ospn.common.OsnServer;
import com.ospn.core.ILTPEvent;
import com.ospn.core.LTPData;
import com.ospn.data.DappData;
import io.netty.channel.ChannelHandlerContext;

import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.ospn.utils.CryptUtils.makeMessage;
import static com.ospn.utils.CryptUtils.takeMessage;

@Service
public class LtpServer extends OsnServer implements ILTPEvent {



    public static LTPData ltpData;

    public static String custormer;

    static RunnerThread runner;

    private ConcurrentHashMap<String, List<String>> pushDataMap = new ConcurrentHashMap<>();


    //public static String robotId = "OSNU6ng2NDGxynjQPvLfuRKcTYzkveSZaCSErj8zWcsDPaKEKYJ";
    //public static String robotKey = "VK0MHcCAQEEIC9fPiYrq8A7oGMMCTSkJvrmRvXz0RxVjJp4Nw5ZAskYoAoGCCqGSM49AwEHoUQDQgAEk09f4tJagEIn6Jp7Fxmrvaw4MBPU2I7jwZWijbHeD+fN4l7mBIR6NTFNzCndIgBXnJmyg4CA6Nmyzb/Vw4clPA==";

    public LtpServer(){
        //wait_commandService = svc;
        init();
    }

    void init(){

        if (ltpData != null){
            return;
        }
        ltpData = new LTPData();

        try {

            InputStream in = new FileInputStream("ospn.properties");
            Properties prop = new Properties();
            prop.load(in);

            custormer = prop.getProperty("customer");
            System.out.println("[custormer]" + custormer);

            ltpData.init(prop, this);
            in.close();

            prop.put("appID", ltpData.apps.osnID);
            prop.put("appKey", ltpData.apps.osnKey);

            FileOutputStream oFile = new FileOutputStream("ospn.properties", false);
            prop.store(oFile, "Comments");

            oFile.close();

        }catch (Exception e){
            System.out.println(e.getMessage());
        }

    }

    public String getAppId(){
        if (ltpData == null){
            init();
        }
        if (ltpData == null){
            return null;
        }
        return ltpData.apps.osnID;
    }

    public void getUserInfo(String osnID) {
        JSONObject json = new JSONObject();
        json.put("command", "GetUserInfo");
        json.put("from", this.getAppId());
        json.put("to", osnID);

        ltpData.sendJson(json);
    }


    public JSONObject createDappInfo(String user) {
        DappData dappData = new DappData();
        dappData.osnID = getAppId();
        dappData.name = ltpData.dappInfo.name;

        dappData.displayName = ltpData.dappInfo.displayName;
        System.out.println("displayName:-----------"+ltpData.dappInfo.displayName);
        dappData.portrait = ltpData.dappInfo.portrait;
        dappData.theme = ltpData.dappInfo.theme;
        dappData.themeLogo = ltpData.dappInfo.themeLogo;
        String url = ltpData.dappInfo.url;
        if (!url.contains("?")) {
            url+="?shareId="+user;
        } else {
            url+="&shareId="+user;
        }
        dappData.url = url;

        return dappData.toClientJsonNoParam(ltpData.apps.osnKey);
    }

    /**
     * 分享按钮获取的数据
     * @param json 前端需要获取的参数
     * @return
     */
    public JSONObject shareDapp(JSONObject json) {

        String tmp = "";

        for (Map.Entry<String, Object> entry : json.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            String tmp2 = key + "=" + value;
            if (tmp.length() > 0) {
                tmp += "&";
            }
            tmp = tmp + tmp2;
        }


        DappData dappData = new DappData();
        dappData.osnID = ltpData.dappInfo.osnID;
        dappData.url = ltpData.dappInfo.url;
        dappData.name = ltpData.dappInfo.name;
        if (dappData.url.contains("?")) {
            dappData.url += "&" + tmp;
        } else {
            dappData.url += "?" + tmp;
        }
        dappData.portrait = ltpData.dappInfo.portrait;

        return dappData.toClientJsonNoParam(ltpData.apps.osnKey);
    }


    public boolean login(CmdDappLogin cmd){
        CmdReDappLogin cmdRe = ltpData.login(cmd);

        if (cmdRe.errCode == null){
            return false;
        }
        if (!cmdRe.errCode.equalsIgnoreCase("0:success")){
            return false;
        }

        return true;
    }

    public String getDappInfo(){
        JSONObject content = ltpData.dappInfo.toClientJsonNoParam(ltpData.apps.osnKey);
        if (content == null){
            return "";
        }
        return content.toString();
    }

    public void push(SendCommandInfo data){
        if (runner == null) {
            runner = new RunnerThread();
            runner.start();
        }
        runner.push(data);
    }



    @Override
    public void handleMessage(ChannelHandlerContext channelHandlerContext, JSONObject jsonObject) {

        //System.out.println("[LtpServer::handleMessage] begin. " + jsonObject);

        System.out.println("----------------handleMessage---------------------:"+jsonObject);
        // 处理收到的消息
        if (jsonObject == null){
            return;
        }


        String command = jsonObject.getString("command");
        String to = jsonObject.getString("to");
        if (command == null || to == null){
            return;
        }

        if (!to.equalsIgnoreCase(ltpData.apps.osnID)){
            System.out.println("[LtpServer::handleMessage] error, to is " + to);
            return;
        }


        if(command.equalsIgnoreCase("Message")){

            JSONObject data = takeMessage(jsonObject);
            if(data == null) {
                return;
            }
            String command2 = data.getString("command");
            if (command2 != null) {
                if (command2.equalsIgnoreCase("PushInfo")) {

                    String user = data.getString("user");
                    String type = data.getString("type");

                    List<String> cmdList = pushDataMap.get(user);
                    if (cmdList == null) {
                        cmdList = new ArrayList<>();
                        pushDataMap.put(user, cmdList);
                    }
                    cmdList.add(type);

                    return;
                }
            }


            String commandID = data.getString("commandID");
            if (commandID == null){
                return;
            }
            System.out.println("commandID : " + commandID);
            String result = data.getString("result");
            if (result == null){
                return;
            }



        } else if (command.equalsIgnoreCase("GetServiceInfo")){

            String from = jsonObject.getString("from");
            if (from == null){
                return;
            }

            JSONObject content = ltpData.dappInfo.toClientJson(ltpData.apps.osnKey);

            System.out.println("[LtpServer::HandleMessage] dapp content : " + content);

            JSONObject original = new JSONObject();
            original.put("id", jsonObject.getString("id"));

            System.out.println("[LtpServer::HandleMessage] original id : " + original);

            JSONObject returnData = makeMessage(
                    "ServiceInfo",
                    ltpData.apps.osnID,
                    from,
                    content,
                    ltpData.apps.osnKey,
                    original);

            ltpData.sendJson(returnData);

        } else if (command.equalsIgnoreCase("UserInfo")) {
            //TODO update to user table
            handleUserInfo(jsonObject);
        }


    }

    public void handleUserInfo(JSONObject json){
        json  = JSONObject.parseObject(json.getString("content"));
        /**
         *  TODO : 这里处理接收到的user info 信息
         *  "userID" : osn id
         *  "displayName" : 昵称
         *  "describes"
         *  "portrait" : 头像url
         *  "urlSpace" :
//         * **/
//
//        String displayName = json.getString("displayName");
//        String portrait = json.getString("portrait");
//        String osnId = json.getString("userID");
//        UserEntity sysUser = userService.getOne(new LambdaQueryWrapper<UserEntity>().eq(UserEntity::getOsnId, osnId));
//        if (sysUser == null){
//            return;
//        }
//        sysUser.setHeadImg(portrait);
//        sysUser.setNickName(displayName);
//        userService.updateById(sysUser);
//        user.setId(json.getString("userID"));
////        System.out.println("---fjasdklfjsdjfklasldk"+json.toJSONString());
//        String nft = json.getString("describes");
//        user.setNft(StringUtils.isEmpty(nft) ? "-" : nft);
//        user.setNickname(displayName);
//        user.setFaceImage(portrait);
//        userMapper.updateByPrimaryKeySelective(user);


    }

    public void pushWorker(){

        ConcurrentHashMap<String, List<String>> tempPushMap = pushDataMap;
        System.out.println("push data map size : " + tempPushMap.size());

        pushDataMap = new ConcurrentHashMap<>();

        System.out.println("push data map size : " + tempPushMap.size());
        Iterator<ConcurrentHashMap.Entry<String, List<String>>> iter = tempPushMap.entrySet().iterator();

        while (iter.hasNext()) {
            ConcurrentHashMap.Entry<String, List<String>> entry = iter.next();
           // pushMessage(entry.getKey(), entry.getValue());
        }

    }

    static byte[] ToMD5(String str) {
        // 加密后的16进制字符串
        String hexStr = "";
        try {
            // 此 MessageDigest 类为应用程序提供信息摘要算法的功能
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            // 转换为MD5码
            byte[] digest = md5.digest(str.getBytes("utf-8"));
            return digest;
            //hexStr = ByteUtils.toHexString(digest);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static String genAlias(String user) {
        String alias = Base64.getUrlEncoder().encodeToString(ToMD5(user));
        return alias;
    }




   // @Data
    class SendCommandInfo{
        String command;
        String groupID;
        JSONObject json;
        long createTime;

        public SendCommandInfo(String command, String to, JSONObject json){
            this.command = command;
            this.groupID = to;
            this.json = json;
            createTime = System.currentTimeMillis();
        }

    }

    class RunnerThread extends Thread {

        final ConcurrentLinkedQueue<SendCommandInfo> dataList = new ConcurrentLinkedQueue<>();

        public RunnerThread(){
        }

        public void push(SendCommandInfo data) {

            dataList.offer(data);
            synchronized (dataList){
                dataList.notify();
            }
        }

        @Override
        public void run(){

            while(true){
                try{
                    if(dataList.isEmpty()){
                        synchronized (dataList){
                            dataList.wait();
                        }
                    }
                    SendCommandInfo data = dataList.poll();

//                    try {
//                        long createTime = data.getCreateTime();
//                        if (createTime + 5000 > System.currentTimeMillis()) {
//                            Thread.sleep(5000);
//                        }
//
//                        if (ltpData != null){
//                            ltpData.sendJson(data.getCommand(), data.getGroupID(), data.getJson());
//                        }
//
//                    } catch (Exception e) {
//
//                    }

                }catch (Exception e){
                    //logError(e);
                }
            }
        }

    }

}
