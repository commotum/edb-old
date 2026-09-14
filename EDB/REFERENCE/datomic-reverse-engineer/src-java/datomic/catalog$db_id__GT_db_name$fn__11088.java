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

public final class catalog$db_id__GT_db_name$fn__11088
extends AFunction {
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__6 = RT.keyword(null, (String)"db-id");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"assoc");

    public Object invoke(Object m, Object p__11087) {
        Object object;
        Object db_id;
        Object map__11092;
        Object object2;
        Object object3 = p__11087;
        p__11087 = null;
        Object vec__11089 = object3;
        Object k = RT.nth((Object)vec__11089, (int)RT.intCast((long)0L), null);
        Object object4 = vec__11089;
        vec__11089 = null;
        Object map__110922 = RT.nth((Object)object4, (int)RT.intCast((long)1L), null);
        Object object5 = ((IFn)const__3.getRawRoot()).invoke(map__110922);
        if (object5 != null && object5 != Boolean.FALSE) {
            Object object6 = map__110922;
            map__110922 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__4.getRawRoot()).invoke(object6)));
        } else {
            object2 = map__110922;
            map__110922 = null;
        }
        Object object7 = map__11092 = object2;
        map__11092 = null;
        Object object8 = db_id = RT.get((Object)object7, (Object)const__6);
        if (object8 != null && object8 != Boolean.FALSE) {
            Object object9 = m;
            m = null;
            Object object10 = db_id;
            db_id = null;
            Object object11 = k;
            k = null;
            catalog$db_id__GT_db_name$fn__11088 this_ = null;
            object = ((IFn)const__7.getRawRoot()).invoke(object9, object10, object11);
        } else {
            object = m;
            Object var1_1 = null;
        }
        return object;
    }
}

