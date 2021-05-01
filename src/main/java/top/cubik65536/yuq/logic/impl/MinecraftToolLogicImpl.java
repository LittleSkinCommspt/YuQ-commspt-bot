package top.cubik65536.yuq.logic.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.icecreamqaq.yuq.FunKt;
import top.cubik65536.yuq.logic.MinecraftToolLogic;
import com.icecreamqaq.yuq.message.Message;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * MinecraftToolLogicImpl
 * top.cubik65536.yuq.logic.impl
 * yuq-commspt-bot
 * <p>
 * Created by Cubik65536 on 2021-04-30.
 * Copyright © 2020-2021 Cubik Inc. All rights reserved.
 * <p>
 * Description:
 * History:
 * 1. 2021-04-30 [Cubik65536]: Create file MinecraftToolLogicImpl;
 */

public class MinecraftToolLogicImpl implements MinecraftToolLogic {

    @Override
    public Message getTexturePreview(String hash) throws Exception {
        hash = URLEncoder.encode(hash, "UTF-8");
        String generalUrl="https://mcskin.littleservice.cn/preview/hash/" + hash + "?png";
        URL url = new URL(generalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        int statusCode = connection.getResponseCode();
        if (statusCode != 200) {
            return FunKt.getMif().text("获取失败！").toMessage();
        }
        return FunKt.getMif().imageByInputStream(connection.getInputStream()).toMessage();
    }

    @Override
    public String getClfcslLatest() throws Exception {
        String generalUrl="https://csl-1258131272.cos.ap-shanghai.myqcloud.com/latest.json";
        URL url = new URL(generalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String info : headers.keySet()) {
            System.err.println(info + "--->" + headers.get(info));
        }
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String version = jsonObject.getString("version");
        String forge = jsonObject.getJSONObject("downloads")
                .getString("Forge");
        return "CustomSkinLoader 最新版本：" + version + "\n" +
                "Forge: " + forge;
    }

    @Override
    public String getYggLatest() throws Exception {
        String generalUrl="https://authlib-injector.yushi.moe/artifact/latest.json";
        URL url = new URL(generalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String info : headers.keySet()) {
            System.err.println(info + "--->" + headers.get(info));
        }
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String version = jsonObject.getString("version");
        String download_url = jsonObject.getString("download_url");
        return "authlib-injector 最新版本：" + version + "\n" + download_url;
    }

    @Override
    public String getCslLatest() throws Exception {
        String generalUrl="https://csl-1258131272.cos.ap-shanghai.myqcloud.com/latest.json";
        URL url = new URL(generalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String info : headers.keySet()) {
            System.err.println(info + "--->" + headers.get(info));
        }
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        JSONObject jsonObject = JSONObject.parseObject(result);
        String version = jsonObject.getString("version");
        String fabric = jsonObject.getJSONObject("downloads").getString("Fabric");
        String forge = jsonObject.getJSONObject("downloads").getString("forge");
        return "CustomSkinLoader 最新版本：" + version + "\nForge: " + forge + "\nFabric: " + fabric;
    }

    @Override
    public String[] getCsl(String playerName) throws Exception {
        playerName = URLEncoder.encode(playerName, "UTF-8");
        String generalUrl="https://mcskin.littleservice.cn/csl/" + playerName + ".json";
        URL url = new URL(generalUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.setUseCaches(false);
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.connect();
        Map<String, List<String>> headers = connection.getHeaderFields();
        for (String info : headers.keySet()) {
            System.err.println(info + "--->" + headers.get(info));
        }
        BufferedReader in = null;
        in = new BufferedReader(
                new InputStreamReader(connection.getInputStream(), "UTF-8"));
        String result = "";
        String getLine;
        while ((getLine = in.readLine()) != null) {
            result += getLine;
        }
        in.close();
        System.err.println("result:" + result);
        if (result.equals("{}")) {
            return new String[]{"Error: Player " + playerName + " not found"};
        }
        JSONObject jsonObject = JSONObject.parseObject(result);
        String name = jsonObject.getString("username");
        String skin_type = "default";
        if (jsonObject.getJSONObject("skins").containsKey("slim")) {
            skin_type = "slim";
        }
        String skin_hash = jsonObject.getJSONObject("skins").getString(skin_type);
        String cape_hash = jsonObject.getString("cape");
        return new String[]{name, skin_type, skin_hash, cape_hash};
    }
}
