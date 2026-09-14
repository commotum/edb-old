/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class common$qualified_name
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"str");

    public static Object invokeStatic(Object k) {
        Object object;
        String temp__5455__auto__9209;
        String string = temp__5455__auto__9209 = ((Keyword)k).getNamespace();
        if (string != null && string != Boolean.FALSE) {
            String ns;
            String string2 = temp__5455__auto__9209;
            temp__5455__auto__9209 = null;
            String string3 = ns = string2;
            ns = null;
            Object object2 = k;
            k = null;
            object = ((IFn)const__0.getRawRoot()).invoke((Object)string3, (Object)"/", (Object)((Keyword)object2).getName());
        } else {
            Object object3 = k;
            k = null;
            object = ((Keyword)object3).getName();
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$qualified_name.invokeStatic(object2);
    }
}

