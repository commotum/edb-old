/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package datomic.valcache_direct;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ValcacheDirect$fn__9916
extends AFunction {
    Object k;
    Object root;
    public static final Var const__0 = RT.var((String)"datomic.valcache", (String)"direct-get");
    public static final Var const__1 = RT.var((String)"datomic.slf4j", (String)"process");
    public static final AFn const__4 = (AFn)RT.map((Object[])new Object[]{RT.keyword(null, (String)"event"), RT.keyword((String)"valcache", (String)"get-exception")});
    public static final Var const__5 = RT.var((String)"datomic.slf4j", (String)"caused-by");

    public ValcacheDirect$fn__9916(Object object, Object object2) {
        this.k = object;
        this.root = object2;
    }

    public Object invoke() {
        Throwable throwable;
        try {
            IPersistentVector iPersistentVector;
            try {
                iPersistentVector = Tuple.create((Object)((IFn)const__0.getRawRoot()).invoke(this.root, this.k));
            }
            catch (Throwable t2) {
                Logger logger = LoggerFactory.getLogger((String)"datomic.valcache-direct");
                Throwable ex = t2;
                if (logger.isInfoEnabled()) {
                    logger.info((String)((IFn)const__1.getRawRoot()).invoke((Object)const__4), ex);
                    Logger logger2 = logger;
                    logger = null;
                    Throwable throwable2 = ex;
                    ex = null;
                    ((IFn)const__5.getRawRoot()).invoke((Object)logger2, (Object)throwable2);
                }
                Object t2 = null;
                iPersistentVector = Tuple.create(null, (Object)t2);
            }
            throwable = iPersistentVector;
        }
        catch (Throwable e2) {
            Object e2 = null;
            throwable = e2;
        }
        return throwable;
    }
}

