/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.Keyword
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.Keyword;
import clojure.lang.RT;
import clojure.lang.Var;

public final class db$create_attr_pred$fn__13099
extends AFunction {
    Object fn_map;
    Object kw;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"true?");
    public static final Var const__6 = RT.var((String)"datomic.error", (String)"arg");
    public static final Keyword const__7 = RT.keyword((String)"db.error", (String)"attr-pred");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"str");
    public static final Var const__10 = RT.var((String)"clojure.set", (String)"map-invert");
    public static final Keyword const__11 = RT.keyword((String)"db.error", (String)"pred-return");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__18 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"next");

    public db$create_attr_pred$fn__13099(Object object, Object object2) {
        this.fn_map = object;
        this.kw = object2;
    }

    public Object invoke(Object e, Object v, Object idmap) {
        Object seq_13100 = ((IFn)const__0.getRawRoot()).invoke(this.fn_map);
        Object chunk_13101 = null;
        long count_13102 = 0L;
        long i_13103 = 0L;
        while (true) {
            Object pred2;
            Object temp__5457__auto__13112;
            if (i_13103 < count_13102) {
                Object pred3;
                Object vec__13104 = ((Indexed)chunk_13101).nth(RT.uncheckedIntCast((long)i_13103));
                Object name = RT.nth((Object)vec__13104, (int)RT.uncheckedIntCast((long)0L), null);
                Object object = vec__13104;
                vec__13104 = null;
                Object object2 = pred3 = RT.nth((Object)object, (int)RT.uncheckedIntCast((long)1L), null);
                pred3 = null;
                Object result2 = ((IFn)object2).invoke(v);
                Object object3 = ((IFn)const__5.getRawRoot()).invoke(result2);
                if (object3 == null || object3 == Boolean.FALSE) {
                    Object object4 = name;
                    name = null;
                    Object[] objectArray = new Object[2];
                    objectArray[0] = const__11;
                    Object object5 = result2;
                    result2 = null;
                    objectArray[1] = object5;
                    throw (Throwable)((IFn)const__6.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)"Entity ", RT.get((Object)((IFn)const__10.getRawRoot()).invoke(idmap), (Object)e, (Object)e), (Object)" attribute ", this.kw, (Object)" value ", v, (Object)" failed pred ", object4), (Object)RT.mapUniqueKeys((Object[])objectArray));
                }
                Object object6 = seq_13100;
                seq_13100 = null;
                Object object7 = chunk_13101;
                chunk_13101 = null;
                ++i_13103;
                chunk_13101 = object7;
                seq_13100 = object6;
                continue;
            }
            Object object = seq_13100;
            seq_13100 = null;
            Object object8 = temp__5457__auto__13112 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object8 == null || object8 == Boolean.FALSE) break;
            Object object9 = temp__5457__auto__13112;
            temp__5457__auto__13112 = null;
            Object seq_131002 = object9;
            Object object10 = ((IFn)const__13.getRawRoot()).invoke(seq_131002);
            if (object10 != null && object10 != Boolean.FALSE) {
                Object c__5719__auto__13111 = ((IFn)const__14.getRawRoot()).invoke(seq_131002);
                Object object11 = seq_131002;
                seq_131002 = null;
                Object object12 = c__5719__auto__13111;
                Object object13 = c__5719__auto__13111;
                c__5719__auto__13111 = null;
                i_13103 = (int)0L;
                count_13102 = RT.count((Object)object13);
                chunk_13101 = object12;
                seq_13100 = ((IFn)const__15.getRawRoot()).invoke(object11);
                continue;
            }
            Object vec__13107 = ((IFn)const__18.getRawRoot()).invoke(seq_131002);
            Object name = RT.nth((Object)vec__13107, (int)RT.uncheckedIntCast((long)0L), null);
            Object object14 = vec__13107;
            vec__13107 = null;
            Object object15 = pred2 = RT.nth((Object)object14, (int)RT.uncheckedIntCast((long)1L), null);
            pred2 = null;
            Object result3 = ((IFn)object15).invoke(v);
            Object object16 = ((IFn)const__5.getRawRoot()).invoke(result3);
            if (object16 == null || object16 == Boolean.FALSE) {
                Object object17 = name;
                name = null;
                Object[] objectArray = new Object[2];
                objectArray[0] = const__11;
                Object object18 = result3;
                result3 = null;
                objectArray[1] = object18;
                throw (Throwable)((IFn)const__6.getRawRoot()).invoke((Object)const__7, ((IFn)const__8.getRawRoot()).invoke((Object)"Entity ", RT.get((Object)((IFn)const__10.getRawRoot()).invoke(idmap), (Object)e, (Object)e), (Object)" attribute ", this.kw, (Object)" value ", v, (Object)" failed pred ", object17), (Object)RT.mapUniqueKeys((Object[])objectArray));
            }
            Object object19 = seq_131002;
            seq_131002 = null;
            i_13103 = 0L;
            count_13102 = 0L;
            chunk_13101 = null;
            seq_13100 = ((IFn)const__19.getRawRoot()).invoke(object19);
        }
        return null;
    }
}

