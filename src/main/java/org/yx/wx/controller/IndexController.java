package org.yx.wx.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.yx.wx.util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.*;


@Slf4j
public class IndexController {

    //获取accessToken接口
//    public static final String url = "https://api.weixin.qq.com/cgi-bin/token";
    public static final String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=${appId}&secret=${appSecret}";
    //关注接口
    private String getUserListUrl = "https://api.weixin.qq.com/cgi-bin/user/get?access_token=${ACCESS_TOKEN}";
    //转换接口
    private String transferUrl = "https://api.weixin.qq.com/cgi-bin/changeopenid?access_token=${ACCESS_TOKEN}";

    public TextField pathInput;
    public Button btn;
    public Label hint;

    /**
     * 选择输入框路径
     */
    public void selectPath() {

        Window window = pathInput.getScene().getWindow();

        DirectoryChooser directoryChooser = new DirectoryChooser();

        File file = directoryChooser.showDialog(window);

        if (file != null) {
            String path = file.getPath();//选择的文件夹路径
            System.out.println("你选择的文件夹路径" + path);
            //将选择的地址输入到选择框
            pathInput.setText(path);
        } else {
            System.out.println("用户取消选择");
        }

    }

    /**
     * 点击生成sql
     *
     * @throws InterruptedException
     */
    public void execute() {

        //参数检查
        if (!valid()) {
            return;
        }

        String oldAppid = ((TextField) btn.getScene().lookup("#old_appid")).getText();
        String oldAppsecret = ((TextField) btn.getScene().lookup("#old_appsecret")).getText();
        String accessToken = ((TextField) btn.getScene().lookup("#accessToken")).getText();
        String newAppid = ((TextField) btn.getScene().lookup("#new_appid")).getText();
        String newAppsecret = ((TextField) btn.getScene().lookup("#new_appsecret")).getText();
        String outPath = pathInput.getText();

        String oldAccessToken, newAccessToken;

//        try {
//            oldAccessToken = getAccessToken(oldAppid, oldAppsecret);
//        } catch (Exception e) {
//            log.error("获取旧AccessToken失败", e);
//            hint.setText("获取旧AccessToken失败" + e.getMessage());
//            return;
//        }

        if (StringUtils.isNotBlank(accessToken)) {
            oldAccessToken = accessToken;
        } else {
            try {
                oldAccessToken = getAccessToken(oldAppid, oldAppsecret);
            } catch (Exception e) {
                log.error("获取旧AccessToken失败", e);
                hint.setText("获取旧AccessToken失败" + e.getMessage());
                return;
            }
        }

        try {
            newAccessToken = getAccessToken(newAppid, newAppsecret);
        } catch (Exception e) {
            log.error("获取新AccessToken失败", e);
            hint.setText("获取新AccessToken失败" + e.getMessage());
            return;
        }

        String sqlContent = null;
        try {
            sqlContent = getSqlContent(oldAppid, oldAccessToken, newAccessToken);
        } catch (Exception e) {
            log.error("转化过程错误", e);
            hint.setText("转化过程错误：" + e.getMessage());
            return;
        }

        try {
            outFile(sqlContent, outPath);
        } catch (IOException e) {
            log.error("保存文件出错", e);
            hint.setText("保存文件出错！" + e.getMessage());
            return;
        }

        hint.setStyle("-fx-text-fill: #33FF00");
        hint.setText("生成完成");
        new Alert(Alert.AlertType.INFORMATION, "生成完成", ButtonType.OK).show();

    }

    /**
     * 验证输入
     *
     * @return
     */
    public boolean valid() {
        String oldAppid = ((TextField) btn.getScene().lookup("#old_appid")).getText();
        String oldAppsecret = ((TextField) btn.getScene().lookup("#old_appsecret")).getText();
        String accessToken = ((TextField) btn.getScene().lookup("#accessToken")).getText();

        String newAppid = ((TextField) btn.getScene().lookup("#new_appid")).getText();
        String newAppsecret = ((TextField) btn.getScene().lookup("#new_appsecret")).getText();

        String outPath = pathInput.getText();


        if (StringUtils.isBlank(oldAppid)) {
            hint.setText("请填写旧APPID");
            return false;
        }

        if (StringUtils.isBlank(oldAppsecret) && StringUtils.isBlank(accessToken)) {
            hint.setText("请填写旧accessToken或appsecret，优先accessToken，避免生产环境的accessToken失效");
            return false;
        }
        if (StringUtils.isBlank(newAppid)) {
            hint.setText("请填写新APPID");
            return false;
        }
        if (StringUtils.isBlank(newAppsecret)) {
            hint.setText("请填写新APPSECRET");
            return false;
        }
        if (StringUtils.isBlank(outPath)) {
            hint.setText("请选择输出路径!");
            return false;
        }

        return true;
    }

