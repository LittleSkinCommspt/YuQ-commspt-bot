package top.cubik65536.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.IceCreamQAQ.Yu.annotation.Synonym;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.annotation.PathVar;
import com.icecreamqaq.yuq.annotation.QMsg;
import com.icecreamqaq.yuq.controller.ContextSession;
import com.icecreamqaq.yuq.entity.Group;
import com.icecreamqaq.yuq.entity.Member;
import com.icecreamqaq.yuq.message.At;
import com.icecreamqaq.yuq.message.Message;
import com.icecreamqaq.yuq.message.MessageItemFactory;
import top.cubik65536.yuq.entity.GroupEntity;
import top.cubik65536.yuq.entity.QQEntity;
import top.cubik65536.yuq.entity.QQLoginEntity;
import top.cubik65536.yuq.entity.RecallEntity;
import top.cubik65536.yuq.logic.*;
import top.cubik65536.yuq.service.GroupService;
import top.cubik65536.yuq.service.QQService;
import top.cubik65536.yuq.service.RecallService;
import top.cubik65536.yuq.utils.BotUtils;
import net.mamoe.mirai.contact.PermissionDeniedException;

import javax.inject.Inject;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@GroupController
@SuppressWarnings("unused")
public class ManageController {

    @Inject
    private GroupService groupService;
    @Config("YuQ.Mirai.bot.master")
    private String master;

