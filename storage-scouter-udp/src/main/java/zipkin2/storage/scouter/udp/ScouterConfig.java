/*
 *  Copyright 2015-2018 the original author or authors.
 *  @https://github.com/scouter-project/scouter
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package zipkin2.storage.scouter.udp;

import scouter.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

public final class ScouterConfig {
    String address;
    int port;
    int udpPacketMaxBytes;
    String loginTag;
    String descTag;
    String text1Tag;
    String text2Tag;
    String text3Tag;
    String text4Tag;
    String text5Tag;
    Map<String, String> seviceToObjTypeMap = new HashMap<>();

    public ScouterConfig(String address, int port, int udpPacketMaxBytes,
                         Map<String, String> tagMap, String serviceMapsToObjType) {
        this.address = address;
        this.port = port;
        this.udpPacketMaxBytes = udpPacketMaxBytes;
        this.loginTag = tagMap.get("login");
        this.descTag = tagMap.get("desc");
        this.text1Tag = tagMap.get("text1");
        this.text2Tag = tagMap.get("text2");
        this.text3Tag = tagMap.get("text3");
        this.text4Tag = tagMap.get("text4");
        this.text5Tag = tagMap.get("text5");

        if (StringUtil.isNotEmpty(serviceMapsToObjType)) {
            seviceToObjTypeMap = Stream.of(serviceMapsToObjType.split(","))
                    .map(str -> str.split("="))
                    .collect(toMap(kv -> kv[0], kv -> kv[1]));
        }
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    public int getUdpPacketMaxBytes() {
        return udpPacketMaxBytes;
    }

    public String getLoginTag() {
        return loginTag;
    }

    public String getDescTag() {
        return descTag;
    }

    public String getText1Tag() {
        return text1Tag;
    }

    public String getText2Tag() {
        return text2Tag;
    }

    public String getText3Tag() {
        return text3Tag;
    }

    public String getText4Tag() {
        return text4Tag;
    }

    public String getText5Tag() {
        return text5Tag;
    }

    @Override
    public String toString() {
        return "ScouterConfig{" +
                "address='" + address + '\'' +
                ", port=" + port +
                ", udpPacketMaxBytes=" + udpPacketMaxBytes +
                ", loginTag='" + loginTag + '\'' +
                ", descTag='" + descTag + '\'' +
                ", text1Tag='" + text1Tag + '\'' +
                ", text2Tag='" + text2Tag + '\'' +
                ", text3Tag='" + text3Tag + '\'' +
                ", text4Tag='" + text4Tag + '\'' +
                ", text5Tag='" + text5Tag + '\'' +
                ", seviceToObjTypeMap=" + seviceToObjTypeMap +
                '}';
    }
}
