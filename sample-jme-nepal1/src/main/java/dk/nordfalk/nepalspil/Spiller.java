package dk.nordfalk.nepalspil;

import com.jme3.math.Transform;
import com.jme3.scene.Node;

class Spiller {
    final Node node;
    private final String navn;
    int feltNr;
    Transform rykFra;
    Transform rykTil;

    Spiller(Node laxmiBrik, String laxmi) {
        node = laxmiBrik;
        navn = laxmi;
    }
}
