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

public final class datafy$vals_to_bean
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.datafy", (String)"val-setters");
    public static final Var const__1 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__7 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__11 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(Object bean, Object props) {
        Object setters2 = ((IFn)const__0.getRawRoot()).invoke(((IFn)const__1.getRawRoot()).invoke(bean));
        Object object = props;
        props = null;
        Object seq_17359 = ((IFn)const__2.getRawRoot()).invoke(object);
        Object chunk_17360 = null;
        long count_17361 = 0L;
        long i_17362 = 0L;
        while (true) {
            Object setter;
            Object temp__5455__auto__17372;
            Object temp__5457__auto__17373;
            if (i_17362 < count_17361) {
                Object setter2;
                Object temp__5455__auto__17370;
                Object vec__17363 = ((Indexed)chunk_17360).nth(RT.intCast((long)i_17362));
                Object k = RT.nth((Object)vec__17363, (int)RT.intCast((long)0L), null);
                Object object2 = vec__17363;
                vec__17363 = null;
                Object v = RT.nth((Object)object2, (int)RT.intCast((long)1L), null);
                Object object3 = temp__5455__auto__17370 = ((IFn)setters2).invoke(k);
                if (object3 == null || object3 == Boolean.FALSE) {
                    Object object4 = k;
                    k = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"No property named ", object4));
                }
                Object object5 = temp__5455__auto__17370;
                temp__5455__auto__17370 = null;
                Object object6 = setter2 = object5;
                setter2 = null;
                Object object7 = v;
                v = null;
                ((IFn)object6).invoke(bean, object7);
                Object object8 = seq_17359;
                seq_17359 = null;
                Object object9 = chunk_17360;
                chunk_17360 = null;
                ++i_17362;
                chunk_17360 = object9;
                seq_17359 = object8;
                continue;
            }
            Object object10 = seq_17359;
            seq_17359 = null;
            Object object11 = temp__5457__auto__17373 = ((IFn)const__2.getRawRoot()).invoke(object10);
            if (object11 == null || object11 == Boolean.FALSE) break;
            Object object12 = temp__5457__auto__17373;
            temp__5457__auto__17373 = null;
            Object seq_173592 = object12;
            Object object13 = ((IFn)const__9.getRawRoot()).invoke(seq_173592);
            if (object13 != null && object13 != Boolean.FALSE) {
                Object c__5719__auto__17371 = ((IFn)const__10.getRawRoot()).invoke(seq_173592);
                Object object14 = seq_173592;
                seq_173592 = null;
                Object object15 = c__5719__auto__17371;
                Object object16 = c__5719__auto__17371;
                c__5719__auto__17371 = null;
                i_17362 = RT.intCast((long)0L);
                count_17361 = RT.intCast((int)RT.count((Object)object16));
                chunk_17360 = object15;
                seq_17359 = ((IFn)const__11.getRawRoot()).invoke(object14);
                continue;
            }
            Object vec__17366 = ((IFn)const__14.getRawRoot()).invoke(seq_173592);
            Object k = RT.nth((Object)vec__17366, (int)RT.intCast((long)0L), null);
            Object object17 = vec__17366;
            vec__17366 = null;
            Object v = RT.nth((Object)object17, (int)RT.intCast((long)1L), null);
            Object object18 = temp__5455__auto__17372 = ((IFn)setters2).invoke(k);
            if (object18 == null || object18 == Boolean.FALSE) {
                Object object19 = k;
                k = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"No property named ", object19));
            }
            Object object20 = temp__5455__auto__17372;
            temp__5455__auto__17372 = null;
            Object object21 = setter = object20;
            setter = null;
            Object object22 = v;
            v = null;
            ((IFn)object21).invoke(bean, object22);
            Object object23 = seq_173592;
            seq_173592 = null;
            i_17362 = 0L;
            count_17361 = 0L;
            chunk_17360 = null;
            seq_17359 = ((IFn)const__15.getRawRoot()).invoke(object23);
        }
        Object object24 = null;
        return bean;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return datafy$vals_to_bean.invokeStatic(object3, object4);
    }
}

