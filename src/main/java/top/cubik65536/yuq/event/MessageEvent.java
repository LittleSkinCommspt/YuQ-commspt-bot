package top.cubik65536.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.IceCreamQAQ.Yu.cache.EhcacheHelp;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.event.*;
import com.icecreamqaq.yuq.message.*;
import top.cubik65536.yuq.entity.GroupEntity;
import top.cubik65536.yuq.entity.QQEntity;
import top.cubik65536.yuq.service.ConfigService;
import top.cubik65536.yuq.service.GroupService;
import top.cubik65536.yuq.service.QQService;
import top.cubik65536.yuq.utils.BotUtils;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@EventListener
@SuppressWarnings("unused")
public class MessageEvent {
    @Inject
    private GroupService groupService;
    @Inject
    private QQService qqService;
    @Inject
    private ConfigService configService;
    @Inject
    @Named("CommandCountOnTime")
    public EhcacheHelp<Integer> eh;

    private final Map<Long, JSONArray> lastMessage = new ConcurrentHashMap<>();
    private final Map<Long, Long> lastQQ = new ConcurrentHashMap<>();
    private final Map<Long, JSONArray> lastRepeatMessage = new ConcurrentHashMap<>();

    @Event(weight = Event.Weight.high)
    public void status(com.IceCreamQAQ.Yu.event.events.Event e){
        Long group = null;
        Message message = null;
        if (e instanceof GroupMemberEvent){
            group = ((GroupMemberEvent) e).getGroup().getId();
        }else if (e instanceof GroupMemberRequestEvent){
            group = ((GroupMemberRequestEvent) e).getGroup().getId();
        }else if (e instanceof GroupRecallEvent){
            group = ((GroupRecallEvent) e).getGroup().getId();
        }else if (e instanceof GroupMessageEvent){
            GroupMessageEvent groupMessageEvent = (GroupMessageEvent) e;
            group = groupMessageEvent.getGroup().getId();
            message = groupMessageEvent.getMessage();
        }
        if (group == null) return;
        GroupEntity groupEntity = groupService.findByGroup(group);
        boolean status = true;
        if (message != null) {
            List<String> list = message.toPath();
            if (list.size() == 2) {
                String pa = list.get(1);
                if ("commspt-bot".equals(list.get(0)) && pa.equals("开") || pa.equals("关")) {
                    status = false;
                }
            }
        }
        if (groupEntity != null && groupEntity.getStatus()){
            status = false;
        }
        if (status){
            e.setCancel(true);
        }
    }

    @Event(weight = Event.Weight.low)
    public void repeat(GroupMessageEvent e){
        long group = e.getGroup().getId();
        GroupEntity groupEntity = groupService.findByGroup(group);
        Boolean repeat;
        if (groupEntity == null) repeat = false;
        else repeat = groupEntity.getRepeat();
        if (repeat == null) {
            repeat = false;
            groupEntity.setRepeat(false);
            groupService.save(groupEntity);
        }
        if (repeat) {
            long qq = e.getSender().getId();
            JSONArray nowJsonArray = BotUtils.messageToJsonArray(e.getMessage());
            if (lastMessage.containsKey(group)) {
//            synchronized (this) {
                JSONArray oldJsonArray = lastMessage.get(group);
                if (BotUtils.equalsMessageJsonArray(nowJsonArray, oldJsonArray) &&
                        !BotUtils.equalsMessageJsonArray(nowJsonArray, lastRepeatMessage.get(group))
                        && lastQQ.get(group) != qq) {
                    lastRepeatMessage.put(group, nowJsonArray);
                    e.getGroup().sendMessage(e.getMessage());
                }
//            }
            }
            lastMessage.put(group, nowJsonArray);
            lastQQ.put(group, qq);
        }
    }

    @Event
    public void qa(GroupMessageEvent e){
        GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
        if (groupEntity == null) return;
        Message message = e.getMessage();
        if (message.toPath().size() == 0) return;
        if ("草".equals(message.toPath().get(0)) || "cao".equals(message.toPath().get(0)) || "艹".equals(message.toPath().get(0))) {
            Boolean cao;
            if (groupEntity == null) cao = false;
            else cao = groupEntity.getCao();
            if (cao == null) {
                cao = false;
                groupEntity.setCao(false);
                groupService.save(groupEntity);
            }
            if (cao) e.getGroup().sendMessage(BotUtils.toMessage("草\u202e"));
            return;
        }
        if ("删问答".equals(message.toPath().get(0))) return;
        String str;
        try {
            str = Message.Companion.firstString(message);
        } catch (IllegalStateException ex){
            return;
        }
        JSONArray qaJsonArray = groupEntity.getQaJsonArray();
        for (int i = 0; i < qaJsonArray.size(); i++) {
            JSONObject jsonObject = qaJsonArray.getJSONObject(i);
            String type = jsonObject.getString("type");
            String q = jsonObject.getString("q");
            boolean status = false;
            if ("ALL".equals(type)){
                if (str.equals(q)) status = true;
            } else if (str.contains(jsonObject.getString("q"))) status = true;
            if (status){
                Integer maxCount = groupEntity.getMaxCommandCountOnTime();
                if (maxCount == null) maxCount = -1;
                if (maxCount > 0){
                    String key = "qq" + e.getSender().getId() + q;
                    Integer num = eh.get(key);
                    if (num == null) num = 0;
                    if (num >= maxCount) return;
                    eh.set(key, ++num);
                }
                JSONArray jsonArray = jsonObject.getJSONArray("a");
                e.getGroup().sendMessage(BotUtils.jsonArrayToMessage(jsonArray));
            }
        }
    }
}
