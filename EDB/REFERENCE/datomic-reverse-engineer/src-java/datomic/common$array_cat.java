/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.ISeq
 *  clojure.lang.RT
 *  clojure.lang.RestFn
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.RT;
import clojure.lang.RestFn;
import clojure.lang.Var;
import java.lang.reflect.Array;

public final class common$array_cat
extends RestFn {
    public static final Var const__2 = RT.var((String)"clojure.core", (String)"apply");
    public static final Var const__3 = RT.var((String)"clojure.core", (String)"+");
    public static final Var const__4 = RT.var((String)"clojure.core", (String)"map");
    public static final Var const__5 = RT.var((String)"clojure.core", (String)"count");
    public static final Var const__6 = RT.var((String)"clojure.core", (String)"class");
    public static final Var const__8 = RT.var((String)"clojure.core", (String)"seq");
    public static final Var const__9 = RT.var((String)"clojure.core", (String)"first");
    public static final Var const__10 = RT.var((String)"clojure.core", (String)"next");

    public static Object invokeStatic(ISeq p__9108) {
        Object object;
        ISeq iSeq = p__9108;
        p__9108 = null;
        ISeq vec__9109 = iSeq;
        Object a = RT.nth((Object)vec__9109, (int)RT.uncheckedIntCast((long)0L), null);
        ISeq iSeq2 = vec__9109;
        vec__9109 = null;
        ISeq as = iSeq2;
        Object object2 = a;
        if (object2 != null && object2 != Boolean.FALSE) {
            ISeq G__9115;
            ISeq vec__9116;
            Class<?> type;
            Object length = ((IFn)const__2.getRawRoot()).invoke(const__3.getRawRoot(), ((IFn)const__4.getRawRoot()).invoke(const__5.getRawRoot(), (Object)as));
            Object object3 = a;
            a = null;
            Class<?> clazz = type = ((Class)((IFn)const__6.getRawRoot()).invoke(object3)).getComponentType();
            type = null;
            Object object4 = length;
            length = null;
            Object result2 = Array.newInstance(clazz, RT.uncheckedIntCast((Object)object4));
            long i = 0L;
            ISeq iSeq3 = as;
            as = null;
            ISeq iSeq4 = vec__9116 = (G__9115 = iSeq3);
            vec__9116 = null;
            Object seq__9117 = ((IFn)const__8.getRawRoot()).invoke((Object)iSeq4);
            Object first__9118 = ((IFn)const__9.getRawRoot()).invoke(seq__9117);
            Object object5 = seq__9117;
            seq__9117 = null;
            Object seq__91172 = ((IFn)const__10.getRawRoot()).invoke(object5);
            first__9118 = null;
            seq__91172 = null;
            long i2 = i;
            ISeq iSeq5 = G__9115;
            G__9115 = null;
            Object G__91152 = iSeq5;
            while (true) {
                ISeq vec__9119;
                long i3 = i2;
                ISeq iSeq6 = G__91152;
                G__91152 = null;
                ISeq iSeq7 = vec__9119 = iSeq6;
                vec__9119 = null;
                Object seq__9120 = ((IFn)const__8.getRawRoot()).invoke((Object)iSeq7);
                Object first__9121 = ((IFn)const__9.getRawRoot()).invoke(seq__9120);
                Object object6 = seq__9120;
                seq__9120 = null;
                Object seq__91202 = ((IFn)const__10.getRawRoot()).invoke(object6);
                Object object7 = first__9121;
                first__9121 = null;
                Object a2 = object7;
                Object object8 = seq__91202;
                seq__91202 = null;
                Object more = object8;
                Object object9 = a2;
                if (object9 == null || object9 == Boolean.FALSE) break;
                System.arraycopy(a2, RT.uncheckedIntCast((long)0L), result2, RT.uncheckedIntCast((long)i3), RT.count((Object)a2));
                Object object10 = a2;
                a2 = null;
                Object object11 = more;
                more = null;
                G__91152 = object11;
                i2 = i3 + (long)RT.count((Object)object10);
            }
            object = result2;
            result2 = null;
        } else {
            object = null;
        }
        return object;
    }

    public Object doInvoke(Object object) {
        ISeq iSeq = (ISeq)object;
        object = null;
        return common$array_cat.invokeStatic(iSeq);
    }

    public int getRequiredArity() {
        return 0;
    }
}

