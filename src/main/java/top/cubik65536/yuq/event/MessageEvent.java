package top.cubik65536.yuq.event;

import com.IceCreamQAQ.Yu.annotation.Event;
import com.IceCreamQAQ.Yu.annotation.EventListener;
import com.IceCreamQAQ.Yu.cache.EhcacheHelp;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.GroupNoticeList;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.event.*;
import com.icecreamqaq.yuq.message.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import top.cubik65536.yuq.entity.ConfigEntity;
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
import java.util.regex.Pattern;

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
    public void inter(GroupMessageEvent e) throws IOException {
        GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
        if (groupEntity == null) return;
        if (groupEntity.getWhiteJsonArray().contains(String.valueOf(e.getSender().getId()))) return;
        Message message = e.getMessage();
        String str;
        try {
            str = message.toPath().get(0);
        }catch (IllegalStateException ex){
            str = null;
        }
        if (str != null){
            JSONArray interceptJsonArray = groupEntity.getInterceptJsonArray();
            for (int i = 0; i < interceptJsonArray.size(); i++){
                String intercept = interceptJsonArray.getString(i);
                if (str.contains(intercept)){
                    e.cancel = true;
                    break;
                }
            }
        }
        if (!e.getGroup().getBot().isAdmin()) return;
        QQEntity qqEntity = qqService.findByQQAndGroup(e.getSender().getId(), e.getGroup().getId());
        if (qqEntity == null) qqEntity = new QQEntity(e.getSender().getId(), groupEntity);
        JSONArray violationJsonArray = groupEntity.getViolationJsonArray();
        int code = 0;
        String vio = null;
        List<Image> images = new ArrayList<>();
        out:for (int i = 0; i < violationJsonArray.size(); i++){
            String violation = violationJsonArray.getString(i);
            String nameCard = e.getSender().getNameCard();
            if (nameCard.contains(violation)) {
                code = 3;
                vio = violation;
                break;
            }
            for (MessageItem item: message.getBody()){
                if (item instanceof Text){
                    Text text = (Text) item;
                    if (text.getText().contains(violation)) code = 1;
                } else if (item instanceof XmlEx){
                    XmlEx xmlEx = (XmlEx) item;
                    if (xmlEx.getValue().contains(violation)) code = 1;
                } else if (item instanceof JsonEx){
                    JsonEx jsonEx = (JsonEx) item;
                    if (jsonEx.getValue().contains(violation)) code = 1;
                }
                if (code != 0){
                    vio = violation;
                    break out;
                }
            }
        }
        if (code != 0){
            message.recall();
            e.getGroup().sendMessage(FunKt.getMif().text("已监测到违规词并进行撤回！").toMessage());
        }
    }

    @Event
    public void qa(GroupMessageEvent e){
        GroupEntity groupEntity = groupService.findByGroup(e.getGroup().getId());
        if (groupEntity == null) return;
        Message message = e.getMessage();
        if (message.toPath().size() == 0) return;
        String messageStr = message.toPath().get(0);
        if ("草".equals(messageStr) || "cao".equals(messageStr) || "艹".equals(messageStr)) {
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
        if (isAskingWhy(messageStr)) {
            ConfigEntity configEntity = configService.findByType("notificationGroup");
            if (configEntity != null) {
                long notificationGroupQQ = Long.parseLong(configEntity.getContent());
                Group notificationGroup = FunKt.getYuq().getGroups().get(notificationGroupQQ);
                notificationGroup.sendMessage(FunKt.getMif().text(messageStr).toMessage());
                notificationGroup.sendMessage(FunKt.getMif().text("现在有一个新的问题被提出，请各位尽快处理。").toMessage());
                return;
            }
        }
        if ("删问答".equals(messageStr)) return;
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

    private static final Pattern
            rx1 = Pattern.compile("^为什么.*"),
            rx2 = Pattern.compile("^为啥.*"),
            rx3 = Pattern.compile("^问个问题.*"),
            rx4 = Pattern.compile("^请问.*"),
            rx5 = Pattern.compile("^问一下.*"),
            rx6 = Pattern.compile("^求助一下.*"),
            rx7 = Pattern.compile("^如何解决.*"),
            rx8 = Pattern.compile("^我想问问.*"),
            rx9 = Pattern.compile("^这是什么问题.*"),
            rx10 = Pattern.compile("^这是咋回事.*"),
            rx11 = Pattern.compile("^怎么办.*"),
            rx12 = Pattern.compile("^怎么解决.*");

    private boolean isAskingWhy(String message) {
        return rx1.matcher(message).matches() || rx2.matcher(message).matches() ||
                rx3.matcher(message).matches() || rx4.matcher(message).matches() ||
                rx5.matcher(message).matches() || rx6.matcher(message).matches() ||
                rx7.matcher(message).matches() || rx8.matcher(message).matches() ||
                rx9.matcher(message).matches() || rx10.matcher(message).matches() ||
                rx11.matcher(message).matches() || rx12.matcher(message).matches();
    }

}
