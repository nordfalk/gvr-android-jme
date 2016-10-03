package jme3test.android.nepal1;

import com.jme3.math.Transform;
import com.jme3.scene.Node;

/**
 * <p>
 * Project : gvr-android-jme<br>
 * Package : jme3test.android.nepal1<br>
 * Created by : rudz<br>
 * On : okt.03.2016 - 09:26
 * </p>
 */

public class Spiller {
    public final Node node;
    public final String navn;
    int feltNr;
    Transform rykFra;
    Transform rykTil;

    Spiller(Node laxmiBrik, String laxmi) {
        node = laxmiBrik;
        navn = laxmi;
    }
}
