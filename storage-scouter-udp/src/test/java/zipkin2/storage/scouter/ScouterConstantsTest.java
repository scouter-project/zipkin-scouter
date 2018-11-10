package zipkin2.storage.scouter;

import org.junit.Test;
import zipkin2.storage.scouter.udp.ScouterConfig;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author Gun Lee (gunlee01@gmail.com) on 10/11/2018
 */
public class ScouterConstantsTest {

    @Test
    public void toScouterObjName() {
        String name = "test-name";
        String expected = "ZIPKIN/test-name";

        assertEquals(expected, ScouterConstants.toScouterObjName(name));
    }

    @Test
    public void toScouterObjType() {
        ScouterConfig config = new ScouterConfig("localhost", 6100, 60000,
                new HashMap<>(), "s1=xxs1,s2=xxs2");

        assertEquals("z$nomap", ScouterConstants.toScouterObjType("nomap", config));
        assertEquals("z$xxs1", ScouterConstants.toScouterObjType("s1", config));
        assertEquals("z$xxs2", ScouterConstants.toScouterObjType("s2", config));
    }
}