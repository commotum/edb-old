/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.impl.db.IDatum;

public final class log$write_excised_log$fn__16409$fn__16421$fn__16426
extends AFunction {
    Object ts;
    Object excise_QMARK_;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"data");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"contains?");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"remove");

    public log$write_excised_log$fn__16409$fn__16421$fn__16426(Object object, Object object2) {
        this.ts = object;
        this.excise_QMARK_ = object2;
    }

    public Object invoke(Object p__16425) {
        Object object;
        Object map__16427;
        Object object2;
        Object object3 = p__16425;
        p__16425 = null;
        Object map__164272 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__164272);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__164272;
            map__164272 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__164272;
            map__164272 = null;
        }
        Object tx = map__16427 = object2;
        Object object6 = map__16427;
        map__16427 = null;
        Object data2 = RT.get((Object)object6, (Object)const__3);
        long t = ((IDatum)((IFn)const__4.getRawRoot()).invoke(data2)).getT();
        Object object7 = ((IFn)const__5.getRawRoot()).invoke(this_.ts, (Object)Numbers.num((long)t));
        if (object7 != null && object7 != Boolean.FALSE) {
            Object object8 = tx;
            tx = null;
            Object object9 = data2;
            data2 = null;
            log$write_excised_log$fn__16409$fn__16421$fn__16426 this_ = null;
            object = ((IFn)const__6.getRawRoot()).invoke(object8, (Object)const__3, ((IFn)const__7.getRawRoot()).invoke(this_.excise_QMARK_, object9));
        } else {
            object = tx;
            tx = null;
        }
        return object;
    }
}

