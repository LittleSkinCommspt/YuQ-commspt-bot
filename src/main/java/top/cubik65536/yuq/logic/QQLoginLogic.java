package top.cubik65536.yuq.logic;

import com.IceCreamQAQ.Yu.annotation.AutoBind;
import top.cubik65536.yuq.entity.QQLoginEntity;
import top.cubik65536.yuq.pojo.GroupMember;
import top.cubik65536.yuq.pojo.Result;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@SuppressWarnings("UnusedReturnValue")
@AutoBind
public interface QQLoginLogic {
    List<Map<String, String>> getGroupMsgList(QQLoginEntity qqLoginEntity) throws IOException;
    String operatingGroupMsg(QQLoginEntity qqLoginEntity, String type, Map<String, String> map, String refuseMsg) throws IOException;
}
