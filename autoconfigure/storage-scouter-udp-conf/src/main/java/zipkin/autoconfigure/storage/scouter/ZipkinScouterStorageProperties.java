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

package zipkin.autoconfigure.storage.scouter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import zipkin2.storage.scouter.udp.ScouterConfig;
import zipkin2.storage.scouter.udp.ScouterUDPStorage;

import java.io.Serializable;
import java.util.Map;
import java.util.logging.Logger;

@ConfigurationProperties("zipkin.storage.scouter")
public class ZipkinScouterStorageProperties implements Serializable {
    private static final long serialVersionUID = 0L;
    private static final Logger logger = Logger.getLogger(ZipkinScouterStorageProperties.class.getName());

    /**
     * Scouter Collector UDP address; defaults to localhost:6100
     */
    private String scouterCollectorAddress;
    private int scouterCollectorPort;
    private int scouterUdpPacketMaxBytes;
    private Map<String, String> tagMap;
    private String serviceMapsToObjType;

    public String getScouterCollectorAddress() {
        return scouterCollectorAddress;
    }
    public void setScouterCollectorAddress(String scouterCollectorAddress) {
        this.scouterCollectorAddress = scouterCollectorAddress;
    }

    public int getScouterCollectorPort() {
        return scouterCollectorPort;
    }
    public void setScouterCollectorPort(int scouterCollectorPort) {
        this.scouterCollectorPort = scouterCollectorPort;
    }

    public int getScouterUdpPacketMaxBytes() {
        return scouterUdpPacketMaxBytes;
    }
    public void setScouterUdpPacketMaxBytes(int scouterUdpPacketMaxBytes) {
        this.scouterUdpPacketMaxBytes = scouterUdpPacketMaxBytes;
    }

    public Map<String, String> getTagMap() {
        return tagMap;
    }

    public void setTagMap(Map<String, String> tagMap) {
        this.tagMap = tagMap;
    }

    public String getServiceMapsToObjType() {
        return serviceMapsToObjType;
    }

    public void setServiceMapsToObjType(String serviceMapsToObjType) {
        this.serviceMapsToObjType = serviceMapsToObjType;
    }

    public ScouterUDPStorage.Builder toBuilder() {
        logger.info("[zipkin-scouter-storage] loading.");
        ScouterConfig config = new ScouterConfig(scouterCollectorAddress, scouterCollectorPort, scouterUdpPacketMaxBytes, tagMap, serviceMapsToObjType);
        logger.info("[zipkin-scouter-storage] config " + config.toString());
        ScouterUDPStorage.Builder builder = ScouterUDPStorage.newBuilder();
        if (scouterCollectorAddress != null) builder.config(config);
        return builder;
    }
}
