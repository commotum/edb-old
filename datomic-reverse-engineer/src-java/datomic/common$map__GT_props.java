/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Properties;

public final class common$map__GT_props
extends AFunction {
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"keys");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object m) {
        Properties props = new Properties();
        Object seq_9221 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(m));
        Object chunk_9222 = null;
        long count_9223 = 0L;
        long i_9224 = 0L;
        while (true) {
            Object temp__5457__auto__9227;
            if (i_9224 < count_9223) {
                Object k = ((Indexed)chunk_9222).nth(RT.uncheckedIntCast((long)i_9224));
                String string = (String)((IFn)const__4.getRawRoot()).invoke(k);
                Object object = k;
                k = null;
                props.setProperty(string, (String)((IFn)const__4.getRawRoot()).invoke(RT.get((Object)m, (Object)object)));
                Object object2 = seq_9221;
                seq_9221 = null;
                Object object3 = chunk_9222;
                chunk_9222 = null;
                ++i_9224;
                chunk_9222 = object3;
                seq_9221 = object2;
                continue;
            }
            Object object = seq_9221;
            seq_9221 = null;
            Object object4 = temp__5457__auto__9227 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object4 == null || object4 == Boolean.FALSE) break;
            Object object5 = temp__5457__auto__9227;
            temp__5457__auto__9227 = null;
            Object seq_92212 = object5;
            Object object6 = ((IFn)const__7.getRawRoot()).invoke(seq_92212);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object c__5719__auto__9226 = ((IFn)const__8.getRawRoot()).invoke(seq_92212);
                Object object7 = seq_92212;
                seq_92212 = null;
                Object object8 = c__5719__auto__9226;
                Object object9 = c__5719__auto__9226;
                c__5719__auto__9226 = null;
                i_9224 = (int)0L;
                count_9223 = RT.count((Object)object9);
                chunk_9222 = object8;
                seq_9221 = ((IFn)const__9.getRawRoot()).invoke(object7);
                continue;
            }
            Object k = ((IFn)const__12.getRawRoot()).invoke(seq_92212);
            String string = (String)((IFn)const__4.getRawRoot()).invoke(k);
            Object object10 = k;
            k = null;
            props.setProperty(string, (String)((IFn)const__4.getRawRoot()).invoke(RT.get((Object)m, (Object)object10)));
            Object object11 = seq_92212;
            seq_92212 = null;
            i_9224 = 0L;
            count_9223 = 0L;
            chunk_9222 = null;
            seq_9221 = ((IFn)const__13.getRawRoot()).invoke(object11);
        }
        Object var1_1 = null;
        return props;
    }

    public Object invoke(Object object) {
        Object object2 = object;
        object = null;
        return common$map__GT_props.invokeStatic(object2);
    }
}

