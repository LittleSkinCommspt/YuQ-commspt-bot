package top.cubik65536.yuq.controller;

import com.IceCreamQAQ.Yu.annotation.Action;
import com.IceCreamQAQ.Yu.annotation.Before;
import com.IceCreamQAQ.Yu.annotation.Config;
import com.icecreamqaq.yuq.FunKt;
import com.icecreamqaq.yuq.annotation.GroupController;
import com.icecreamqaq.yuq.entity.Group;
import top.cubik65536.yuq.entity.GroupEntity;
import top.cubik65536.yuq.logic.MinecraftToolLogic;
import top.cubik65536.yuq.service.GroupService;

import javax.inject.Inject;

@GroupController
@SuppressWarnings("unused")
public class ToolController {
    @Inject
    private GroupService groupService;
    @Inject
    private MinecraftToolLogic minecraftToolLogic;
    @Config("YuQ.Mirai.bot.master")
    private String master;

    @Before
    public GroupEntity before(long group, long qq){
        GroupEntity groupEntity = groupService.findByGroup(group);
        if (groupEntity == null) groupEntity = new GroupEntity();
        if (groupEntity.getBanJsonArray().contains(String.valueOf(qq))) FunKt.getMif().text(null).toMessage().toThrowable();
        return groupEntity;
    }

    @Action("&ping")
    public String ping() {
        return "Pong!";
    }

    @Action("&manual")
    public String manual() {
        return "请仔细阅读 LittleSkin 用户使用手册！\nhttps://manual.littlesk.in/";
    }

    @Action("&faq")
    public String faq() {
        return "你也许可以在「常见问题解答」中找到答案！\nhttps://manual.littlesk.in/faq.html";
    }

    @Action("&ot")
    public String ot() {
        return "您正在水群，但是本群不允许水群！\n请前往 Honoka Café 继续您的闲聊话题，群号：651672723。\n如果无视该提醒会发生不好的事情！";
    }

    @Action("&csl {player_name}")
    public void cslPlayerName(String player_name, Group group) throws Exception {
        String[] input = minecraftToolLogic.getCsl(player_name);
        if (input.length == 1) {
            group.sendMessage(FunKt.getMif().text(input[0]).toMessage());
            return;
        }
        String message = "角色名：" + input[0] + "\n" +
                "模型：" + input[1] + "\n" +
                "皮肤：" + input[2] + "\n" +
                "披风：" + input[3];
        group.sendMessage(FunKt.getMif().text(message).toMessage());
        group.sendMessage(minecraftToolLogic.getTexturePreview(input[2]));
        if (input[3].equals(null)) return;
        group.sendMessage(minecraftToolLogic.getTexturePreview(input[3]));
    }

    @Action("&csl.log")
    public String cslLog() {
        return "CustomSkinLoader 的日志位于 .minecraft/CustomSkinLoader/CustomSkinLoader.log 或 .minecraft/versions/" +
                "[version]/CustomSkinLoader/CustomSkinLoader.log，请将文件内容直接粘贴至 https://pastebin.aosc.io/" +
                "\n在提交后，请将网址发送至群内";
    }

    @Action("&csl.config")
    public String cslConfig(Group group) {
        return "请参照「手动修改配置文件」\\nhttps://manual.littlesk.in/newbee/mod.html#%E6%89%8B%E5%8A%A8%E4%BF%AE%E6%94%B9%E9%85%8D%E7%BD%AE%E6%96%87%E4%BB%B6";
    }

    @Action("&csl.latest")
    public String cslLatest() throws Exception {
        return minecraftToolLogic.getCslLatest();
    }

    @Action("&csl.gui")
    public String cslGui() {
        return "新的 CustomSkinLoader GUI 地址是 https://mc-csl.netlify.app/ 。";
    }

//    @Action("&ygg {player_name}")
//    public void yggPlayerName(String player_name, Group group) throws Exception {
//
//    }

    @Action("&ygg.nsis")
    public String yggNsis() {
        return "请确认服务器正确配置 authlib-injector 并将 online-mode 设为 true，否则请使用 CustomSkinLoader。" +
                "更多：https://manual.littlesk.in/advanced/yggdrasil.html";
    }

    @Action("&ygg.server.jvm")
    public String yggServerJVM() {
        return "请在启动脚本中加入参数 -Dauthlibinjector.debug=all，然后将 logs/latest.log 上传至群文件";
    }

    @Action("&ygg.latest")
    public String yggLatest() throws Exception {
        return minecraftToolLogic.getYggLatest();
    }

    @Action("&ygg.client.refresh")
    public String yggClientRefresh(Group group) {
        group.sendMessage(FunKt.getMif().imageByUrl("https://cdn.jsdelivr.net/gh/LittleSkinCommspt/commspt-bot@master/images/ygg-client-refresh.png").toMessage());
        return "请在你的 启动器 -> 账户列表 内刷新你的账户（以 HMCL3 为例）";
    }

    @Action("&ygg.url")
    public String yggUrl(Group group) {
        group.sendMessage(FunKt.getMif().imageByUrl("https://cdn.jsdelivr.net/gh/LittleSkinCommspt/commspt-bot@master/images/ygg-url.png").toMessage());
        return "https://littlesk.in";
    }

    @Action("&clfcsl.latest")
    public String clfcslLatest() throws Exception {
        String s = minecraftToolLogic.getClfcslLatest();
        return s + "\n在 1.7.10 中使用需要同时安装最新 Forge 版的 CustomSkinLoader 和 CompatibilityLayerForCustomSkinLoader，" +
                "你可以在 https://www.mcbbs.net/thread-1109996-1-1.html 下载到后者";
    }

    @Action("&view {textureHash}")
    public String view(String textureHash, Group group) throws Exception {
        if (textureHash.length() != 64) return "材质哈希值长度错误！";
        group.sendMessage(minecraftToolLogic.getTexturePreview(textureHash));
        return null;
    }

    @Action("&browser")
    public String browser(Group group) {
        group.sendMessage(FunKt.getMif().imageByUrl("https://cdn.jsdelivr.net/gh/LittleSkinCommspt/commspt-bot@master/images/browser.png").toMessage());
        return "请仔细阅读图片中的内容！以下是几个推荐的浏览器\n" +
                "Chrome: https://www.google.cn/chrome\n" +
                "Firefox: https://www.mozilla.org/firefox/new/\n" +
                "Edge: https://aka.ms/msedge";
    }

    @Action("&domain")
    public String domain() {
        return "我们推测您可能使用百度搜索 LittleSkin 并使用了在中国大陆过时的 littleskin.cn，我们建议您：\n1.将域名替换成 littlesk.in；\n2.使用除百度外的搜索引擎。";
    }

    @Action("&mail")
    public String mail() {
        return "请发送邮件至 support@littlesk.in，并在邮件中详细说明你的情况\n更多：https://manual.littlesk.in/email.html";
    }

    @Action("&help")
    public String help() {
        return "请查看 https://qnzh3311.restent.site/";
    }

}
