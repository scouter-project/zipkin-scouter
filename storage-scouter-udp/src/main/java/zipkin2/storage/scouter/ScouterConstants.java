package zipkin2.storage.scouter;

import scouter.util.StringUtil;
import zipkin2.storage.scouter.udp.ScouterConfig;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/10/2018
 */
public class ScouterConstants {
    public static final String OBJ_PREFIX = "ZIPKIN/";
    public static final String OBJ_TYPE_PREFIX = "z$";
    public static final String UNKNOWN = "UNKNOWN";

    public static final String toScouterObjName(String name) {
        if (StringUtil.isNotEmpty(name)) {
            return OBJ_PREFIX + name;
        } else {
            return OBJ_PREFIX + UNKNOWN;
        }
    }

    public static final String toScouterObjType(String name, ScouterConfig conf) {
        if (StringUtil.isNotEmpty(name)) {
            return OBJ_TYPE_PREFIX + name;
        } else {
            return "zipkin";
        }
    }
}
