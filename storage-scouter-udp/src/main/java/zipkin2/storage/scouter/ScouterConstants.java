package zipkin2.storage.scouter;

import scouter.util.StringUtil;
import zipkin2.storage.scouter.udp.ScouterConfig;

import java.util.Map;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/10/2018
 */
public class ScouterConstants {
    public static final String OBJ_PREFIX = "ZIPKIN/";
    public static final String OBJ_TYPE_PREFIX = "z$";
    public static final String UNKNOWN = "UNKNOWN";

    public static String toScouterObjName(String name) {
        if (StringUtil.isNotEmpty(name)) {
            return OBJ_PREFIX + name;
        } else {
            return OBJ_PREFIX + UNKNOWN;
        }
    }

    public static String toScouterObjType(String name, ScouterConfig conf) {
        if (StringUtil.isNotEmpty(name)) {
            Map<String, String> serviceToType = conf.getServiceToObjTypeMap();
            return OBJ_TYPE_PREFIX + serviceToType.getOrDefault(name, name);
        } else {
            return "zipkin";
        }
    }
}
