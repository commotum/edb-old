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

public final class common$into_bean
extends AFunction {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"bean-setters");
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
        Object seq_9192 = ((IFn)const__2.getRawRoot()).invoke(object);
        Object chunk_9193 = null;
        long count_9194 = 0L;
        long i_9195 = 0L;
        while (true) {
            Object setter;
            Object temp__5455__auto__9205;
            Object temp__5457__auto__9206;
            if (i_9195 < count_9194) {
                Object setter2;
                Object temp__5455__auto__9203;
                Object vec__9196 = ((Indexed)chunk_9193).nth(RT.uncheckedIntCast((long)i_9195));
                Object k = RT.nth((Object)vec__9196, (int)RT.uncheckedIntCast((long)0L), null);
                Object object2 = vec__9196;
                vec__9196 = null;
                Object v = RT.nth((Object)object2, (int)RT.uncheckedIntCast((long)1L), null);
                Object object3 = temp__5455__auto__9203 = ((IFn)setters2).invoke(k);
                if (object3 == null || object3 == Boolean.FALSE) {
                    Object object4 = k;
                    k = null;
                    throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"No property named ", object4));
                }
                Object object5 = temp__5455__auto__9203;
                temp__5455__auto__9203 = null;
                Object object6 = setter2 = object5;
                setter2 = null;
                Object object7 = v;
                v = null;
                ((IFn)object6).invoke(bean, object7);
                Object object8 = seq_9192;
                seq_9192 = null;
                Object object9 = chunk_9193;
                chunk_9193 = null;
                ++i_9195;
                chunk_9193 = object9;
                seq_9192 = object8;
                continue;
            }
            Object object10 = seq_9192;
            seq_9192 = null;
            Object object11 = temp__5457__auto__9206 = ((IFn)const__2.getRawRoot()).invoke(object10);
            if (object11 == null || object11 == Boolean.FALSE) break;
            Object object12 = temp__5457__auto__9206;
            temp__5457__auto__9206 = null;
            Object seq_91922 = object12;
            Object object13 = ((IFn)const__9.getRawRoot()).invoke(seq_91922);
            if (object13 != null && object13 != Boolean.FALSE) {
                Object c__5719__auto__9204 = ((IFn)const__10.getRawRoot()).invoke(seq_91922);
                Object object14 = seq_91922;
                seq_91922 = null;
                Object object15 = c__5719__auto__9204;
                Object object16 = c__5719__auto__9204;
                c__5719__auto__9204 = null;
                i_9195 = (int)0L;
                count_9194 = RT.count((Object)object16);
                chunk_9193 = object15;
                seq_9192 = ((IFn)const__11.getRawRoot()).invoke(object14);
                continue;
            }
            Object vec__9199 = ((IFn)const__14.getRawRoot()).invoke(seq_91922);
            Object k = RT.nth((Object)vec__9199, (int)RT.uncheckedIntCast((long)0L), null);
            Object object17 = vec__9199;
            vec__9199 = null;
            Object v = RT.nth((Object)object17, (int)RT.uncheckedIntCast((long)1L), null);
            Object object18 = temp__5455__auto__9205 = ((IFn)setters2).invoke(k);
            if (object18 == null || object18 == Boolean.FALSE) {
                Object object19 = k;
                k = null;
                throw (Throwable)new IllegalArgumentException((String)((IFn)const__7.getRawRoot()).invoke((Object)"No property named ", object19));
            }
            Object object20 = temp__5455__auto__9205;
            temp__5455__auto__9205 = null;
            Object object21 = setter = object20;
            setter = null;
            Object object22 = v;
            v = null;
            ((IFn)object21).invoke(bean, object22);
            Object object23 = seq_91922;
            seq_91922 = null;
            i_9195 = 0L;
            count_9194 = 0L;
            chunk_9193 = null;
            seq_9192 = ((IFn)const__15.getRawRoot()).invoke(object23);
        }
        Object object24 = null;
        return bean;
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$into_bean.invokeStatic(object3, object4);
    }
}

