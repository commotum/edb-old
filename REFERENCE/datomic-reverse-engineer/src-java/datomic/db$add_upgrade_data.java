/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.db$add_upgrade_data$fn__13617;
import java.util.Date;

public final class db$add_upgrade_data
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"basisT");
    public static final Keyword const__4 = RT.keyword(null, (String)"nextT");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"assoc");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"partition");
    public static final Object const__9 = 2L;

    public static Object invokeStatic(Object db2, Object system_data) {
        Date epoch;
        Object object;
        Object map__13615 = db2;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__13615);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = map__13615;
            map__13615 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object3)));
        } else {
            object = map__13615;
            map__13615 = null;
        }
        Object map__136152 = object;
        Object basisT = RT.get((Object)map__136152, (Object)const__3);
        Object object4 = map__136152;
        map__136152 = null;
        Object nextT = RT.get((Object)object4, (Object)const__4);
        Date date = epoch = new Date(0L);
        epoch = null;
        Object object5 = db2;
        db2 = null;
        Object object6 = system_data;
        system_data = null;
        Object object7 = basisT;
        basisT = null;
        Object object8 = nextT;
        nextT = null;
        return ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke((Object)new db$add_upgrade_data$fn__13617(date), object5, ((IFn)const__8.getRawRoot()).invoke(const__9, object6)), (Object)const__3, object7, (Object)const__4, object8);
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$add_upgrade_data.invokeStatic(object3, object4);
    }
}

