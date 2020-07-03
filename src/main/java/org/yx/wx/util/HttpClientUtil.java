package org.yx.wx.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.http.*;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class HttpClientUtil {

    public static final Logger log = LoggerFactory.getLogger(HttpClientUtil.class);

    /**
     * 超时时间 .
     */
    private static int SOCKET_TIMEOUT = 180000;
    /**
     * 连接超时时间 .
     */
    private static int CONNECT_TIMEOUT = 180000;

    private static RequestConfig requestConfig = null;

    static {
        // 设置请求和传输超时时间
        requestConfig = RequestConfig.custom()/*.setProxy(new HttpHost("127.0.0.1",8888))*/.setSocketTimeout(2000).setConnectTimeout(2000).build();
    }


    /**
     * 请求处理类.
     *
     * @param url     .
     * @param dataMap .
     * @return CloseableHttpResponse .
     */
    public static String httpPost(String url, Map<String, Object> dataMap) {

        String result = null;

        try {
            CloseableHttpClient client = HttpClients.custom().build();
            RequestConfig config = RequestConfig.custom()
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    .setAuthenticationEnabled(false)
                    .build();


            HttpPost post = new HttpPost(url);
            post.setProtocolVersion(org.apache.http.HttpVersion.HTTP_1_1);
            post.setConfig(config);

            //构造参数集合
            List<NameValuePair> formpair = new ArrayList<NameValuePair>();

            for (String str : dataMap.keySet().toArray(new String[dataMap.size()])) {
                formpair.add(new BasicNameValuePair(str, dataMap.get(str).toString()));
            }

            //构造http请求实体
            HttpEntity entity = new UrlEncodedFormEntity(formpair, Consts.UTF_8);

            if (entity != null) {
                post.setEntity(entity);
            }

            log.warn("httpPost开始发送");

            CloseableHttpResponse response = client.execute(post);

            // 请求发送成功，并得到响应。
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) try {
                log.info("httpPost发送成功");
                // 读取服务器返回过来的json字符串数据
                result = EntityUtils.toString(response.getEntity(), "utf-8");
            } catch (Exception e) {
                log.error("httpPost请求提交失败:" + url, e);
            }
            else {
                //如果状态码不是200
                log.error("httpPost请求提交失败:{},状态码:{}", url, response.getStatusLine().getStatusCode());
            }
        } catch (ClientProtocolException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        return result;
    }


    /**
     * post请求传输json参数
     * 当接口接收的参数为json时使用此方法
     *
     * @param url       url地址
     * @param jsonParam 参数
     * @return
     */
    public static JSONObject httpPost(String url, JSONObject jsonParam) {
        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        JSONObject jsonResult = null;
        HttpPost httpPost = new HttpPost(url);
        // 设置请求和传输超时时间
        httpPost.setConfig(requestConfig);
        try {
            if (null != jsonParam) {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(jsonParam.toString(), "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/json");
                httpPost.setEntity(entity);
            }
            CloseableHttpResponse result = httpClient.execute(httpPost);
            // 请求发送成功，并得到响应
            if (result.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                String str = "";
                try {
                    // 读取服务器返回过来的json字符串数据
                    str = EntityUtils.toString(result.getEntity(), "utf-8");
                    // 把json字符串转换成json对象
                    jsonResult = JSONObject.parseObject(str);
                } catch (Exception e) {
                    log.error("post请求提交失败:" + url, e);
                }
            }
        } catch (IOException e) {
            log.error("post请求提交失败:" + url, e);
        } finally {
            httpPost.releaseConnection();
        }
        return jsonResult;
    }

    /**
     * 当接口使用表单时请使用此方法
     * post请求传输String参数 例如：name=Jack&sex=1&type=2
     * Content-type:application/x-www-form-urlencoded
     *
     * @param url      url地址
     * @param strParam 参数
     * @return
     */
    public static String httpPost(String url, String strParam) {
        // post请求返回结果
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String jsonResult = null;
//        JSONObject jsonResult = null;
        HttpPost httpPost = new HttpPost(url);
        httpPost.setConfig(requestConfig);
        try {
            if (null != strParam) {
                // 解决中文乱码问题
                StringEntity entity = new StringEntity(strParam, "utf-8");
                entity.setContentEncoding("UTF-8");
                entity.setContentType("application/x-www-form-urlencoded");
                httpPost.setEntity(entity);
            }
            CloseableHttpResponse response = httpClient.execute(httpPost);
            // 请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    // 读取服务器返回过来的json字符串数据
                    jsonResult = EntityUtils.toString(response.getEntity(), "utf-8");
                } catch (Exception e) {
                    log.error("post请求提交失败:" + url, e);
                }
            } else {
                //如果状态码不是200
                log.error("get请求提交失败:{},状态码:{}", url, response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.error("post请求提交失败:" + url, e);
        } finally {
            httpPost.releaseConnection();
        }
        return jsonResult;
    }

    /**
     * 发送get请求
     * 请求传输String参数 例如：name=Jack&sex=1&type=2
     *
     * @param url 路径
     * @return
     */
    public static String httpGet(String url, String param) {
        // get请求返回结果
        String result = null;
        CloseableHttpClient client = HttpClients.createDefault();
        // 发送get请求
        HttpGet request = new HttpGet(url + param);
        request.setConfig(requestConfig);
        try {
            CloseableHttpResponse response = client.execute(request);

            // 请求发送成功，并得到响应
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // 读取服务器返回过来的json字符串数据
                HttpEntity entity = response.getEntity();
                result = EntityUtils.toString(entity, "utf-8");
            } else {
                log.error("get请求提交失败:{},状态码:{}", url, response.getStatusLine().getStatusCode());
            }
        } catch (IOException e) {
            log.error("get请求提交失败:" + url, e);
        } finally {
            request.releaseConnection();
        }
        return result;
    }


    /**
     * 转换成url参数
     *
     * @param map       需要参数map
     * @param isSort    是否需要排序
     * @param removeKey map中不需要参与转成url的key
     * @return String
     */
    public static String getURLParam(Map map, boolean isSort, Set removeKey) {
        StringBuffer param = new StringBuffer("?");
        List msgList = new ArrayList();
        for (Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            String value = (String) map.get(key);
            if (removeKey != null && removeKey.contains(key)) {
                continue;
            }
            msgList.add(key + "=" + value);
        }

        if (isSort) {
            // 排序
            Collections.sort(msgList);
        }

        for (int i = 0; i < msgList.size(); i++) {
            String msg = (String) msgList.get(i);
            if (i > 0) {
                param.append("&");
            }
            param.append(msg);
        }

        return param.toString();
    }


}
