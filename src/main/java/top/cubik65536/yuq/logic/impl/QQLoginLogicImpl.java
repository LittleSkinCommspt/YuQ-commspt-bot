package top.cubik65536.yuq.logic.impl;

import com.alibaba.fastjson.JSONObject;
import top.cubik65536.yuq.entity.QQLoginEntity;
import top.cubik65536.yuq.logic.QQLoginLogic;
import top.cubik65536.yuq.utils.OkHttpUtils;
import okhttp3.FormBody;
import okhttp3.Headers;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;

@SuppressWarnings("unused")
public class QQLoginLogicImpl implements QQLoginLogic {
    @Override
    public String allShutUp(QQLoginEntity qqLoginEntity, Long group, Boolean isShutUp) throws IOException {
        long num = 0L;
        if (isShutUp) num = 4294967295L;
        Map<String, String> map = new HashMap<>();
        map.put("src", "qinfo_v3");
        map.put("gc", group.toString());
        map.put("bkn", qqLoginEntity.getGtk());
        map.put("all_shutup", String.valueOf(num));
        JSONObject jsonObject = OkHttpUtils.postJson("https://qinfo.clt.qq.com/cgi-bin/qun_info/set_group_shutup", map,
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        switch (jsonObject.getInteger("ec")){
            case 0:
                if (isShutUp) return "全体禁言成功！！";
                else return "解除全体禁言成功";
            case 7: return "权限不够，我无法执行！！";
            case -100005: return "群号不存在";
            case 4: return "执行失败，请更新QQ！！";
            default: return "执行失败，" + jsonObject.getString("em");
        }
    }

    @Override
    public List<Map<String, String>> getGroupMsgList(QQLoginEntity qqLoginEntity) throws IOException {
        String html = OkHttpUtils.getStr("https://web.qun.qq.com/cgi-bin/sys_msg/getmsg?ver=5761&filter=0&ep=0",
                OkHttpUtils.addCookie(qqLoginEntity.getCookie()));
        Elements elements = Jsoup.parse(html).getElementById("msg_con").getElementsByTag("dd");
        List<Map<String, String>> list = new ArrayList<>();
        out:for (Element ele: elements){
            Element ddEle = ele.getElementsByTag("dd").first();
            int isOp = 1;
            try {
                int opBtn = ddEle.getElementsByClass("btn_group").first().children().size();
                if (opBtn == 0) isOp = 0;
            }catch (NullPointerException e){
                isOp = 0;
            }
            int typeInt = Integer.parseInt(ele.attr("type"));
            String type;
            switch (typeInt){
                case 1: type = "apply"; break;
                case 2: type = "beInvite"; break;
                case 13: type = "leave"; break;
                case 22: type = "invite"; break;
                case 3: type = "addManager"; break;
                case 60: type = "payAdd"; break;
                default: continue out;
            }
            String seq = ddEle.attr("seq");
            String group = ddEle.attr("qid");
            String authKey = ddEle.attr("authKey");
            Elements liElements = ele.getElementsByTag("li");
            Element liEle = liElements.first();
            String msg = liEle.attr("aria-label");
            String qq = liEle.getElementsByTag("a").first().attr("uin");
            Map<String, String> map = new HashMap<>();
            map.put("seq", seq);
            map.put("group", group);
            map.put("authKey", authKey);
            map.put("msg", msg);
            map.put("qq", qq);
            map.put("type", type);
            map.put("isOp", String.valueOf(isOp));
            map.put("typeInt", String.valueOf(typeInt));
            if (liElements.size() != 1){
                Element secondLiEle = liElements.get(1);
                if (secondLiEle.getElementsByClass("apply_add_msg").size() == 0){
                    map.put("inviteMsg", secondLiEle.attr("aria-label"));
                    Element memberEle = secondLiEle.getElementsByTag("a").first();
                    map.put("inviteQQ", memberEle.attr("uin"));
                    map.put("inviteName", memberEle.text());
                }else {
                    map.put("applyMsg", secondLiEle.attr("title"));
                }
            }
            list.add(map);
        }
        return list;
    }

    @Override
    public String operatingGroupMsg(QQLoginEntity qqLoginEntity, String type, Map<String, String> map, String refuseMsg) throws IOException {
        String url = "https://web.qun.qq.com/cgi-bin/sys_msg/set_msgstate";
        Headers cookie = OkHttpUtils.addCookie(qqLoginEntity.getCookie());
        FormBody.Builder builder = new FormBody.Builder()
                .add("seq", map.get("seq"))
                .add("t", map.get("typeInt"))
                .add("gc", map.get("group"))
                .add("uin", qqLoginEntity.getQq().toString())
                .add("ver", "false")
                .add("from", "2")
                .add("bkn", qqLoginEntity.getGtk());
        switch (type){
            case "ignore":
                JSONObject jsonObject = OkHttpUtils.postJson(url, builder.add("cmd", "3").build(), cookie);
                if (jsonObject.getInteger("ec") == 0) return "忽略加入群聊成功！！";
                else return jsonObject.getString("em");
            case "refuse":
                if (refuseMsg == null) refuseMsg = "这是一条拒绝的消息哦！！";
                jsonObject = OkHttpUtils.postJson(url, builder.add("msg", refuseMsg).add("flag", "0").build(),
                        cookie);
                if (jsonObject.getInteger("ec") == 0) return "拒绝加入群聊成功！！";
                else return jsonObject.getString("em");
            case "agree":
                jsonObject = OkHttpUtils.postJson(url, builder.add("cmd", "1").build(), cookie);
                if (jsonObject.getInteger("ec") == 0) return "同意加入群聊成功！！";
                else return jsonObject.getString("em");
            default: return "类型不匹配！！";
        }
    }
}
