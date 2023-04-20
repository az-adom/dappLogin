package com.example.dm_demo.test;

import com.ospn.command.CmdDappLogin;
import com.ospn.command.CmdGetServerInfo;
import lombok.Data;

@Data
public class DappRequest {
    public String command;
    public String user;
    public String sign;
    public String hash;
    public String session;
    public long random;

    public CmdDappLogin getCmdDappLogin(){
        CmdDappLogin cmd = new CmdDappLogin();
        cmd.command = command;
        cmd.user = user;
        cmd.sign = sign;
        cmd.hash = hash;
        cmd.session = session;
        return cmd;
    }

    public CmdGetServerInfo getCmdGetServerInfo(){
        CmdGetServerInfo cmd = new CmdGetServerInfo();
        cmd.command = command;
        cmd.user = user;
        cmd.random = random;
        return cmd;
    }
}
