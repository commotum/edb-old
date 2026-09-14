/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;

public final class index$vecdiff
extends AFunction {
    public static final Object const__0 = 0L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"subvec");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"not");
    public static final Var const__8 = RT.var((String)"datomic.common", (String)"compare");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"conj");
    public static final Var const__11 = RT.var((String)"datomic.index", (String)"mindiff");
    public static final Keyword const__12 = RT.keyword(null, (String)"default");

    /*
     * WARNING - void declaration
     */
    public static Object invokeStatic(Object minv, Object maxv) {
        Object object;
        block6: {
            void var1_1;
            for (long i = 0L; i < (long)RT.count((Object)maxv); ++i) {
                if (i == (long)RT.count((Object)minv)) {
                    object = ((IFn)const__4.getRawRoot()).invoke(maxv, const__0, (Object)Numbers.num((long)Numbers.unchecked_inc((long)i)));
                } else {
                    Object object2 = ((IFn)const__6.getRawRoot()).invoke((Object)(Numbers.isZero((long)((IFn.OOL)const__8.getRawRoot()).invokePrim(RT.get((Object)minv, (Object)Numbers.num((long)i)), RT.get((Object)maxv, (Object)Numbers.num((long)i)))) ? Boolean.TRUE : Boolean.FALSE));
                    if (object2 != null && object2 != Boolean.FALSE) {
                        object = ((IFn)const__10.getRawRoot()).invoke(((IFn)const__4.getRawRoot()).invoke(maxv, const__0, (Object)Numbers.num((long)i)), ((IFn)const__11.getRawRoot()).invoke(RT.get((Object)minv, (Object)Numbers.num((long)i)), RT.get((Object)maxv, (Object)Numbers.num((long)i))));
                    } else {
                        Keyword keyword = const__12;
                        if (keyword != null && keyword != Boolean.FALSE) {
                            continue;
                        }
                        object = null;
                    }
                }
                break block6;
            }
            object = var1_1;
        }
        return object;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return index$vecdiff.invokeStatic(object3, object4);
    }
}

