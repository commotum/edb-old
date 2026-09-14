/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFn
 *  clojure.lang.AFunction
 *  clojure.lang.IFn
 *  clojure.lang.Indexed
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFn;
import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Indexed;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Var;
import datomic.integrity$crosscheck_indexes$fn__22207$fn__22212$fn__22213;
import datomic.integrity$crosscheck_indexes$fn__22207$fn__22212$fn__22215;

public final class integrity$crosscheck_indexes$fn__22207$fn__22212
extends AFunction {
    Object o;
    public static final Var const__0 = RT.var((String)"clojure.core", (String)"seq");
    public static final AFn const__9 = (AFn)RT.vector((Object[])new Object[]{Symbol.intern(null, (String)"crosscheck-eavt-aevt"), Symbol.intern(null, (String)"crosscheck-aevt-eavt"), Symbol.intern(null, (String)"crosscheck-avet-eavt"), Symbol.intern(null, (String)"crosscheck-avet-aevt"), Symbol.intern(null, (String)"crosscheck-raet-aevt"), Symbol.intern(null, (String)"crosscheck-raet-eavt"), Symbol.intern(null, (String)"crosscheck-aevt-avet"), Symbol.intern(null, (String)"crosscheck-eavt-avet")});
    public static final Var const__12 = RT.var((String)"clojure.core", (String)"print");
    public static final Var const__13 = RT.var((String)"clojure.core", (String)"flush");
    public static final Var const__14 = RT.var((String)"clojure.core", (String)"println");
    public static final Var const__15 = RT.var((String)"clojure.core", (String)"ns-resolve");
    public static final AFn const__16 = (AFn)Symbol.intern(null, (String)"datomic.integrity");
    public static final Var const__17 = RT.var((String)"datomic.integrity", (String)"get-db");
    public static final Var const__19 = RT.var((String)"clojure.core", (String)"chunked-seq?");
    public static final Var const__20 = RT.var((String)"clojure.core", (String)"chunk-first");
    public static final Var const__21 = RT.var((String)"clojure.core", (String)"chunk-rest");
    public static final Var const__24 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__25 = RT.var((String)"clojure.core", (String)"next");

    public integrity$crosscheck_indexes$fn__22207$fn__22212(Object object) {
        this.o = object;
    }

    public Object invoke() {
        Object seq_22208 = ((IFn)const__0.getRawRoot()).invoke((Object)const__9);
        Object chunk_22209 = null;
        long count_22210 = 0L;
        long i_22211 = 0L;
        while (true) {
            Object temp__5457__auto__22219;
            if (i_22211 < count_22210) {
                Object c = ((Indexed)chunk_22209).nth(RT.intCast((long)i_22211));
                ((IFn)const__12.getRawRoot()).invoke((Object)"\n", c);
                ((IFn)const__13.getRawRoot()).invoke();
                Object object = c;
                c = null;
                ((IFn)const__14.getRawRoot()).invoke(((IFn)((IFn)const__15.getRawRoot()).invoke((Object)const__16, object)).invoke(((IFn)const__17.getRawRoot()).invoke(this.o), ((IFn)const__17.getRawRoot()).invoke(this.o), (Object)new integrity$crosscheck_indexes$fn__22207$fn__22212$fn__22213()));
                ((IFn)const__13.getRawRoot()).invoke();
                Object object2 = seq_22208;
                seq_22208 = null;
                Object object3 = chunk_22209;
                chunk_22209 = null;
                ++i_22211;
                chunk_22209 = object3;
                seq_22208 = object2;
                continue;
            }
            Object object = seq_22208;
            seq_22208 = null;
            Object object4 = temp__5457__auto__22219 = ((IFn)const__0.getRawRoot()).invoke(object);
            if (object4 == null || object4 == Boolean.FALSE) break;
            Object object5 = temp__5457__auto__22219;
            temp__5457__auto__22219 = null;
            Object seq_222082 = object5;
            Object object6 = ((IFn)const__19.getRawRoot()).invoke(seq_222082);
            if (object6 != null && object6 != Boolean.FALSE) {
                Object c__5719__auto__22218 = ((IFn)const__20.getRawRoot()).invoke(seq_222082);
                Object object7 = seq_222082;
                seq_222082 = null;
                Object object8 = c__5719__auto__22218;
                Object object9 = c__5719__auto__22218;
                c__5719__auto__22218 = null;
                i_22211 = RT.intCast((long)0L);
                count_22210 = RT.intCast((int)RT.count((Object)object9));
                chunk_22209 = object8;
                seq_22208 = ((IFn)const__21.getRawRoot()).invoke(object7);
                continue;
            }
            Object c = ((IFn)const__24.getRawRoot()).invoke(seq_222082);
            ((IFn)const__12.getRawRoot()).invoke((Object)"\n", c);
            ((IFn)const__13.getRawRoot()).invoke();
            Object object10 = c;
            c = null;
            ((IFn)const__14.getRawRoot()).invoke(((IFn)((IFn)const__15.getRawRoot()).invoke((Object)const__16, object10)).invoke(((IFn)const__17.getRawRoot()).invoke(this.o), ((IFn)const__17.getRawRoot()).invoke(this.o), (Object)new integrity$crosscheck_indexes$fn__22207$fn__22212$fn__22215()));
            ((IFn)const__13.getRawRoot()).invoke();
            Object object11 = seq_222082;
            seq_222082 = null;
            i_22211 = 0L;
            count_22210 = 0L;
            chunk_22209 = null;
            seq_22208 = ((IFn)const__25.getRawRoot()).invoke(object11);
        }
        return null;
    }
}

