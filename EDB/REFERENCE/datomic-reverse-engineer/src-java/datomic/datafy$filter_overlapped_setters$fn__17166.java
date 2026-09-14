/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.RT
 *  clojure.lang.Util
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.RT;
import clojure.lang.Util;
import clojure.lang.Var;
import java.lang.reflect.Method;

public final class datafy$filter_overlapped_setters$fn__17166
extends AFunction {
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"first");
    public static final Object const__9 = RT.classForName((String)"java.lang.String");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"second");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"str");

    public Object invoke(Object result2, Object p__17165) {
        Object object;
        Object object2 = p__17165;
        p__17165 = null;
        Object vec__17167 = object2;
        RT.nth((Object)vec__17167, (int)RT.intCast((long)0L), null);
        Object object3 = vec__17167;
        vec__17167 = null;
        Object setters2 = RT.nth((Object)object3, (int)RT.intCast((long)1L), null);
        int G__17170 = RT.count((Object)setters2);
        switch (G__17170) {
            case 1: {
                Object object4 = result2;
                result2 = null;
                Object object5 = setters2;
                setters2 = null;
                datafy$filter_overlapped_setters$fn__17166 this_ = null;
                object = ((IFn)const__5.getRawRoot()).invoke(object4, ((IFn)const__6.getRawRoot()).invoke(object5));
                break;
            }
            case 2: {
                boolean first_string_QMARK_ = Util.equiv((Object)const__9, (Object)((IFn)const__6.getRawRoot()).invoke(((Method)((IFn)const__6.getRawRoot()).invoke(setters2)).getParameterTypes()));
                Object object6 = result2;
                result2 = null;
                Object object7 = setters2;
                setters2 = null;
                datafy$filter_overlapped_setters$fn__17166 this_ = null;
                object = ((IFn)const__5.getRawRoot()).invoke(object6, ((IFn)(first_string_QMARK_ ? const__10.getRawRoot() : const__6.getRawRoot())).invoke(object7));
                break;
            }
            default: {
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__11.getRawRoot()).invoke((Object)"No matching clause: ", (Object)G__17170));
            }
        }
        return object;
    }
}

