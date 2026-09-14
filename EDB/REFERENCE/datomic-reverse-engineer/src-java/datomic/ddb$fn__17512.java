/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IPersistentVector
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Tuple
 *  clojure.lang.Var
 *  com.amazonaws.ResponseMetadata
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.IPersistentVector;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Tuple;
import clojure.lang.Var;
import com.amazonaws.ResponseMetadata;

public final class ddb$fn__17512
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"hash-map");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"concat");
    public static final Keyword const__3 = RT.keyword(null, (String)"requestId");
    public static final Var const__4 = RT.var((String)"datomic.datafy", (String)"object-to-data-wrapper");

    public static Object invokeStatic(Object o) {
        IPersistentVector iPersistentVector;
        String temp__5457__auto__17515;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object = const__1.getRawRoot();
        IFn iFn2 = (IFn)const__2.getRawRoot();
        Object object2 = o;
        o = null;
        String string = temp__5457__auto__17515 = ((ResponseMetadata)object2).getRequestId();
        if (string != null && string != Boolean.FALSE) {
            String v__17285__auto__17514;
            String string2 = temp__5457__auto__17515;
            temp__5457__auto__17515 = null;
            String string3 = v__17285__auto__17514 = string2;
            v__17285__auto__17514 = null;
            iPersistentVector = Tuple.create((Object)const__3, (Object)((IFn)const__4.getRawRoot()).invoke((Object)string3));
        } else {
            iPersistentVector = null;
        }
        return iFn.invoke(object, iFn2.invoke(iPersistentVector));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return ddb$fn__17512.invokeStatic(object2);
    }
}

