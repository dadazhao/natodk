package com.natodk;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsRequest;
import com.aliyuncs.alidns.model.v20150109.DescribeDomainRecordsResponse;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordRequest;
import com.aliyuncs.alidns.model.v20150109.UpdateDomainRecordResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.google.gson.Gson;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ALiYunDDNS {

    private static final String IP_TYPE = "ip-type";
    private static final String IPV4 = "ipv4";
    private static final String IPV6 = "ipv6";

    private static final String TEST_IPV4 = "test-ipv4";
    private static final String TEST_IPV4_REXP = "test-ipv4-rexp";
    private static final String TEST_IPV6 = "test-ipv6";
    private static final String TEST_IPV6_REXP = "test-ipv6-rexp";

    private static final String ALI_REGINO_ID="ali-regino-id";
    private static final String ALI_ACCESS_KEY_ID="ali-access-key-id";
    private static final String ALI_ACCESS_KEY_SECRET="ali-access-key-secret";

    private static final String ALI_DOMAIN_NAME="ali-domain-name";
    private static final String ALI_DOMAIN_KEYWORD="ali-domain-keyword";
    private static final String ALI_DOMAIN_TYPE="ali-domain-type";


    private static Properties props = new Properties();

    /**
     * 获取主域名的所有解析记录列表
     */
    private DescribeDomainRecordsResponse describeDomainRecords(DescribeDomainRecordsRequest request, IAcsClient client) {
        try {
            // 调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
            // 发生调用错误，抛出运行时异常
            throw new RuntimeException();
        }
    }

    /**
     * 获取当前主机公网IP
     */
    private String getCurrentHostIP() {

        String jsonip="";
        String rexp ="";
        // 这里使用jsonip.com第三方接口获取本地IP
        if (IPV6.equals(props.getProperty(IP_TYPE))){
            jsonip=props.getProperty(TEST_IPV6);
            rexp=props.getProperty(TEST_IPV6_REXP);
        }else{
            jsonip=props.getProperty(TEST_IPV4);
            rexp=props.getProperty(TEST_IPV4_REXP);
        }
        // 接口返回结果
        String result = "";
        BufferedReader in = null;
        if (jsonip!=null&&jsonip.startsWith("http://")){
            try {
                // 使用HttpURLConnection网络请求第三方接口
                URL url = new URL(jsonip);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 使用finally块来关闭输入流
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
        }else{
            try {
                TrustManager[] trustAllCerts = new TrustManager[1];
                TrustManager tm = new miTM();
                trustAllCerts[0] = tm;
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, null);
                // 使用HttpURLConnection网络请求第三方接口
                URL url = new URL(jsonip);
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setSSLSocketFactory(sc.getSocketFactory());
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();
                in = new BufferedReader(new InputStreamReader(
                        urlConnection.getInputStream()));
                String line;
                while ((line = in.readLine()) != null) {
                    result += line;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            // 使用finally块来关闭输入流
            finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (Exception e2) {
                    e2.printStackTrace();
                }

            }
        }
        // 正则表达式，提取xxx.xxx.xxx.xxx，将IP地址从接口返回结果中提取出来
        Pattern pat = Pattern.compile(rexp);
        Matcher mat = pat.matcher(result);
        String res = "";
        while (mat.find()) {
            res = mat.group();
            break;
        }
        return res;
    }

    /**
     * 修改解析记录
     */
    private UpdateDomainRecordResponse updateDomainRecord(UpdateDomainRecordRequest request, IAcsClient client) {
        try {
            // 调用SDK发送请求
            return client.getAcsResponse(request);
        } catch (ClientException e) {
            e.printStackTrace();
            // 发生调用错误，抛出运行时异常
            throw new RuntimeException();
        }
    }

    private static void log_print(String functionName, Object result) {
        Gson gson = new Gson();
        System.out.println("-------------------------------" + functionName + "-------------------------------");
        System.out.println(gson.toJson(result));
    }

    public static void main(String[] args){
        //加载配置文件
        if (!loadProperties()) {
            System.out.println("配置文件加载失败!!");
            return;
        }
        //兼容https java1.7
        System.setProperty("https.protocols", "TLSv1.2");

        // 设置鉴权参数，初始化客户端
        DefaultProfile profile = DefaultProfile.getProfile(
                props.getProperty(ALI_REGINO_ID),// 地域ID
                props.getProperty(ALI_ACCESS_KEY_ID),// 您的AccessKey ID
                props.getProperty(ALI_ACCESS_KEY_SECRET));// 您的AccessKey Secret
        IAcsClient client = new DefaultAcsClient(profile);

        ALiYunDDNS ddns = new ALiYunDDNS();

        // 查询指定二级域名的最新解析记录
        DescribeDomainRecordsRequest describeDomainRecordsRequest = new DescribeDomainRecordsRequest();
        // 主域名
        describeDomainRecordsRequest.setDomainName(props.getProperty(ALI_DOMAIN_NAME));
        // 主机记录
        describeDomainRecordsRequest.setRRKeyWord(props.getProperty(ALI_DOMAIN_KEYWORD));
        // 解析记录类型
        describeDomainRecordsRequest.setType(props.getProperty(ALI_DOMAIN_TYPE));
        DescribeDomainRecordsResponse describeDomainRecordsResponse = ddns.describeDomainRecords(describeDomainRecordsRequest, client);
        log_print("describeDomainRecords",describeDomainRecordsResponse);

        List<DescribeDomainRecordsResponse.Record> domainRecords = describeDomainRecordsResponse.getDomainRecords();
        // 最新的一条解析记录
        if(domainRecords.size() != 0 ){
            DescribeDomainRecordsResponse.Record record = domainRecords.get(0);
            // 记录ID
            String recordId = record.getRecordId();
            // 记录值
            String recordsValue = record.getValue();
            // 当前主机公网IP
            String currentHostIP = ddns.getCurrentHostIP();
            System.out.println("-------------------------------当前主机公网IP为："+currentHostIP+"-------------------------------");
            if(!currentHostIP.equals(recordsValue)){
                // 修改解析记录
                UpdateDomainRecordRequest updateDomainRecordRequest = new UpdateDomainRecordRequest();
                // 主机记录
                updateDomainRecordRequest.setRR(props.getProperty(ALI_DOMAIN_KEYWORD));
                // 记录ID
                updateDomainRecordRequest.setRecordId(recordId);
                // 将主机记录值改为当前主机IP
                updateDomainRecordRequest.setValue(currentHostIP);
                // 解析记录类型
                updateDomainRecordRequest.setType(props.getProperty(ALI_DOMAIN_TYPE));
                UpdateDomainRecordResponse updateDomainRecordResponse = ddns.updateDomainRecord(updateDomainRecordRequest, client);
                log_print("updateDomainRecord",updateDomainRecordResponse);
            }
        }
    }


    /**
     * 加载配置文件
     * @return
     */
    private static boolean loadProperties(){
        InputStream is =null;
        try {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(("application.properties"));
            props.load(is);
        } catch (IOException e) {
            return false;
        }finally {
            if (is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    static class miTM implements TrustManager, X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        public boolean isServerTrusted(X509Certificate[] certs) {
            return true;
        }

        public boolean isClientTrusted(X509Certificate[] certs) {
            return true;
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            return;
        }
    }

}
