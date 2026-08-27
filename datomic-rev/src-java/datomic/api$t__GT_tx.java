/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$LO
 *  clojure.lang.RT
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import datomic.Peer;

public final class api$t__GT_tx
extends AFunction
implements IFn.LO {
    public static Object invokeStatic(long t) {
        return Peer.toTx(t);
    }

    public Object invoke(Object object) {
        return api$t__GT_tx.invokeStatic(RT.longCast((Object)((Number)object)));
    }

    public final Object invokePrim(long l) {
        return api$t__GT_tx.invokeStatic(l);
    }
}

