/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.PersistentHashMap
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.PersistentHashMap;
import clojure.lang.RT;
import clojure.lang.Var;
import datomic.query$pull_fv$fn__19501;

public final class query$pull_fv
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq?");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"seq");
    public static final Keyword const__3 = RT.keyword(null, (String)"find");
    public static final Keyword const__4 = RT.keyword(null, (String)"pull");
    public static final Keyword const__5 = RT.keyword(null, (String)"with");
    public static final Keyword const__6 = RT.keyword(null, (String)"in");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"zipmap");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"range");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"reduce");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"vec");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"repeat");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"identity");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"filter");
    public static final Var const__16 = RT.var((String)"clojure.core", (String)"map?");

    public static Object invokeStatic(Object p__19498, Object srcs) {
        Object object;
        Object object2 = p__19498;
        p__19498 = null;
        Object map__19499 = object2;
        Object object3 = ((IFn)const__0.getRawRoot()).invoke(map__19499);
        if (object3 != null && object3 != Boolean.FALSE) {
            Object object4 = map__19499;
            map__19499 = null;
            object = PersistentHashMap.create((ISeq)((ISeq)((IFn)const__1.getRawRoot()).invoke(object4)));
        } else {
            object = map__19499;
            map__19499 = null;
        }
        Object map__194992 = object;
        Object find = RT.get((Object)map__194992, (Object)const__3);
        Object pull2 = RT.get((Object)map__194992, (Object)const__4);
        Object with2 = RT.get((Object)map__194992, (Object)const__5);
        Object object5 = map__194992;
        map__194992 = null;
        Object in = RT.get((Object)object5, (Object)const__6);
        Object smap = ((IFn)const__7.getRawRoot()).invoke(find, ((IFn)const__8.getRawRoot()).invoke());
        Object object6 = in;
        in = null;
        Object srcmap = ((IFn)const__7.getRawRoot()).invoke(object6, srcs);
        Object object7 = find;
        find = null;
        Object object8 = with2;
        with2 = null;
        long colct = Numbers.minus((long)RT.count((Object)object7), (long)RT.count((Object)object8));
        Object object9 = srcs;
        srcs = null;
        Object object10 = smap;
        smap = null;
        Object object11 = srcmap;
        srcmap = null;
        Object object12 = pull2;
        pull2 = null;
        return ((IFn)const__11.getRawRoot()).invoke((Object)new query$pull_fv$fn__19501(object9, object10, object11), ((IFn)const__12.getRawRoot()).invoke(((IFn)const__13.getRawRoot()).invoke((Object)Numbers.num((long)colct), const__14.getRawRoot())), ((IFn)const__15.getRawRoot()).invoke(const__16.getRawRoot(), object12));
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return query$pull_fv.invokeStatic(object3, object4);
    }
}

