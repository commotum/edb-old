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

public final class ddb_s3_cluster$storage_path
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"system");
    public static final Keyword const__4 = RT.keyword(null, (String)"db-id");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"format");

    public static Object invokeStatic(Object p__22769) {
        Object object;
        Object object2;
        Object object3 = p__22769;
        p__22769 = null;
        Object map__22770 = object3;
        Object object4 = ((IFn)const__0.getRawRoot()).invoke(map__22770);
        if (object4 != null && object4 != Boolean.FALSE) {
            Object object5 = map__22770;
            map__22770 = null;
            object2 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object5)));
        } else {
            object2 = map__22770;
            map__22770 = null;
        }
        Object map__227702 = object2;
        Object system = RT.get((Object)map__227702, (Object)const__3);
        Object object6 = map__227702;
        map__227702 = null;
        Object db_id = RT.get((Object)object6, (Object)const__4);
        IFn iFn = (IFn)const__5.getRawRoot();
        Object object7 = system;
        system = null;
        Object object8 = ((IFn)const__6.getRawRoot()).invoke((Object)"/%s", object7);
        Object object9 = db_id;
        if (object9 != null && object9 != Boolean.FALSE) {
            Object object10 = db_id;
            db_id = null;
            object = ((IFn)const__6.getRawRoot()).invoke((Object)"/%s", object10);
        } else {
            object = null;
        }
        return iFn.invoke(object8, (Object)"/data", object, (Object)"/vals");
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb_s3_cluster$storage_path.invokeStatic(object2);
    }
}

