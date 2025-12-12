/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.log$tx_range$reify__16479$fn__16480;
import java.util.Iterator;

public final class log$tx_range$reify__16479
implements Iterable,
IObj {
    final IPersistentMap __meta;
    Object end;
    Object log;
    Object start;
    public static final Var const__0 = RT.var((String)"datomic.iter", (String)"iterator");
    public static final Var const__1 = RT.var((String)"datomic.log", (String)"seek-tx");
    public static final Var const__2 = RT.var((String)"datomic.iter", (String)"take-while");

    public log$tx_range$reify__16479(IPersistentMap iPersistentMap, Object object, Object object2, Object object3) {
        this.__meta = iPersistentMap;
        this.end = object;
        this.log = object2;
        this.start = object3;
    }

    public log$tx_range$reify__16479(Object object, Object object2, Object object3) {
        this(null, object, object2, object3);
    }

    public IPersistentMap meta() {
        return this.__meta;
    }

    public IObj withMeta(IPersistentMap iPersistentMap) {
        return new log$tx_range$reify__16479(iPersistentMap, this.end, this.log, this.start);
    }

    public Iterator iterator() {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object ret = ((IFn)const__1.getRawRoot()).invoke(this_.log, this_.start);
        Object object2 = this_.end;
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ret;
            ret = null;
            object = ((IFn)const__2.getRawRoot()).invoke((Object)new log$tx_range$reify__16479$fn__16480(this_.end), object3);
        } else {
            object = ret;
            Object var1_1 = null;
        }
        log$tx_range$reify__16479 this_ = null;
        return (Iterator)iFn.invoke(object);
    }
}