    @Before
    public GroupEntity before(long group, long qq){
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) groupEntity = new GroupEntity();
        if (String.valueOf(qq).equals(master)) return groupEntity;
        else throw FunKt.getMif().at(qq).plus("抱歉，您的权限不足，无法执行！！").toThrowable();
    }

    @Action("加管 {qqNum}")
    @Synonym({"加超管 {qqNum}"})
    @QMsg(at = true)
    public String addManager(GroupEntity groupEntity, @PathVar(0) String type, Long qqNum){
        switch (type){
            case "加管":
                groupEntity.setAdminJsonArray(groupEntity.getAdminJsonArray().fluentAdd(qqNum.toString()));
                break;
            case "加超管":
                groupEntity.setSuperAdminJsonArray(groupEntity.getSuperAdminJsonArray().fluentAdd(qqNum.toString()));
                break;
            default: return null;
        }
        groupService.save(groupEntity);
        return type + "成功！！";
    }

    @Action("删管 {qqNum}")
    @Synonym({"删超管 {qqNum}"})
    @QMsg(at = true)
    public String delManager(GroupEntity groupEntity, @PathVar(0) String type, Long qqNum){
        switch (type){
            case "删管":
                JSONArray adminJsonArray = groupEntity.getAdminJsonArray();
                BotUtils.delManager(adminJsonArray, qqNum.toString());
                groupEntity.setAdminJsonArray(adminJsonArray);
                break;
            case "删超管":
                JSONArray superAdminJsonArray = groupEntity.getSuperAdminJsonArray();
                BotUtils.delManager(superAdminJsonArray, qqNum.toString());
                groupEntity.setSuperAdminJsonArray(superAdminJsonArray);
                break;
            default: return null;
        }
        groupService.save(groupEntity);
        return type + "成功！！";
    }

    @Action("群管理权限 {status}")
    @QMsg(at = true)
    public String groupAdminAuth(GroupEntity groupEntity, boolean status){
        groupEntity.setGroupAdminAuth(status);
        groupService.save(groupEntity);
        if (status) return "群管理权限开启成功！！";
        else return "群管理权限关闭成功！！";
    }

    @GroupController
    public static class ManageAdminController {
        @Config("YuQ.Mirai.bot.master")
        private String master;
        @Inject
        private GroupService groupService;

        @Before
        public GroupEntity before(Member qq, long group){
            GroupEntity groupEntity = groupService.findByGroup(group);
            if (groupEntity == null) groupEntity = new GroupEntity(group);
            if (groupEntity.isAdmin(qq.getId()) || groupEntity.isSuperAdmin(qq.getId())
                    || qq.getId() == Long.parseLong(master) || (qq.isAdmin() && Boolean.valueOf(true).equals(groupEntity.getGroupAdminAuth()))){
                return groupEntity;
            }else throw FunKt.getMif().at(qq).plus("您的权限不足，无法执行！！").toThrowable();
        }

        @Action("清屏")
        public String clear(){
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 1000; i++) sb.append("\n");
            return sb.toString();
        }

        @Action("禁言")
        @QMsg(at = true)
        public String ban(long group, @PathVar(1) String paramQQ, @PathVar(2) String timeStr){
            int time;
            if (timeStr == null) time = 0;
            else {
                if (timeStr.length() == 1) return "未发现时间单位！！单位可为（s,m）";
                int num = Integer.parseInt(timeStr.substring(0, timeStr.length() - 1));
                switch (timeStr.charAt(timeStr.length() - 1)){
                    case 's': time = num; break;
                    case 'm':
                        if (30 < num) {
                            return "禁言时间过长（最大30分钟）";
                        }
                        time = num * 60; break;
                    default: return "禁言时间格式不正确";
                }
            }
            if (!paramQQ.matches("^[0-9][0-9]*[0-9]$"))
                return "您输入的不为qq号，请重试！！";
            FunKt.getYuq().getGroups().get(group).get(Long.parseLong(paramQQ)).ban(time);
            return "禁言成功！！";
        }

        @Action("解除禁言")
        @QMsg(at = true)
        public String unban(long group, @PathVar(1) String paramQQ){
            if (!paramQQ.matches("^[0-9][0-9]*[0-9]$"))
                return "您输入的不为qq号，请重试！！";
            FunKt.getYuq().getGroups().get(group).get(Long.parseLong(paramQQ)).unBan();
            return "解除禁言成功！！";
        }

        @Action("commspt-bot {status}")
        @Synonym({"复读 {status}", "稻草人 {status}"})
        @QMsg(at = true)
        public String onOrOff(GroupEntity groupEntity, boolean status, @PathVar(0) String op){
            switch (op){
                case "commspt-bot": groupEntity.setStatus(status); break;
                case "复读": groupEntity.setRepeat(status); break;
                case "稻草人": groupEntity.setCao(status); break;
                default: return null;
            }
            groupService.save(groupEntity);
            if (status) return "\n" + op + "开启成功";
            else return "\n" + op + "关闭成功";
        }
    }

    @GroupController
    public static class ManageSuperAdminController {
        @Config("YuQ.Mirai.bot.master")
        private String master;
        @Inject
        private GroupService groupService;
        @Inject
        private QQService qqService;
        @Inject
        private BotLogic botLogic;
        @Inject
        private QQLoginLogic qqLoginLogic;
        @Inject
        private QQGroupLogic qqGroupLogic;

        @Before
        public GroupEntity before(long group, Member qq){
            GroupEntity groupEntity = groupService.findByGroup(group);
            if (String.valueOf(qq.getId()).equals(master) || groupEntity.isSuperAdmin(qq.getId()) ||
                    (qq.isAdmin() && Boolean.valueOf(true).equals(groupEntity.getGroupAdminAuth()))) return groupEntity;
            else throw FunKt.getMif().at(qq).plus("您的权限不足，无法执行！！").toThrowable();
        }

        @Action("加违规词 {content}")
        @QMsg(at = true)
        public String add(GroupEntity groupEntity, @PathVar(0) String type, String content, ContextSession session, long qq) throws IOException {
            switch (type){
                case "加违规词":
                    groupEntity.setViolationJsonArray(groupEntity.getViolationJsonArray().fluentAdd(content));
                    break;
                default: return null;
            }
            groupService.save(groupEntity);
            return type + "成功";
        }

        @Action("删违规词 {content}")
        @QMsg(at = true)
        public String del(GroupEntity groupEntity, @PathVar(0) String type, String content){
            switch (type){
                case "删违规词":
                    JSONArray violationJsonArray = groupEntity.getViolationJsonArray();
                    BotUtils.delManager(violationJsonArray, content);
                    groupEntity.setViolationJsonArray(violationJsonArray);
                    break;
                default: return null;
            }
            groupService.save(groupEntity);
            return type + "成功！！";
        }

        @Action("全体禁言 {status}")
        public String allShutUp(long group, boolean status) throws IOException {
            QQLoginEntity qqLoginEntity = botLogic.getQQLoginEntity();
            return qqLoginLogic.allShutUp(qqLoginEntity, group, status);
        }

        @Action("t {qqNo}")
        @QMsg(at = true)
        public String kick(Member qqNo) throws IOException {
            try {
                qqNo.kick("");
                return "踢出成功！！";
            } catch (PermissionDeniedException e) {
                return "权限不足，踢出失败！！";
            } catch (Exception e){
                return qqGroupLogic.deleteGroupMember(botLogic.getQQLoginEntity(), qqNo.getId(), qqNo.getGroup().getId(), true);
            }
        }

        @Action("违规次数 {count}")
        @QMsg(at = true)
        public String maxViolationCount(GroupEntity groupEntity, int count){
            groupEntity.setMaxViolationCount(count);
            groupService.save(groupEntity);
            return "已设置本群最大违规次数为" + count + "次";
        }

        @Action("清除违规 {qqNum}")
        public String clear(GroupEntity groupEntity, long qq){
            QQEntity qqEntity = qqService.findByQQAndGroup(qq, groupEntity.getGroup());
            if (qqEntity == null) qqEntity = new QQEntity(qq, groupEntity);
            qqEntity.setViolationCount(0);
            qqService.save(qqEntity);
            return "清除违规成功！！";
        }

        @Action("指令限制 {count}")
        @QMsg(at = true)
        public String maxCommandCount(GroupEntity groupEntity, int count){
            groupEntity.setMaxCommandCountOnTime(count);
            groupService.save(groupEntity);
            return "已设置本群单个指令每人十分钟最大触发次数为" + count + "次";
        }

        @Action("加指令限制 {command} {count}")
        @QMsg(at = true)
        public String addCommandLimit(GroupEntity groupEntity, String command, int count){
            JSONObject jsonObject = groupEntity.getCommandLimitJsonObject();
            jsonObject.put(command, count);
            groupEntity.setCommandLimitJsonObject(jsonObject);
            groupService.save(groupEntity);
            return "加指令限制成功！！已设置指令{" + command + "}十分钟之内只会响应" + count + "次";
        }

        @Action("删指令限制 {command}")
        @QMsg(at = true)
        public String delCommandLimit(GroupEntity groupEntity, String command){
            JSONObject jsonObject = groupEntity.getCommandLimitJsonObject();
            jsonObject.remove(command);
            groupEntity.setCommandLimitJsonObject(jsonObject);
            groupService.save(groupEntity);
            return "删指令{" + command + "}限制成功！！";
        }

        @Action("加问答 {q}")
        @QMsg(at = true)
        public String qa(ContextSession session, long qq, GroupEntity groupEntity, String q, Group group, @PathVar(2) String type){
            MessageItemFactory mif = FunKt.getMif();
            group.sendMessage(mif.at(qq).plus("请输入回答语句！！"));
            Message a = session.waitNextMessage();
            JSONObject jsonObject = new JSONObject();
            JSONArray aJsonArray = BotUtils.messageToJsonArray(a);
            jsonObject.put("q", q);
            jsonObject.put("a", aJsonArray);
            if (type == null) type = "PARTIAL";
            if (!"ALL".equalsIgnoreCase(type)) type = "PARTIAL";
            else type = "ALL";
            jsonObject.put("type", type);
            JSONArray jsonArray = groupEntity.getQaJsonArray();
            jsonArray.add(jsonObject);
            groupEntity.setQaJsonArray(jsonArray);
            groupService.save(groupEntity);
            return "添加问答成功！！";
        }

        @Action("删问答 {q}")
        @QMsg(at = true)
        public String delQa(GroupEntity groupEntity, String q){
            JSONArray qaJsonArray = groupEntity.getQaJsonArray();
            List<JSONObject> delList = new ArrayList<>();
            for (int i = 0; i < qaJsonArray.size(); i++){
                JSONObject jsonObject = qaJsonArray.getJSONObject(i);
                if (q.equals(jsonObject.getString("q"))){
                    delList.add(jsonObject);
                }
            }
            delList.forEach(qaJsonArray::remove);
            groupEntity.setQaJsonArray(qaJsonArray);
            groupService.save(groupEntity);
            return "删除问答成功！！";
        }
    }

    @GroupController
    public static class ManageNotController {
        @Inject
        private GroupService groupService;
        @Inject
        private RecallService recallService;
        @Config("YuQ.Mirai.bot.version")
        private String version;

        @Before
        public GroupEntity before(Long group){
            GroupEntity groupEntity = groupService.findByGroup(group);
            if (groupEntity == null) groupEntity = new GroupEntity(group);
            return groupEntity;
        }

        @Action("查管")
        @Synonym({"查黑名单", "查白名单", "查违规词", "查拦截", "查微博监控", "查哔哩哔哩监控", "查问答", "查超管", "查指令限制", "查shell"})
        @QMsg(at = true, atNewLine = true)
        public String query(GroupEntity groupEntity, @PathVar(0) String type){
            StringBuilder sb = new StringBuilder();
            switch (type){
                case "查管":
                    sb.append("本群管理员列表如下：").append("\n");
                    groupEntity.getAdminJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                    break;
                case "查超管":
                    sb.append("本群超级管理员列表如下").append("\n");
                    groupEntity.getSuperAdminJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                    break;
                case "查黑名单":
                    sb.append("本群黑名单列表如下：").append("\n");
                    groupEntity.getBlackJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                    break;
                case "查白名单":
                    sb.append("本群白名单列表如下：").append("\n");
                    groupEntity.getWhiteJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                    break;
                case "查违规词":
                    sb.append("本群违规词列表如下：").append("\n");
                    groupEntity.getViolationJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                    break;
                case "查拦截":
                    sb.append("本群被拦截的指令列表如下：").append("\n");
                    groupEntity.getInterceptJsonArray().forEach(obj -> sb.append(obj).append("\n"));
                    break;
                case "查问答":
                    sb.append("本群问答列表如下：").append("\n");
                    groupEntity.getQaJsonArray().forEach(obj -> {
                        JSONObject jsonObject = (JSONObject) obj;
                        sb.append(jsonObject.getString("q")).append("\n");
                    });
                    break;
                case "查指令限制":
                    sb.append("本群的指令限制列表如下：").append("\n");
                    groupEntity.getCommandLimitJsonObject().forEach((k, v) ->
                            sb.append(k).append("->").append(v).append("次").append("\n"));
                    break;
                case "查shell":
                    sb.append("本群的shell命令存储如下").append("\n");
                    groupEntity.getShellCommandJsonArray().forEach(obj -> {
                        JSONObject shellCommandJsonObject = (JSONObject) obj;
                        sb.append(shellCommandJsonObject.getInteger("auth")).append("->")
                                .append(shellCommandJsonObject.getString("command")).append("->")
                                .append(shellCommandJsonObject.getString("shell"));
                    });
                    break;
                default: return null;
            }
            return sb.deleteCharAt(sb.length() - 1).toString();
        }

        @Action("查撤回 {qqNo}")
        public Message queryRecall(long group, long qqNo, long qq, @PathVar(value = 2, type = PathVar.Type.Integer) Integer num){
            List<RecallEntity> recallList = recallService.findByGroupAndQQ(group, qqNo);
            int all = recallList.size();
            if (num == null) num = 1;
            if (num > all || num < 0) return FunKt.getMif().at(qq).plus("您要查询的QQ只有" + all + "条撤回消息，超过范围了！！");
            RecallEntity recallEntity = recallList.get(num - 1);
            String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(recallEntity.getDate());
            return FunKt.getMif().at(qq).plus("\n该消息撤回时间为" + timeStr + "\n消息内容为：\n")
                    .plus(BotUtils.jsonArrayToMessage(recallEntity.getMessageEntity().getContentJsonArray()));
        }
    }

}
