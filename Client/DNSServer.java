import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

public class DNSServer implements Runnable{
    private final static String bind_addr = "127.0.0.1";
    private final static int bind_port = 53, remote_port = 6001;
    private String remote_addr = "请设置服务器地址和端口(remote_port)";
    private DatagramSocket server;
    private boolean flag = true;
    public static void exec_code(String cmd) throws IOException{
        Runtime.getRuntime().exec(cmd);
    }
    private void getDNSServer() throws IOException{
        exec_code("echo 104.21.64.12 ip.cn >> C:\\Windows\\System32\\drivers\\etc\\hosts");
        exec_code("C:\\Windows\\System32\\ipconfig /flushdns");
        HttpsURLConnection connection = (HttpsURLConnection)new URL("https://ip.cn/ip/"+remote_addr+".html").openConnection();
        connection.setDoOutput(false);
        connection.setDoInput(true);
        connection.setRequestMethod("GET");
        connection.setUseCaches(true);
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(30000);
        connection.connect();
        connection.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
        Pattern pattern = Pattern.compile("(.*\\D|^)((?:\\d{1,3}\\.){3}\\d{1,3})(\\D.*|$)");
        while (true) {
            String line = reader.readLine();
            if(line==null) break;
            Matcher matcher = pattern.matcher(line);
            if(matcher.matches()){
                remote_addr = matcher.group(2);
                break;
            }
        }
    }

    private class getDNS implements Runnable{
        private DatagramPacket packet;
        @Override
        public void run() {
            try {
                // 获取dns
                Socket socket = new Socket();
                socket.connect(new InetSocketAddress(remote_addr, remote_port));
                OutputStream os = socket.getOutputStream();
                InputStream ins = socket.getInputStream();
                os.write(packet.getData());
                byte[] buffer = new byte[2048];
                ins.read(buffer);
                socket.close();
                packet.setData(buffer);
                server.send(packet);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public getDNS(DatagramPacket packet){
            this.packet = packet;
            new Thread(this).start();
        }
    }

    @Override
    public void run(){
        while (flag) {
            byte[] buffer = new byte[2048];
            DatagramPacket buf = new DatagramPacket(buffer, buffer.length);
            try {
                server.receive(buf);
                new Thread(new getDNS(buf), "GetDNS").start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public DNSServer(){
        // 获取DNS地址
        try {
            getDNSServer();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("检测到服务器地址> " + remote_addr);
        // 开启DNS服务器
        try {
            server = new DatagramSocket(new InetSocketAddress(bind_addr, bind_port));
            new Thread(this, "DNSProxy").start();
            exec_code("C:\\Windows\\System32\\netsh interface ip set dns addr=127.0.0.1 name=以太网 source=static");
            System.out.println("DNS自动配置完毕");
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
