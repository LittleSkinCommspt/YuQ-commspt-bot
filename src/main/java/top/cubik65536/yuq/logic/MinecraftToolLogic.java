package top.cubik65536.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import com.icecreamqaq.yuq.message.Message;

/**
 * MinecraftToolLogic
 * top.cubik65536.yuq.logic
 * yuq-commspt-bot
 * <p>
 * Created by Cubik65536 on 2021-04-30.
 * Copyright © 2020-2021 Cubik Inc. All rights reserved.
 * <p>
 * Description:
 * History:
 * 1. 2021-04-30 [Cubik65536]: Create file MinecraftToolLogic;
 */

@AutoBind
public interface MinecraftToolLogic {
    public Message getTexturePreview(String hash) throws Exception;
    public String getClfcslLatest() throws Exception;
    public String getYggLatest() throws Exception;
    public String getCslLatest() throws Exception;
    public String[] getCsl(String playerName) throws Exception;
}
