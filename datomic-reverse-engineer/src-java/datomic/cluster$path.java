/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;

public final class cluster$path
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"tenant");
    public static final Keyword const__4 = RT.keyword(null, (String)"db");
    public static final Keyword const__5 = RT.keyword(null, (String)"partition");
    public static final Keyword const__6 = RT.keyword(null, (String)"key");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"pr-str");
    public static final AFn const__9 = (AFn)Symbol.intern(null, (String)"key");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"format");

    public static Object invokeStatic(Object args) {
        Object object;
        Object object2;
        Object object3;
        Object key;
        Object object4;
        Object object5 = args;
        args = null;
        Object map__10420 = object5;
        Object object6 = ((IFn)const__0.getRawRoot()).invoke(map__10420);
        if (object6 != null && object6 != Boolean.FALSE) {
            Object object7 = map__10420;
            map__10420 = null;
            object4 = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object7)));
        } else {
            object4 = map__10420;
            map__10420 = null;
        }
        Object map__104202 = object4;
        Object tenant = RT.get((Object)map__104202, (Object)const__3);
        Object db2 = RT.get((Object)map__104202, (Object)const__4);
        Object partition = RT.get((Object)map__104202, (Object)const__5);
        Object object8 = map__104202;
        map__104202 = null;
        Object object9 = key = RT.get((Object)object8, (Object)const__6);
        if (object9 == null || object9 == Boolean.FALSE) {
            throw (Throwable)((Object)new AssertionError(((IFn)const__7.getRawRoot()).invoke((Object)"Assert failed: ", ((IFn)const__8.getRawRoot()).invoke((Object)const__9))));
        }
        IFn iFn = (IFn)const__7.getRawRoot();
        Object object10 = tenant;
        if (object10 != null && object10 != Boolean.FALSE) {
            Object object11 = tenant;
            tenant = null;
            object3 = ((IFn)const__10.getRawRoot()).invoke((Object)"%s/", object11);
        } else {
            object3 = null;
        }
        Object object12 = db2;
        if (object12 != null && object12 != Boolean.FALSE) {
            Object object13 = db2;
            db2 = null;
            object2 = ((IFn)const__10.getRawRoot()).invoke((Object)"%s/", object13);
        } else {
            object2 = null;
        }
        Object object14 = partition;
        if (object14 != null && object14 != Boolean.FALSE) {
            Object object15 = partition;
            partition = null;
            object = ((IFn)const__10.getRawRoot()).invoke((Object)"%03X/", object15);
        } else {
            object = null;
        }
        Object object16 = key;
        key = null;
        return iFn.invoke(object3, object2, object, ((IFn)const__10.getRawRoot()).invoke((Object)"%s", object16));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return cluster$path.invokeStatic(object2);
    }
}

