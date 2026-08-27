/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import clojure.lang.Util;
import clojure.lang.Var;
import java.util.List;

public final class qtune$cbinds
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"concat");
    public static final Var const__1 = RT.var((String)"datomic.qtune", (String)"cvars");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"symbol?");
    public static final AFn const__8 = (AFn)Symbol.intern(null, (String)"...");
    public static final Keyword const__9 = RT.keyword(null, (String)"else");

    public static Object invokeStatic(Object c) {
        Object object;
        IFn iFn = (IFn)const__0.getRawRoot();
        Object object2 = ((IFn)const__1.getRawRoot()).invoke(c);
        if (((IFn)const__4.getRawRoot()).invoke(c) instanceof List) {
            Object object3 = c;
            c = null;
            Object binds = ((IFn)const__5.getRawRoot()).invoke(object3);
            Object object4 = ((IFn)const__6.getRawRoot()).invoke(binds);
            if (object4 != null && object4 != Boolean.FALSE) {
                Object object5 = binds;
                binds = null;
                object = Tuple.create((Object)object5);
            } else if (binds instanceof List) {
                if (((IFn)const__4.getRawRoot()).invoke(binds) instanceof List) {
                    Object object6 = binds;
                    binds = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object6);
                } else if (Util.equiv((Object)const__8, (Object)((IFn)const__5.getRawRoot()).invoke(binds))) {
                    Object object7 = binds;
                    binds = null;
                    object = Tuple.create((Object)((IFn)const__4.getRawRoot()).invoke(object7));
                } else {
                    Keyword keyword = const__9;
                    if (keyword != null && keyword != Boolean.FALSE) {
                        object = binds;
                        binds = null;
                    } else {
                        object = null;
                    }
                }
            } else {
                object = null;
            }
        } else {
            object = null;
        }
        return iFn.invoke(object2, object);
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return qtune$cbinds.invokeStatic(object2);
    }
}

