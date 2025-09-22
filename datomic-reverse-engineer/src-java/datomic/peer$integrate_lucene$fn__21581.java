/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;

public final class peer$integrate_lucene$fn__21581
extends AFunction {
    Object q;
    Object tx;
    public static final Var const__0 = RT.var((String)"datomic.queue", (String)"poll");
    public static final Keyword const__2 = RT.keyword(null, (String)"done");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"conj");

    public peer$integrate_lucene$fn__21581(Object object, Object object2) {
        this.q = object;
        this.tx = object2;
    }

    public Object invoke() {
        Object txes = Tuple.create((Object)this.tx);
        Object ntx = ((IFn)const__0.getRawRoot()).invoke(this.q);
        while (true) {
            boolean or__5238__auto__21583;
            if ((or__5238__auto__21583 = Util.equiv((Object)ntx, (Object)const__2)) ? or__5238__auto__21583 : Util.identical((Object)ntx, null)) break;
            IPersistentVector iPersistentVector = txes;
            txes = null;
            Object object = ntx;
            ntx = null;
            ntx = ((IFn)const__0.getRawRoot()).invoke(this.q);
            txes = ((IFn)const__4.getRawRoot()).invoke((Object)iPersistentVector, object);
        }
        IPersistentVector iPersistentVector = txes;
        txes = null;
        Object object = ntx;
        ntx = null;
        return Tuple.create((Object)iPersistentVector, (Object)object);
    }
}

