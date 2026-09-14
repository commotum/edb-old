/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class db$memlog_txes_since
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"get-in");
    public static final AFn const__3 = (AFn)Tuple.create((Object)RT.keyword(null, (String)"memlog"), (Object)RT.keyword(null, (String)"txes"));
    public static final Keyword const__4 = RT.keyword(null, (String)"t");
    public static final Var const__5 = RT.var((String)"datomic.common", (String)"key-comparator");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"subvec");

    public static Object invokeStatic(Object db2, Object t) {
        Object object = db2;
        db2 = null;
        Object memlog2 = ((IFn)const__0.getRawRoot()).invoke(object, (Object)const__3);
        Object[] objectArray = new Object[2];
        objectArray[0] = const__4;
        Object object2 = t;
        t = null;
        objectArray[1] = object2;
        int idx = Collections.binarySearch((List)memlog2, RT.mapUniqueKeys((Object[])objectArray), (Comparator)((IFn)const__5.getRawRoot()).invoke((Object)const__4));
        Object object3 = memlog2;
        memlog2 = null;
        return ((IFn)const__6.getRawRoot()).invoke(object3, (Object)((long)idx < 0L ? (Number)Numbers.num((long)Numbers.unchecked_minus((long)((long)idx + 1L))) : (Number)Numbers.num((long)Numbers.unchecked_inc((long)idx))));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return db$memlog_txes_since.invokeStatic(object3, object4);
    }
}

