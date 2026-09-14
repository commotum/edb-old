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
import datomic.integrity$validate_log_cli$progress__22381;

public final class integrity$validate_log_cli
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.integrity", (String)"enhance-uri");
    public static final Var const__1 = RT.var((String)"datomic.tools", (String)"connection-resources");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"atom");
    public static final Object const__3 = 0L;
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"flush");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__7 = RT.var((String)"datomic.integrity", (String)"validate-t-order");
    public static final Keyword const__8 = RT.keyword(null, (String)"find-log");
    public static final Object const__9 = 100L;
    public static final Keyword const__10 = RT.keyword(null, (String)"create-log-val");
    public static final Var const__11 = RT.var((String)"datomic.integrity", (String)"crosscheck-dir-segs");

    public static Object invokeStatic(Object uri2) {
        Object count2;
        Object object = uri2;
        uri2 = null;
        Object uri3 = ((IFn)const__0.getRawRoot()).invoke(object);
        Object cr = ((IFn)const__1.getRawRoot()).invoke(uri3);
        Object object2 = count2 = ((IFn)const__2.getRawRoot()).invoke(const__3);
        count2 = null;
        integrity$validate_log_cli$progress__22381 progress = new integrity$validate_log_cli$progress__22381(object2);
        ((IFn)const__4.getRawRoot()).invoke((Object)"\nValidating t order (find log)");
        ((IFn)const__5.getRawRoot()).invoke();
        ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(uri3, (Object)const__8, ((IFn)progress).invoke(const__9)));
        ((IFn)const__4.getRawRoot()).invoke((Object)"\nValidating t order (create-log-val)");
        ((IFn)const__5.getRawRoot()).invoke();
        Object object3 = uri3;
        uri3 = null;
        ((IFn)const__6.getRawRoot()).invoke(((IFn)const__7.getRawRoot()).invoke(object3, (Object)const__10, ((IFn)progress).invoke(const__9)));
        ((IFn)const__4.getRawRoot()).invoke((Object)"\nCrosschecking dir segs");
        ((IFn)const__5.getRawRoot()).invoke();
        Object object4 = cr;
        cr = null;
        integrity$validate_log_cli$progress__22381 integrity$validate_log_cli$progress__22381 = progress;
        progress = null;
        return ((IFn)const__11.getRawRoot()).invoke(object4, ((IFn)integrity$validate_log_cli$progress__22381).invoke(const__9));
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return integrity$validate_log_cli.invokeStatic(object2);
    }
}

