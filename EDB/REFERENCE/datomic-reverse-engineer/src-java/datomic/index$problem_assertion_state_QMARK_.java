/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentHashSet
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentHashSet;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;

public final class index$problem_assertion_state_QMARK_
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"mid-index");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"contains?");
    public static final AFn const__4 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"segmented"), RT.keyword(null, (String)"separated")});
    public static final Keyword const__5 = RT.keyword(null, (String)"history");
    public static final AFn const__7 = (AFn)PersistentHashSet.create((Object[])new Object[]{RT.keyword(null, (String)"absent"), RT.keyword(null, (String)"segmented"), RT.keyword(null, (String)"separated")});
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object tier, Object state2) {
        Object object = tier;
        tier = null;
        Object G__15686 = object;
        switch (Util.hash((Object)G__15686) >> 0 & 1) {
            case 0: {
                if (G__15686 != const__0) break;
                Object object2 = state2;
                state2 = null;
                Object object3 = ((IFn)const__1.getRawRoot()).invoke((Object)const__4, object2);
                return object3;
            }
            case 1: {
                if (G__15686 != const__5) break;
                Object object4 = state2;
                state2 = null;
                Object object3 = ((IFn)const__1.getRawRoot()).invoke((Object)const__7, object4);
                return object3;
            }
        }
        Object object5 = G__15686;
        G__15686 = null;
        throw (Throwable)new IllegalArgumentException((String)((IFn)const__8.getRawRoot()).invoke((Object)"No matching clause: ", object5));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$problem_assertion_state_QMARK_.invokeStatic(object3, object4);
    }
}

