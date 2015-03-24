import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Administrator on 2015/3/24.
 */
public class NetManagerUtil {
    /**
     * 文件上传压缩比
     */
    public final static String COMPRESSRATIO = "50";

    private Context c;
    private static NetManagerUtil instance = null;
    private final String CHARTSET = "utf8";
    private final int TIMEOUT = 15000;
    private static String COOKIE;

    public static NetManagerUtil getInstance(Context c) {
        if (instance == null) {
            instance = new NetManagerUtil();
            instance.c = c.getApplicationContext();
        }
        return instance;
    }

    private NetManagerUtil() {
    }

    /**
     * 此方法是用HttpPost方式访问http资源
     *
     * @param url url地址
     * @param ht  存储键值对
     * @return String
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public String dopostAsString(String url, Hashtable<String, String> ht)
            throws CniiaException.NetworkException {
        try {
            DefaultHttpClient httpclient = getHttpClient();
            HttpPost req = new HttpPost(url);
            if (COOKIE != null && !COOKIE.equals("")) {
                req.setHeader("Cookie", COOKIE);
            }
            // 加入session
            // if (!"".equals(PreferencesUtil.getSharedStringData(c,
            // "session"))) {
            // req.setHeader("Cookie","JSESSIONID="+PreferencesUtil.getSharedStringData(c,
            // "session"));
            // System.out.println("传入session "+PreferencesUtil.getSharedStringData(c,
            // "session"));
            // }
            req.getParams().setParameter(
                    HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT);
            List<NameValuePair> valuepairs = new ArrayList<NameValuePair>(); // post提交的名值对
            Enumeration<String> en = ht.keys();
            while (en.hasMoreElements()) {// 遍历hashtable 获取key
                String key = en.nextElement();
                valuepairs.add(new BasicNameValuePair(key, ht.get(key)));
            }

            req.setEntity(new UrlEncodedFormEntity(valuepairs, HTTP.UTF_8));
            HttpResponse rep = httpclient.execute(req);
            Header[] headers = rep.getHeaders("Set-Cookie");

            StringBuffer sb = new StringBuffer();
            for (Header h : headers) {
                sb.append(h.getValue() + ";");
            }
            if (!sb.toString().equals("")) {
                COOKIE = sb.toString();
            }

            InputStream in = rep.getEntity().getContent();
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            sb = new StringBuffer();
            String line = "";
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            return sb.toString();
        } catch (Exception e) {
            throw new CniiaException.NetworkException(e.toString(), e);
        }
    }

    /**
     * 此方法是用HttpPost方式访问http资源
     *
     * @param url url地址
     * @param ht  存储键值对
     * @return InputSteam
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public InputStream dopostAsStream(String url, Hashtable<String, String> ht)
            throws CniiaException.NetworkException {
        try {
            DefaultHttpClient httpclient = getHttpClient();
            HttpPost req = new HttpPost(url);
            if (COOKIE != null && !COOKIE.equals("")) {
                req.setHeader("Cookie", COOKIE);
            }

            req.getParams().setParameter(
                    HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT);

            List<NameValuePair> valuepairs = new ArrayList<NameValuePair>();// post提交的名值对
            Enumeration<String> en = ht.keys();
            while (en.hasMoreElements()) {// 遍历hashtable 获取key
                String key = en.nextElement();
                valuepairs.add(new BasicNameValuePair(key, ht.get(key)));
            }
            req.setEntity(new UrlEncodedFormEntity(valuepairs, HTTP.UTF_8));
            HttpResponse rep = httpclient.execute(req);
            Header[] headers = rep.getHeaders("Set-Cookie");
            StringBuffer sb = new StringBuffer();
            for (Header h : headers) {
                sb.append(h.getValue() + ";");
            }
            if (!sb.toString().equals("")) {
                COOKIE = sb.toString();
            }

            HttpEntity entity = rep.getEntity();
            InputStream in = entity.getContent();
            return in;
        } catch (Exception e) {
            throw new CniiaException.NetworkException(e.toString(), e);
        }
    }

    /**
     * 此方法是用HttpPost方式访问http资源
     *
     * @param url  url地址
     * @param data JSON字串
     * @return String
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public String dopostAsString(String url, String data)
            throws CniiaException.NetworkException {
        try {
            HttpURLConnection hc = getURLConnection(url);
            hc.setRequestMethod("POST");
            hc.setDoInput(true);
            hc.setDoOutput(true);
            if (COOKIE != null) {
                hc.setRequestProperty("Cookie", COOKIE);
            }
            OutputStream out = hc.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            out.close();

            InputStream in;
            String str = hc.getHeaderField("Content-Encoding");
            if (str != null && str.equals("gzip")) {
                in = new GZIPInputStream(hc.getInputStream());
            } else {
                in = hc.getInputStream();
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            String cookie = hc.getHeaderField("set-cookie");
            if (cookie != null && cookie.length() > 0) {
                COOKIE = cookie;
            }
            //便于观察数据
            return sb.toString();
        } catch (Exception e) {
            throw new CniiaException.NetworkException("Net Exception", e);
        }
    }

    /**
     * 此方法是用HttpPost方式访问http资源
     *
     * @param url  url地址
     * @param data JSON字串
     * @return String
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public String dopostAsStringWithSession(String url, String data, String Session)
            throws CniiaException.NetworkException {
        try {
            HttpURLConnection hc = getURLConnection(url);
            hc.setRequestMethod("POST");
            hc.setDoInput(true);
            hc.setDoOutput(true);
            if (COOKIE != null) {
                hc.setRequestProperty("Cookie", COOKIE);
            }
            COOKIE = Session;
            OutputStream out = hc.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            out.close();

            InputStream in;
            String str = hc.getHeaderField("Content-Encoding");
            if (str != null && str.equals("gzip")) {
                in = new GZIPInputStream(hc.getInputStream());
            } else {
                in = hc.getInputStream();
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            String cookie = hc.getHeaderField("set-cookie");
            if (cookie != null && cookie.length() > 0) {
                COOKIE = cookie;
            }
            return sb.toString();
        } catch (Exception e) {
            throw new CniiaException.NetworkException("Net Exception", e);
        }
    }

    /**
     * 此方法是用HttpPost方式访问http资源
     *
     * @param url  url地址
     * @param data JSON字串
     * @return InputStream
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public InputStream dopostAsInputStream(String url, String data)
            throws CniiaException.NetworkException {
        try {
            HttpURLConnection hc = getURLConnection(url);
            hc.setRequestMethod("POST");
            if (COOKIE != null) {
                hc.setRequestProperty("Cookie", COOKIE);
            }
            hc.setDoInput(true);
            hc.setDoOutput(true);
            OutputStream out = hc.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            out.close();
            InputStream in;
            String str = hc.getHeaderField("Content-Encoding");
            if (str != null && str.equals("gzip")) {
                in = new GZIPInputStream(hc.getInputStream());
            } else {
                in = hc.getInputStream();
            }
            return in;
        } catch (Exception e) {
            throw new CniiaException.NetworkException("Net Exception", e);
        }
    }

    /**
     * 此方法是用HttpGet方式访问http资源
     *
     * @param url ：url地址
     * @return String
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public String dogetAsString(String url) throws CniiaException.NetworkException {
        try {
            DefaultHttpClient httpclient = getHttpClient();
            HttpGet req = new HttpGet(url);
            if (COOKIE != null && !COOKIE.equals("")) {
                req.setHeader("Cookie", COOKIE);
            }
            req.getParams().setParameter(
                    HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT);
            HttpResponse rep = httpclient.execute(req);
            Header[] headers = rep.getHeaders("Set-Cookie");
            StringBuffer sb = new StringBuffer();
            for (Header h : headers) {
                sb.append(h.getValue() + ";");
            }
            if (!sb.toString().equals("")) {
                COOKIE = sb.toString();
            }

            InputStream in = rep.getEntity().getContent();
            BufferedReader r = new BufferedReader(new InputStreamReader(in,
                    CHARTSET));
            sb = new StringBuffer();
            String line = "";
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            return sb.toString();
        } catch (Exception e) {
            throw new CniiaException.NetworkException(e.toString(), e);
        }
    }

    /**
     * 此方法是用HttpGet方式访问http资源
     *
     * @param url ：url地址
     * @return inputStream
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public InputStream dogetAsStream(String url) throws CniiaException.NetworkException {
        try {
            DefaultHttpClient httpclient = getHttpClient();
            HttpGet req = new HttpGet(url);
            if (COOKIE != null && !COOKIE.equals("")) {
                req.setHeader("Cookie", COOKIE);
            }
            req.getParams().setParameter(
                    HttpConnectionParams.CONNECTION_TIMEOUT, TIMEOUT);
            HttpResponse rep = httpclient.execute(req);
            Header[] headers = rep.getHeaders("Set-Cookie");
            StringBuffer sb = new StringBuffer();
            for (Header h : headers) {
                sb.append(h.getValue() + ";");
            }
            if (!sb.toString().equals("")) {
                COOKIE = sb.toString();
            }

            return rep.getEntity().getContent();
        } catch (Exception e) {
            throw new CniiaException.NetworkException(e.toString(), e);
        }
    }

    /**
     * 此类包含了三种连接网络方式分别是：wifi,proxy和除了这两种之外的网络连接方式，
     * 使用此类的execute方法发送HttpGet和HttpPost请求
     *
     * @return DefaultHttpClient对象
     */
    public DefaultHttpClient getHttpClient() {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        if (isWiFiActive()) {

        } else {
            String proxyHost = android.net.Proxy.getDefaultHost();
            if (proxyHost != null) {
                HttpHost proxy = new HttpHost(
                        android.net.Proxy.getDefaultHost(),
                        android.net.Proxy.getDefaultPort());
                httpClient.getParams().setParameter(
                        ConnRoutePNames.DEFAULT_PROXY, proxy);
            } else {

            }
        }
        httpClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                TIMEOUT);
        return httpClient;
    }

    /**
     * 此方法是用HttpURLConnection方式访问http资源, 包含三种网络连接方式wifi,proxy和除了这两种方式之外的连接方式
     *
     * @param url url地址
     * @return HttpURLConnection
     * @throws Exception
     */
    public HttpURLConnection getURLConnection(String url) throws Exception {
        HttpURLConnection hc;
        if (isWiFiActive()) {
            hc = (HttpURLConnection) new URL(url).openConnection();
        } else {
            String proxyHost = android.net.Proxy.getDefaultHost();
            if (proxyHost != null) {
                java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP,
                        new InetSocketAddress(
                                android.net.Proxy.getDefaultHost(),
                                android.net.Proxy.getDefaultPort()));
                hc = (HttpURLConnection) new URL(url).openConnection(p);
            } else {
                hc = (HttpURLConnection) new URL(url).openConnection();
            }
        }
        hc.setRequestProperty("Content-Type",
                "application/x-www-form-urlencoded; charset=UTF-8");
        return hc;
    }

    /**
     * 判断是否WiFi连接
     *
     * @return 如果是wifi连接，返回true,否则返回false
     */
    private boolean isWiFiActive() {
        ConnectivityManager connectivity = (ConnectivityManager) c
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTypeName().equals("WIFI")
                            && info[i].isConnected()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 上传图片
     *
     * @param picUrl 请求地址
     * @param list   图片路径集合
     * @return 返回数据
     * @throws NetManagerUtil.CniiaException.NetworkException 异常
     */
    public String uploadFile(String picUrl, List<?> list)
            throws CniiaException.NetworkException {
        List<?> picList = list;
        StringBuffer buffer = new StringBuffer();
        try {
            String BOUNDARY = "------------------------7dc3482080a10"; // 定义数据分隔线
            URL url = new URL(picUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());
            byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
            int leng = picList.size();
            for (int i = 0; i < leng; i++) {
                String fname = (String) picList.get(i);
                File file = new File(fname);
                StringBuilder sb = new StringBuilder();
                sb.append("--");
                sb.append(BOUNDARY);
                sb.append("\r\n");
                sb.append("Content-Disposition: form-data;name=\"file"
                        + "\";filename=\"" + file.getName() + "\"\r\n");
                sb.append("Content-Type:image/png\r\n\r\n");
                byte[] data = sb.toString().getBytes();
                out.write(data);
                DataInputStream in = new DataInputStream(new FileInputStream(
                        file));
                int bytes = 0;
                byte[] bufferOut = new byte[1024];
                while ((bytes = in.read(bufferOut)) != -1) {
                    out.write(bufferOut, 0, bytes);
                }
                out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
                in.close();
            }
            out.write(end_data);
            out.flush();
            out.close();
            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            throw new CniiaException.NetworkException("Net Exception", e);
        }
    }

    /**
     * 上传byte[] 文件
     *
     * @param picUrl
     * @param list
     * @return
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public String uploadFileByte(String picUrl,
                                 List<HashMap<String, Object>> list) throws CniiaException.NetworkException {
        List<HashMap<String, Object>> picList = list;
        StringBuffer buffer = new StringBuffer();
        try {
            String BOUNDARY = "------------------------7dc3482080a10"; // 定义数据分隔线
            URL url = new URL(picUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; Win64; x64; Trident/5.0)");
            conn.setRequestProperty("Charsert", "UTF-8");
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + BOUNDARY);
            OutputStream out = new DataOutputStream(conn.getOutputStream());

            if (null != list && !list.isEmpty()) {
                byte[] end_data = ("\r\n--" + BOUNDARY + "--\r\n").getBytes();// 定义最后数据分隔线
                int leng = picList.size();
                for (int i = 0; i < leng; i++) {
                    if (!picList.get(i).isEmpty()) {
                        String fname = (String) picList.get(i).get("fileName");
                        byte[] bytes = (byte[]) picList.get(i).get("byte");
                        File file = new File(fname);
                        StringBuilder sb = new StringBuilder();
                        sb.append("--");
                        sb.append(BOUNDARY);
                        sb.append("\r\n");
                        sb.append("Content-Disposition: form-data;name=\"file"
                                + "\";filename=\"" + file.getName()
                                + "\"\r\n");
                        sb.append("Content-Type:image/png\r\n\r\n");
                        byte[] data = sb.toString().getBytes();
                        out.write(data);
                        out.write(bytes);
                        out.write("\r\n".getBytes()); // 多个文件时，二个文件之间加入这个
                    }
                }
                out.write(end_data);
                out.flush();
                out.close();
            }

            // 定义BufferedReader输入流来读取URL的响应
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line = null;

            while ((line = reader.readLine()) != null) {
                buffer.append(line);
            }
            return buffer.toString();
        } catch (Exception e) {
            throw new CniiaException.NetworkException("Net Exception", e);
        }
    }

    /**
     * 通过拼接的方式构造请求内容，实现参数传输以及文件传输
     *
     * @param actionUrl
     * @param params
     * @param files
     * @return
     * @throws java.io.IOException
     */
    public static String post(String actionUrl, Map<String, String> params,
                              Map<String, File> files) throws IOException {
        StringBuffer buffer = new StringBuffer();
        String BOUNDARY = "----WebKitFormBoundaryzxfGlnK9WtAnyTVa";
        String PREFIX = "--", LINEND = "\r\n";
        String MULTIPART_FROM_DATA = "multipart/form-data";
        String CHARSET = "UTF-8";

        URL uri = new URL(actionUrl);
        HttpURLConnection conn = (HttpURLConnection) uri.openConnection();
        conn.setReadTimeout(5 * 1000); // 缓存的最长时间
        conn.setDoInput(true);// 允许输入
        conn.setDoOutput(true);// 允许输出
        conn.setUseCaches(false); // 不允许使用缓存
        conn.setRequestMethod("POST");
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Charsert", "UTF-8");
        conn.setRequestProperty("Content-Type", MULTIPART_FROM_DATA
                + "; boundary=" + BOUNDARY);

        // 首先组拼文本类型的参数
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            sb.append(PREFIX);
            sb.append(BOUNDARY);
            sb.append(LINEND);
            sb.append("Content-Disposition: form-data; name=\""
                    + entry.getKey() + "\"" + LINEND);
            // sb.append("Content-Type: text/plain; charset=" + CHARSET +
            // LINEND);
            // sb.append("Content-Transfer-Encoding: 8bit" + LINEND);
            sb.append(LINEND);
            // sb.append(entry.getValue());
            sb.append(java.net.URLEncoder.encode(entry.getValue()));
            sb.append(LINEND);
        }
        sb.append(PREFIX);
        sb.append(BOUNDARY);
        sb.append(LINEND);
        DataOutputStream outStream = new DataOutputStream(
                conn.getOutputStream());

        outStream.write(sb.toString().getBytes());
        // 发送文件数据
        if (files != null) {
            for (Map.Entry<String, File> file : files.entrySet()) {
                StringBuilder sb1 = new StringBuilder();
                sb1.append(PREFIX);
                sb1.append(BOUNDARY);
                sb1.append(LINEND);
                sb1.append("Content-Disposition: form-data; name=\"file\"; filename=\""
                        + file.getKey() + "\"" + LINEND);
                sb1.append("Content-Type: application/octet-stream; charset="
                        + CHARSET + LINEND);
                sb1.append(LINEND);
                outStream.write(sb1.toString().getBytes());

                // 压缩文件
                compress(file.getValue().toString());
                InputStream is = new FileInputStream(Environment
                        .getExternalStorageDirectory().toString()
                        + "/CONSDCGMPIC/" + "temp.jpg");// 压缩后的文件

                // 源文件流
                // InputStream is = new FileInputStream(file.getValue());
                byte[] buffer1 = new byte[1024];
                int len = 0;
                while ((len = is.read(buffer1)) != -1) {
                    outStream.write(buffer1, 0, len);
                }

                is.close();
                outStream.write(LINEND.getBytes());
            }

            // 请求结束标志
            byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINEND).getBytes();
            outStream.write(end_data);
            outStream.flush();

            // 得到响应码
            String result = "";

            InputStream inputStream = conn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(
                    inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);// 读字符串用的。
            String inputLine = null;
            // 使用循环来读取获得的数据，把数据都村到result中了
            while (((inputLine = reader.readLine()) != null)) {
                result += inputLine;
            }

            try {
                JSONObject a = new JSONObject(result.trim());
                // 网络传送与服务器验证信息都OK
                if (0 == Integer.parseInt(a.get("result").toString())
                        && "OK".equals(conn.getResponseMessage())) {
                    return "Success";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            reader.close();// 关闭输入流
            outStream.close();
            conn.disconnect();
        }

        // 定义BufferedReader输入流来读取URL的响应
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String line = null;

        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        return buffer.toString();
    }

    /**
     * 文件压缩
     *
     * @param filePath
     */
    public static void compress(String filePath) {
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath);
            File file = new File(Environment.getExternalStorageDirectory()
                    .toString() + "/CONSDCGMPIC/" + "temp.jpg");
            BufferedOutputStream bos = new BufferedOutputStream(
                    new FileOutputStream(file));
            // 设置上传压缩比例
            bitmap.compress(Bitmap.CompressFormat.JPEG,
                    Integer.parseInt(COMPRESSRATIO), bos);
            bos.flush();
            bos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 此方法是用HttpPost方式访问https资源
     *
     * @param url  url地址
     * @param data JSON字串
     * @return String
     * @throws NetManagerUtil.CniiaException.NetworkException
     */
    public String dopostAsStringWithHttps(String url, String data)
            throws CniiaException.NetworkException {
        try {
            EasyX509TrustManager.checkSSL();
            HttpsURLConnection hc;
            if (isWiFiActive()) {
                hc = (HttpsURLConnection) new URL(url).openConnection();
            } else {
                String proxyHost = android.net.Proxy.getDefaultHost();
                if (proxyHost != null) {
                    java.net.Proxy p = new java.net.Proxy(java.net.Proxy.Type.HTTP,
                            new InetSocketAddress(
                                    android.net.Proxy.getDefaultHost(),
                                    android.net.Proxy.getDefaultPort()));
                    hc = (HttpsURLConnection) new URL(url).openConnection(p);
                } else {
                    hc = (HttpsURLConnection) new URL(url).openConnection();
                }
            }
            hc.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded; charset=UTF-8");

            hc.setRequestMethod("POST");
            hc.setDoInput(true);
            hc.setDoOutput(true);
            if (COOKIE != null) {
                hc.setRequestProperty("Cookie", COOKIE);
            }
            OutputStream out = hc.getOutputStream();
            out.write(data.getBytes());
            out.flush();
            out.close();

            InputStream in;
            String str = hc.getHeaderField("Content-Encoding");
            if (str != null && str.equals("gzip")) {
                in = new GZIPInputStream(hc.getInputStream());
            } else {
                in = hc.getInputStream();
            }
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = r.readLine()) != null) {
                sb.append(line);
            }
            in.close();

            String cookie = hc.getHeaderField("set-cookie");
            if (cookie != null && cookie.length() > 0) {
                COOKIE = cookie;
            }
            //便于观察数据
            return sb.toString();
        } catch (Exception e) {
            throw new CniiaException.NetworkException("Net Exception", e);
        }
    }

    public static class CniiaException {
        public static class NetworkException extends Exception {
            private static final long serialVersionUID = 1L;

            public NetworkException(String s, Throwable e) {
                super(s, e);
            }
        }

        public static class SdcardException extends Exception {
            private static final long serialVersionUID = 1L;

            public SdcardException(String s, Throwable e) {
                super(s, e);
            }
        }

        public static class UnknownException extends Exception {
            private static final long serialVersionUID = 1L;

            public UnknownException(String s, Throwable e) {
                super(s, e);
            }
        }
    }
}