    /**
     * 清理提示
     */
    public void cleanHint() {
        hint.setText(null);
    }

    /**
     * 获取AccessToken
     */
    public String getAccessToken(String appId, String appSecret) {

        RestTemplate restTemplate = new RestTemplate();

        String jsonString = restTemplate.getForObject(url.replace("${appId}", appId).replace("${appSecret}", appSecret), String.class);
        JSONObject jsonObject = JSONObject.parseObject(jsonString);
        if (jsonObject.containsKey("errcode")) {
            throw new RuntimeException(jsonObject.toJSONString());
        }
        String accessToken = String.valueOf(jsonObject.get("access_token"));

        return accessToken;


//        String param = "?grant_type=client_credential&appid=${appId}&secret=${appSecret}";
//        param = param.replace("${appId}", appId).replace("${appSecret}", appSecret);
//        String resultText = HttpClientUtil.httpGet(url, param);
//        JSONObject result = JSONObject.parseObject(resultText);
//        if (result.containsKey("access_token")) {
//            String access_token = result.getString("access_token");
//            return access_token;
//        } else {
//            log.error("获取access_token失败:{}", resultText);
//            throw new RuntimeException("获取access_token失败");
//        }

    }

    /**
     * 调用接口循环获取openId
     *
     * @return
     */
    public String getSqlContent(String oldAppId, String oldAccessToken,
                                String newAccessToken) {
        List<JSONObject> responseResult = new ArrayList<>();

        /**
         * 查询此公众号现有用户的openid
         */
        RestTemplate restTemplate = new RestTemplate();
        String forObject = restTemplate.getForObject(getUserListUrl.replace("${ACCESS_TOKEN}", oldAccessToken), String.class);
        log.info("查询关注者列表：{}", forObject);
        JSONObject jsonObject = JSONObject.parseObject(forObject);
        if (jsonObject.containsKey("errmsg")) {
            throw new RuntimeException(jsonObject.getString("errmsg"));
        }
        JSONArray jsonArray = jsonObject.getJSONObject("data").getJSONArray("openid");
        List<String> openIds = new ArrayList<>();
        for (Object openId : jsonArray) {
            openIds.add(String.valueOf(openId));
        }

        //转换每次要求最多100个，那么将所有关注用户除以一百，得到多个集合分段进行
        Iterator<String> iterator = openIds.iterator();
        List<List<String>> openIdGroup = new ArrayList<>();

        int count = 0;
        List<String> batchIds = new ArrayList<>();

        while (iterator.hasNext()) {

            //小于100的时候累加
            if (count < 100) {
                batchIds.add(iterator.next());
                count++;
            } else {
                openIdGroup.add(batchIds);
                count = 0;
                batchIds = new ArrayList<>();
            }
        }

        //添加最后一组
        if (batchIds.size() > 0) {
            openIdGroup.add(batchIds);
        }

        //拼接字符串
        StringBuilder sb = new StringBuilder();

        //通过新的微信公众号accessToken获得查询地址
        String queryUrl = transferUrl.replace("${ACCESS_TOKEN}", newAccessToken);
        //封装数据
        for (List<String> list : openIdGroup) {
            JSONObject groupJson = new JSONObject();
            groupJson.put("from_appid", oldAppId);
            groupJson.put("openid_list", list);
            log.info("当前请求数据：{}", groupJson.toJSONString());
            String groupResponse = restTemplate.postForObject(queryUrl, groupJson, String.class);
            JSONObject response = JSONObject.parseObject(groupResponse);
            //有三个字段 ori_openid，new_openid，err_msg
            String errmsg = response.getString("errmsg");
            if ("ok".equals(errmsg)) {
                JSONArray result_list = response.getJSONArray("result_list");

                for (Object o : result_list) {
                    JSONObject result = JSONObject.parseObject(String.valueOf(o));
                    if ("ok".equals(result.getString("err_msg"))) {
                        String ori_openid = result.getString("ori_openid");
                        String new_openid = result.getString("new_openid");
                        // 拼装成sql
                        sb.append(String.format("UPDATE `user` set openid='%s' where openid ='%s'; -- 旧：%s，新：%s \r\n", new_openid, ori_openid, ori_openid, new_openid));
                    }
                    responseResult.add(result);
                }

            } else {
                throw new RuntimeException("调用接口失败！" + groupResponse);
            }

        }

        String content = sb.toString();
        log.info("生成的sql文本内容:{}", content);

        return content;

    }


    /**
     * 输出内容到文件
     *
     * @param text     文本内容
     * @param filePath 文件路径
     * @throws IOException
     */
    public void outFile(String text, String filePath) throws IOException {

        File file = new File(filePath + "/updateOpenId.sql");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileUtil.writeString(file, text);

    }


}
