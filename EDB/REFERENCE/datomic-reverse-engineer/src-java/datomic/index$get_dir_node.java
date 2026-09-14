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
import datomic.index.RootNode;

public final class index$get_dir_node
extends AFunction {
    public static final Var const__2 = RT.var((String)"datomic.index", (String)"use-array-cache?");
    public static final Var const__3 = RT.var((String)"datomic.measure.io-stats", (String)"inc!");
    public static final Keyword const__4 = RT.keyword(null, (String)"dir");
    public static final Var const__5 = RT.var((String)"datomic.measure.io-trace", (String)"note!");
    public static final Var const__6 = RT.var((String)"datomic.common", (String)"getx");

    public static Object invokeStatic(Object root, Object ridx, Object lookup, Object cache_QMARK_) {
        Object object;
        Object or__5238__auto__15030;
        Object k = RT.aget((Object[])((Object[])((RootNode)root).dirids), (int)RT.uncheckedIntCast((Object)ridx));
        Object ac = ((IFn)const__2.getRawRoot()).invoke();
        ((IFn)const__3.getRawRoot()).invoke((Object)const__4);
        Object object2 = k;
        k = null;
        ((IFn)const__5.getRawRoot()).invoke(object2, (Object)const__4);
        Object object3 = ac;
        Object object4 = or__5238__auto__15030 = object3 != null && object3 != Boolean.FALSE ? RT.aget((Object[])((Object[])((RootNode)root).dirs), (int)RT.uncheckedIntCast((Object)ridx)) : null;
        if (object4 != null && object4 != Boolean.FALSE) {
            object = or__5238__auto__15030;
            or__5238__auto__15030 = null;
        } else {
            Object and__5236__auto__15029;
            Object object5 = lookup;
            lookup = null;
            Object dir = ((IFn)const__6.getRawRoot()).invoke(object5, RT.aget((Object[])((Object[])((RootNode)root).dirids), (int)RT.uncheckedIntCast((Object)ridx)));
            Object object6 = ac;
            ac = null;
            Object object7 = and__5236__auto__15029 = object6;
            if (object7 != null && object7 != Boolean.FALSE) {
                Object and__5236__auto__15028;
                Object object8 = cache_QMARK_;
                cache_QMARK_ = null;
                Object object9 = and__5236__auto__15028 = object8;
                if (object9 != null) {
                    if (object9 != Boolean.FALSE) {
                        Object object10 = root;
                        root = null;
                        Object object11 = ridx;
                        ridx = null;
                        RT.aset((Object[])((Object[])((RootNode)object10).dirs), (int)RT.uncheckedIntCast((Object)object11), (Object)dir);
                    }
                }
            }
            object = dir;
            dir = null;
        }
        return object;
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
        return index$get_dir_node.invokeStatic(object5, object6, object7, object8);
    }
}

