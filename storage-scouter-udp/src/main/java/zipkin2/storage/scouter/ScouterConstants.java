package zipkin2.storage.scouter;

import scouter.util.StringUtil;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 31/10/2018
 */
public class ScouterConstants {
    public static final String OBJ_PREFIX = "ZIPKIN/";
    public static final String UNKNOWN = "UNKNOWN";

    public static final String toScouterObjName(String name) {
        if (StringUtil.isNotEmpty(name)) {
            return OBJ_PREFIX + name;
        } else {
            return OBJ_PREFIX + UNKNOWN;
        }
    }
}
