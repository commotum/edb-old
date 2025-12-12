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
package datomic.core2.aws.s3;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.PersistentArrayMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.core2.aws.s3.aws_api$get_bytes$fn__20487;

public final class aws_api$get_bytes
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"next");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"to-array");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"first");
    public static final Keyword const__6 = RT.keyword(null, (String)"bucket");
    public static final Keyword const__7 = RT.keyword(null, (String)"client");
    public static final Keyword const__8 = RT.keyword(null, (String)"key");
    public static final Var const__9 = RT.var((String)"clojure.core.async", (String)"chan");
    public static final Object const__10 = 1L;
    public static final Var const__11 = RT.var((String)"clojure.core.async.impl.dispatch", (String)"run");

    public static Object invokeStatic(Object p__20460) {
        Object object;
        Object map__20461 = p__20460;
        Object object2 = ((IFn)const__0.getRawRoot()).invoke(map__20461);
        if (object2 != null && object2 != Boolean.FALSE) {
            Object object3 = ((IFn)const__1.getRawRoot()).invoke(map__20461);
            if (object3 != null && object3 != Boolean.FALSE) {
                Object object4 = map__20461;
                map__20461 = null;
                object = PersistentArrayMap.createAsIfByAssoc((Object[])((Object[])((IFn)const__2.getRawRoot()).invoke(object4)));
            } else {
                Object object5 = ((IFn)const__3.getRawRoot()).invoke(map__20461);
                if (object5 != null && object5 != Boolean.FALSE) {
                    Object object6 = map__20461;
                    map__20461 = null;
                    object = ((IFn)const__4.getRawRoot()).invoke(object6);
                } else {
                    object = PersistentArrayMap.EMPTY;
                }
            }
        } else {
            object = map__20461;
            map__20461 = null;
        }
        Object map__204612 = object;
        Object bucket = RT.get((Object)map__204612, (Object)const__6);
        Object client2 = RT.get((Object)map__204612, (Object)const__7);
        Object key = RT.get((Object)map__204612, (Object)const__8);
        Object c__10230__auto__20520 = ((IFn)const__9.getRawRoot()).invoke(const__10);
        Object captured_bindings__10231__auto__20521 = Var.getThreadBindingFrame();
        Object object7 = map__204612;
        map__204612 = null;
        Object object8 = captured_bindings__10231__auto__20521;
        captured_bindings__10231__auto__20521 = null;
        Object object9 = p__20460;
        p__20460 = null;
        Object object10 = key;
        key = null;
        Object object11 = bucket;
        bucket = null;
        Object object12 = client2;
        client2 = null;
        ((IFn)const__11.getRawRoot()).invoke((Object)new aws_api$get_bytes$fn__20487(object7, object8, object9, c__10230__auto__20520, object10, object11, object12));
        Object object13 = c__10230__auto__20520;
        c__10230__auto__20520 = null;
        return object13;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return aws_api$get_bytes.invokeStatic(object2);
    }
}

