/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.infinispan.client.hotrod.Flag
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import org.infinispan.client.hotrod.Flag;

public final class kv_hotrod$fn__17796
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"into-array");

    public static Object invokeStatic() {
        return ((IFn)const__0.getRawRoot()).invoke((Object)Tuple.create((Object)Flag.FORCE_RETURN_VALUE));
    }

    public Object invoke() {
        return kv_hotrod$fn__17796.invokeStatic();
    }
}

