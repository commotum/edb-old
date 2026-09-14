/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Keyword
 *  clojure.lang.PersistentArrayMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.pull$normalize_pattern$direction__18968;
import datomic.pull$normalize_pattern$fn__18970;

public final class pull$normalize_pattern
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"map?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"string?");
    public static final Var const__2 = RT.var((String)"datomic.pull", (String)"normalize-pattern");
    public static final Var const__3 = RT.var((String)"clojure.edn", (String)"read-string");
    public static final Keyword const__4 = RT.keyword(null, (String)"else");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"reduce");

    public static Object invokeStatic(Object pull_spec) {
        Object object;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(pull_spec);
        if (object2 != null && object2 != Boolean.FALSE) {
            object = pull_spec;
            pull_spec = null;
        } else {
            Object object3 = ((IFn)const__1.getRawRoot()).invoke(pull_spec);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = pull_spec;
                pull_spec = null;
                object = ((IFn)const__2.getRawRoot()).invoke(((IFn)const__3.getRawRoot()).invoke(object4));
            } else {
                Keyword keyword = const__4;
                if (keyword != null && keyword != Boolean.FALSE) {
                    pull$normalize_pattern$direction__18968 direction;
                    pull$normalize_pattern$direction__18968 pull$normalize_pattern$direction__18968 = direction = new pull$normalize_pattern$direction__18968();
                    direction = null;
                    Object object5 = pull_spec;
                    pull_spec = null;
                    object = ((IFn)const__5.getRawRoot()).invoke((Object)new pull$normalize_pattern$fn__18970((Object)pull$normalize_pattern$direction__18968), (Object)PersistentArrayMap.EMPTY, object5);
                } else {
                    object = null;
                }
            }
        }
        return object;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return pull$normalize_pattern.invokeStatic(object2);
    }
}

