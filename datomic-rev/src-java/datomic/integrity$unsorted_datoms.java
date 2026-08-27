/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import datomic.integrity$unsorted_datoms$fn__22084;

public final class integrity$unsorted_datoms
extends AFunction {
    public static final Keyword const__0 = RT.keyword(null, (String)"allow-duplicates");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"<=");
    public static final Keyword const__2 = RT.keyword(null, (String)"strict");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"<");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__5 = RT.var((String)"datomic.integrity", (String)"datom-comparator");
    public static final Var const__6 = RT.var((String)"datomic.tools", (String)"unsorted-seq");
    public static final Var const__7 = RT.var((String)"datomic.api", (String)"datoms");

    /*
     * Enabled aggressive block sorting
     */
    public static Object invokeStatic(Object db2, Object sort, Object order, Object progress) {
        Object object;
        block4: {
            Object object2 = order;
            order = null;
            Object G__22083 = object2;
            switch (Util.hash((Object)G__22083) >> 1 & 1) {
                case 0: {
                    if (G__22083 != const__0) break;
                    object = const__1.getRawRoot();
                    break block4;
                }
                case 1: {
                    if (G__22083 != const__2) break;
                    object = const__3.getRawRoot();
                    break block4;
                }
            }
            Object object3 = G__22083;
            G__22083 = null;
            throw (Throwable)new IllegalArgumentException((String)((IFn)const__4.getRawRoot()).invoke((Object)"No matching clause: ", object3));
        }
        Object op = object;
        Object cmp = ((IFn)const__5.getRawRoot()).invoke(sort);
        Object object4 = progress;
        progress = null;
        Object object5 = op;
        op = null;
        Object object6 = cmp;
        cmp = null;
        Object object7 = db2;
        db2 = null;
        return ((IFn)const__6.getRawRoot()).invoke((Object)new integrity$unsorted_datoms$fn__22084(object4, object5, object6), ((IFn)const__7.getRawRoot()).invoke(object7, sort));
    }

    public Object invoke(Object object, Object object2, Object object3, Object object4) {
        Object object5 = object;
        object = null;
        Object object6 = object2;
        object2 = null;
        Object object7 = object3;
        object3 = null;
        Object object8 = object4;
        object4 = null;
        return integrity$unsorted_datoms.invokeStatic(object5, object6, object7, object8);
    }
}

