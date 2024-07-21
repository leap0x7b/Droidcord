package leap.droidcord;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

import cc.nnproject.json.JSON;
import cc.nnproject.json.JSONException;
import cc.nnproject.json.JSONObject;

public class HTTPThing {
    State s;
    String api;
    String token;

    public HTTPThing(State s, String api, String token) {
        this.s = s;
        this.api = api;
        this.token = token.trim();
    }

    private static byte[] readBytes(InputStream inputStream, int initialSize,
                                    int bufferSize, int expandSize) throws IOException {
        if (initialSize <= 0)
            initialSize = bufferSize;
        byte[] buf = new byte[initialSize];
        int count = 0;
        byte[] readBuf = new byte[bufferSize];
        int readLen;
        while ((readLen = inputStream.read(readBuf)) != -1) {
            if (count + readLen > buf.length) {
                byte[] newbuf = new byte[count + expandSize];
                System.arraycopy(buf, 0, newbuf, 0, count);
                buf = newbuf;
            }
            System.arraycopy(readBuf, 0, buf, count, readLen);
            count += readLen;
        }
        if (buf.length == count) {
            return buf;
        }
        byte[] res = new byte[count];
        System.arraycopy(buf, 0, res, 0, count);
        return res;
    }

    public HttpURLConnection openConnection(String url) throws IOException {
        String fullUrl = api + "/api/v9" + url;

        if (s.tokenType == State.TOKEN_TYPE_QUERY) {
            if (fullUrl.indexOf("?") != -1) {
                fullUrl += "&token=" + token;
            } else {
                fullUrl += "?token=" + token;
            }
        }

        HttpURLConnection c = (HttpURLConnection) new URL(fullUrl)
                .openConnection();
        c.setDoOutput(true);

        if (s.tokenType == State.TOKEN_TYPE_HEADER) {
            c.addRequestProperty("Content-Type", "application/json");
            c.addRequestProperty("Authorization", token);
        }

        return c;
    }

    public String sendRequest(HttpURLConnection c) throws Exception {
        c.setRequestMethod("GET");

        InputStream is = null;
        is = c.getInputStream();

        try {
            int respCode = c.getResponseCode();

            // Read response
            StringBuffer stringBuffer = new StringBuffer();
            int ch;
            while ((ch = is.read()) != -1) {
                stringBuffer.append((char) ch);
            }
            String response = stringBuffer.toString().trim();

            if (respCode == HttpURLConnection.HTTP_OK) {
                return response;
            }
            if (respCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new Exception("Check your token");
            }

            try {
                String message = JSON.getObject(response).getString("message");
                throw new Exception(message);
            } catch (JSONException e) {
                throw new Exception("HTTP error " + respCode);
            }
        } finally {
            if (is != null)
                is.close();
        }
    }

    private String sendData(String method, String url, String data)
            throws Exception {
        HttpURLConnection c = null;
        OutputStream os = null;

        try {
            c = openConnection(url);
            c.setRequestMethod(method);

            byte[] b;
            try {
                b = data.getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                b = data.getBytes();
            }

            if (s.tokenType == State.TOKEN_TYPE_HEADER) {
                c.setRequestProperty("Content-Length", String.valueOf(b.length));
            }

            os = c.getOutputStream();
            os.write(b);

            return sendRequest(c);
        } finally {
            if (os != null)
                os.close();
        }
    }

    private String sendJson(String method, String url, JSONObject data)
            throws Exception {
        if (s.tokenType == State.TOKEN_TYPE_JSON)
            data.put("token", token);
        return sendData(method, url, data.build());
    }

    public String get(String url) throws Exception {
        if (s.tokenType == State.TOKEN_TYPE_JSON) {
            JSONObject tokenJson = new JSONObject();
            tokenJson.put("token", token);
            return get(url, tokenJson);
        }

        HttpURLConnection c = null;

        c = openConnection(url);
        c.setRequestMethod("GET");
        return sendRequest(c);
    }

    public String post(String url, String data) throws Exception {
        return sendData("POST", url, data);
    }

    public String get(String url, String data) throws Exception {
        return sendData("GET", url, data);
    }

    public String post(String url, JSONObject data) throws Exception {
        return sendJson("POST", url, data);
    }

    public String get(String url, JSONObject data) throws Exception {
        return sendJson("GET", url, data);
    }

    public Bitmap getImage(String url) throws IOException {
        byte[] b = getBytes(url);
        return BitmapFactory.decodeByteArray(b, 0, b.length);
    }

    public byte[] getBytes(String url) throws IOException {
        HttpURLConnection hc = null;
        InputStream in = null;
        try {
            hc = open(url);
            int r;
            if ((r = hc.getResponseCode()) >= 400) {
                throw new IOException("HTTP " + r);
            }
            in = hc.getInputStream();
            return readBytes(in, (int) hc.getContentLength(), 1024, 2048);
        } finally {
            if (in != null)
                in.close();
        }
    }

    private HttpURLConnection open(String url) throws IOException {
        HttpURLConnection hc = (HttpURLConnection) new URL(url)
                .openConnection();
        hc.setRequestMethod("GET");
        if (s.tokenType == State.TOKEN_TYPE_HEADER) {
            hc.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (X11; Linux x86_64; rv:125.0) Gecko/20100101 Firefox/125.0");
        }
        return hc;
    }
}