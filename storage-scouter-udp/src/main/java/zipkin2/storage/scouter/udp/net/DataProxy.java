/*
 *  Copyright 2015 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package zipkin2.storage.scouter.udp.net;

import scouter.io.DataOutputX;
import scouter.lang.TextTypes;
import scouter.lang.pack.ObjectPack;
import scouter.lang.pack.Pack;
import scouter.lang.pack.SpanContainerPack;
import scouter.lang.pack.SpanPack;
import scouter.lang.pack.SpanTypes;
import scouter.lang.pack.TextPack;
import scouter.lang.pack.XLogPack;
import scouter.lang.pack.XLogTypes;
import scouter.lang.value.ListValue;
import scouter.lang.value.MapValue;
import scouter.util.HashUtil;
import scouter.util.IntIntLinkedMap;
import scouter.util.IntLinkedSet;
import scouter.util.IntLongLinkedMap;
import scouter.util.StringUtil;
import zipkin2.Annotation;
import zipkin2.Endpoint;
import zipkin2.Span;
import zipkin2.internal.HexCodec;
import zipkin2.storage.scouter.ScouterConstants;
import zipkin2.storage.scouter.udp.ScouterConfig;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class DataProxy {
    private static final Logger logger = Logger.getLogger(DataProxy.class.getName());
    private static UDPDataSendThread udpCollect = UDPDataSendThread.getInstance();
    private static IntIntLinkedMap sqlHash = new IntIntLinkedMap().setMax(5000);

    private static int getSqlHash(String sql) {
        if (sql.length() < 100)
            return HashUtil.hash(sql);
        int id = sql.hashCode();
        int hash = sqlHash.get(id);
        if (hash == 0) {
            hash = HashUtil.hash(sql);
            sqlHash.put(id, hash);
        }
        return hash;
    }

    private static IntLinkedSet sqlText = new IntLinkedSet().setMax(10000);

    public static int sendSqlText(String sql) {
        int hash = getSqlHash(sql);
        if (sqlText.contains(hash)) {
            return hash;
        }
        sqlText.put(hash);
        // udp.add(new TextPack(TextTypes.SQL, hash, sql));
        sendDirect(new TextPack(TextTypes.SQL, hash, sql));
        return hash;
    }

    private static IntLinkedSet serviceName = new IntLinkedSet().setMax(10000);

    public static int sendServiceName(String service) {
        int hash = HashUtil.hash(service);
        sendServiceName(hash, service);
        return hash;
    }

    public static void sendServiceName(int hash, String service) {
        if (serviceName.contains(hash)) {
            return;
        }
        serviceName.put(hash);
        udpCollect.add(new TextPack(TextTypes.SERVICE, hash, service));
    }

    private static IntLinkedSet descTable = new IntLinkedSet().setMax(1000);

    public static int sendDesc(String desc) {
        int hash = HashUtil.hash(desc);
        if (descTable.contains(hash)) {
            return hash;
        }
        descTable.put(hash);
        udpCollect.add(new TextPack(TextTypes.DESC, hash, desc));
        return hash;
    }

    private static IntLinkedSet loginTable = new IntLinkedSet().setMax(10000);

    public static int sendLogin(String loginName) {
        int hash = HashUtil.hash(loginName);
        if (loginTable.contains(hash)) {
            return hash;
        }
        loginTable.put(hash);
        udpCollect.add(new TextPack(TextTypes.LOGIN, hash, loginName));
        return hash;
    }

    private static IntLinkedSet objNameSet = new IntLinkedSet().setMax(10000);

    private static IntLongLinkedMap objSentMap = new IntLongLinkedMap().setMax(1000);

    public static void registerZipkinObj(Span span, ScouterConfig conf) {
        String objName = ScouterConstants.toScouterObjName(span.localServiceName());
        int objHash = HashUtil.hash(objName);

        long registered = objSentMap.get(objHash);
        long now = System.currentTimeMillis();
        if (registered != 0 && now - registered < 30 * 1000) {
            return;
        }

        ObjectPack p = new ObjectPack();
        p.objType = ScouterConstants.toScouterObjType(span.localServiceName(), conf);
        p.objName = objName;
        p.objHash = objHash;
        if (span.localEndpoint() != null) {
            p.address = span.localEndpoint().ipv4() + ":" + span.localEndpoint().portAsInt();
        }
        p.tags.put(scouter.lang.constants.ScouterConstants.TAG_OBJ_DETECTED_TYPE, "zipkin");
        p.tags.put(ObjectPack.TAG_KEY_DEAD_TIME, 300 * 1000);

        objSentMap.put(objHash, now);
        sendHeartBeat(p);
    }

    public static void sendHeartBeat(ObjectPack p) {
        udpCollect.add(p);
    }

    public static int sendObjName(String objName) {
        if (objName == null) {
            return 0;
        }
        int hash = HashUtil.hash(objName);
        sendObjName(hash, objName);
        return hash;
    }

    public static void sendObjName(int hash, String objName) {
        if (objName == null) {
            return;
        }
        if (objNameSet.contains(hash)) {
            return;
        }
        objNameSet.put(hash);
        udpCollect.add(new TextPack(TextTypes.OBJECT, hash, objName));
    }

    private static IntLinkedSet errText = new IntLinkedSet().setMax(10000);

    public static int sendError(String message) {
        int hash = HashUtil.hash(message);
        if (errText.contains(hash)) {
            return hash;
        }
        errText.put(hash);
        udpCollect.add(new TextPack(TextTypes.ERROR, hash, message));
        return hash;
    }

    private static IntLinkedSet hashMessage = new IntLinkedSet().setMax(10000);

    public static int sendHashedMessage(String text) {
        int hash = HashUtil.hash(text);
        if (hashMessage.contains(hash)) {
            return hash;
        }
        hashMessage.put(hash);
        udpCollect.add(new TextPack(TextTypes.HASH_MSG, hash, text));
        return hash;
    }

    public static void reset() {
        serviceName.clear();
        errText.clear();
        sqlText.clear();
        hashMessage.clear();
    }

    public static void sendXLog(XLogPack p) {
        sendDirect(p);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, p.toString());
        }
    }

    static DataUdpAgent udpNet = DataUdpAgent.getInstance();

    public static void sendDirect(Pack p) {
        try {
            udpNet.write(new DataOutputX().writePack(p).toByteArray());
        } catch (IOException e) {
        }
    }

    private static void sendDirect(List<byte[]> buff) {
        switch (buff.size()) {
            case 0:
                return;
            case 1:
                udpNet.write(buff.get(0));
                break;
            default:
                udpNet.write(buff);
                break;
        }
    }

    public static void sendSpanContainer(final List<Span> spans, final ScouterConfig conf) {
        int udpMaxBytes = conf.getUdpPacketMaxBytes();
        if (spans == null || spans.size() == 0)
            return;

        Map<String, List<Span>> spansById = spans.stream()
                .collect(Collectors.groupingBy(Span::traceId));

        for (Map.Entry<String, List<Span>> entry : spansById.entrySet()) {
            SpanContainerPack containerPack = new SpanContainerPack();
            containerPack.gxid = HexCodec.lowerHexToUnsignedLong(entry.getKey());
            containerPack.spanCount = entry.getValue().size();

            List<Span> spanList = entry.getValue();
            spanList.forEach(span -> DataProxy.registerZipkinObj(span, conf));

            List<SpanPack> spanPacks = spanList.stream()
                    .map(DataProxy::makeSpanPack).collect(Collectors.toList());

            List<byte[]> spansBytesList = SpanPack.toBytesList(spanPacks, udpMaxBytes);
            for (byte[] spansBytes : spansBytesList) {
                containerPack.spans = spansBytes;
                sendDirect(containerPack);
            }

            for (SpanPack spanPack : spanPacks) {
                if (SpanTypes.isXLoggable(spanPack.spanType)) {
                    sendXLog(makeXLogPack(spanPack, conf));
                }
            }
        }
    }

    private static SpanPack makeSpanPack(Span span) {
        SpanPack pack = new SpanPack();

        pack.gxid = HexCodec.lowerHexToUnsignedLong(span.traceId());
        pack.txid = HexCodec.lowerHexToUnsignedLong(span.id());
        pack.caller = span.parentId() != null ? HexCodec.lowerHexToUnsignedLong(span.parentId()) : 0L;

        pack.timestamp = span.timestampAsLong() / 1000;
        pack.elapsed = (int) (span.durationAsLong() / 1000);

        pack.spanType = SpanTypes.UNKNOWN;
        if (span.kind() != null) {
            switch (span.kind()) {
                case SERVER:
                    pack.spanType = SpanTypes.SERVER;
                    break;
                case CLIENT:
                    pack.spanType = SpanTypes.CLIENT;
                    break;
                case PRODUCER:
                    pack.spanType = SpanTypes.PRODUCER;
                    break;
                case CONSUMER:
                    pack.spanType = SpanTypes.CONSUMER;
                    break;
            }
        }

        pack.name = sendServiceName(span.name());
        String error = span.tags().get("error");
        if (StringUtil.isNotEmpty(error)) {
            pack.error = sendError(error);
        }

        Endpoint localEndPoint = span.localEndpoint();
        Endpoint remoteEndPoint = span.remoteEndpoint();

        pack.objHash = sendObjName(ScouterConstants.toScouterObjName(span.localServiceName()));
        pack.localEndpointServiceName = sendObjName(span.localServiceName());
        pack.remoteEndpointServiceName = sendObjName(span.remoteServiceName());
        pack.localEndpointIp = localEndPoint != null ? localEndPoint.ipv4Bytes() : null;
        pack.remoteEndpointIp = remoteEndPoint != null ? remoteEndPoint.ipv4Bytes() : null;
        pack.localEndpointPort = localEndPoint != null ? (short) localEndPoint.portAsInt() : 0;
        pack.remoteEndpointPort = remoteEndPoint != null ? (short) remoteEndPoint.portAsInt() : 0;

        Boolean spanDebug = span.debug();
        Boolean spanShared = span.shared();
        pack.debug = spanDebug != null ? spanDebug : false;
        pack.shared = spanShared != null ? spanShared : false;

        pack.annotationTimestamps = new ListValue();
        pack.annotationValues = new ListValue();
        for (Annotation annotation : span.annotations()) {
            pack.annotationTimestamps.add(annotation.timestamp() / 1000);
            pack.annotationValues.add(annotation.value());
        }

        pack.tags = MapValue.ofStringValueMap(span.tags());

        return pack;
    }

    public static XLogPack makeXLogPack(SpanPack pack, ScouterConfig conf) {
        XLogPack xlog = new XLogPack();
        xlog.endTime = pack.timestamp + pack.elapsed;
        xlog.objHash = pack.objHash;
        xlog.service = pack.name;
        xlog.txid = pack.txid;
        xlog.gxid = pack.gxid;
        xlog.caller = pack.caller;
        xlog.elapsed = pack.elapsed;
        xlog.error = pack.error;
        xlog.xType = XLogTypes.ZIPKIN_SPAN;

        String loginTag = conf.getLoginTag();
        if (StringUtil.isNotEmpty(loginTag) && StringUtil.isNotEmpty(pack.tags.getText(loginTag))) {
            xlog.login = sendLogin(pack.tags.getText(loginTag));
        }

        String text1Tag = conf.getText1Tag();
        if (StringUtil.isNotEmpty(text1Tag) && StringUtil.isNotEmpty(pack.tags.getText(text1Tag))) {
            xlog.text1 = pack.tags.getText(text1Tag);
        }

        return xlog;
    }
}
