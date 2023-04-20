package com.example.dm_demo.test;

import com.alibaba.fastjson.JSONObject;
import com.ospn.command.CmdReDappLogin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@Slf4j
public class Demo {
    /**-----------------------------------------------注意--------------------------------------------------------
     *                                    需要将sdk文件夹里面的jar引入的本地仓库
     *                                      执行指令：
     * mvn install:install-file -Dfile=D:/workspace/dm_demo/sdk/ospnBase.jar -DgroupId=ospn-old -DartifactId=ospn-old -Dversion=1.4 -Dpackaging=jar
     * 修改sdk里面的配置文件，只需要修改ospn.properties和start.sh即可
     * 项目打包出来后需要把libecSSL.so和opsnBase.jar和start.sh放在项目的同级目录，启动项目直接是执行命令: sh start.sh
     */

    @Autowired
    private LtpServer ltpServer;

    /**
     * 小程序登录
     * @param cmd
     * @return
     */
    @PostMapping("/dappLogin")
    public JSONObject dappLogin(@RequestBody DappRequest cmd) {
        log.info("小程序登录开始-------------------{}", cmd);

        JSONObject ret = null;
        if (cmd.command.equalsIgnoreCase("GetServerInfo")) {
            ret = ltpServer.ltpData.getServerInfo(cmd.getCmdGetServerInfo());
            return ret;
        } else if (cmd.command.equalsIgnoreCase("Login")) {

            CmdReDappLogin cmdRe = ltpServer.ltpData.login(cmd.getCmdDappLogin());

            if (cmdRe.errCode.equalsIgnoreCase("0:success")) {

                //登录成功，返回数据给前端
                String token = System.currentTimeMillis() + "";
                JSONObject tokenJson = new JSONObject();
                tokenJson.put("token", token);
                cmdRe.addSessionKey(cmd.user, tokenJson.toString().getBytes());
                System.out.println("------------------token-------------" + tokenJson.get("token"));
                return cmdRe.toJson();
            } else {
                ret.put("code", 500);
                return cmdRe.toJson();
            }
        }
        return ret;
    }

    /**
     * 分享需要的内容
     * @return
     */
    @GetMapping("/share")
    public JSONObject share() {
        //String osnId = RequestUtil.getMainUserId();

        JSONObject json = new JSONObject();
        json.put("test", "test");//可以put前端需要获取到的内容
        //json.put("invitationCode", /**这里填写你们体系里自己的邀请码**/);

        JSONObject ret = ltpServer.shareDapp(json);
        return ret;
    }

    //@ApiOperation(" dappInfo")

    /**
     * 集成到小程序的数据
     * @return
     */
    @GetMapping("dappinfo")
    public String getDappInifo(){
        String dappInfo = ltpServer.getDappInfo();
        return dappInfo;
    }
}
