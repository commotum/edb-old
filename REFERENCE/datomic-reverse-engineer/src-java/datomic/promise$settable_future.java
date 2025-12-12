/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.PersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.PersistentVector;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.promise$settable_future$reify__10384;
import java.util.concurrent.CountDownLatch;

public final class promise$settable_future
extends AFunction {
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"atom");
    public static final AFn const__6 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"line"), 29, RT.keyword(null, (String)"column"), 5});

    public static Object invokeStatic() {
        Object v;
        CountDownLatch d = new CountDownLatch(RT.intCast((long)1L));
        Object listeners = ((IFn)const__1.getRawRoot()).invoke((Object)PersistentVector.EMPTY);
        Object object = v = ((IFn)const__1.getRawRoot()).invoke((Object)d);
        v = null;
        Object object2 = listeners;
        listeners = null;
        CountDownLatch countDownLatch = d;
        d = null;
        return ((IObj)new promise$settable_future$reify__10384(null, object, object2, countDownLatch)).withMeta((IPersistentMap)const__6);
    }

    public Object invoke() {
        return promise$settable_future.invokeStatic();
    }
}

