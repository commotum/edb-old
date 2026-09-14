/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.AFunction
 *  clojure.lang.IFn$OOL
 *  clojure.lang.Keyword
 *  clojure.lang.Numbers
 *  clojure.lang.RT
 *  clojure.lang.Var
 */
package datomic;

import clojure.lang.AFunction;
import clojure.lang.IFn;
import clojure.lang.Keyword;
import clojure.lang.Numbers;
import clojure.lang.RT;
import clojure.lang.Var;
import java.util.Iterator;
import java.util.List;

public final class common$cl
extends AFunction
implements IFn.OOL {
    public static final Var const__0 = RT.var((String)"datomic.common", (String)"compare");
    public static final Object const__2 = 1L;
    public static final Object const__3 = -1L;
    public static final Keyword const__4 = RT.keyword(null, (String)"else");
    public static final Object const__5 = 0L;

    public static long invokeStatic(Object a, Object b) {
        Object object;
        block6: {
            boolean hb;
            boolean ha;
            block5: {
                long c;
                Object object2 = a;
                a = null;
                Iterator as = ((List)object2).iterator();
                Object object3 = b;
                b = null;
                Iterator bs = ((List)object3).iterator();
                while (true) {
                    ha = as.hasNext();
                    hb = bs.hasNext();
                    boolean and__5236__auto__9025 = ha;
                    if (!(and__5236__auto__9025 ? hb : and__5236__auto__9025)) break block5;
                    c = ((IFn.OOL)const__0.getRawRoot()).invokePrim(as.next(), bs.next());
                    if (c != 0L) break;
                    Iterator iterator2 = as;
                    as = null;
                    Iterator iterator3 = bs;
                    bs = null;
                    bs = iterator3;
                    as = iterator2;
                }
                object = Numbers.num((long)c);
                break block6;
            }
            if (ha) {
                object = const__2;
            } else if (hb) {
                object = const__3;
            } else {
                Keyword keyword = const__4;
                object = keyword != null && keyword != Boolean.FALSE ? const__5 : null;
            }
        }
        return ((Number)object).longValue();
    }

    public Object invoke(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return new Long(common$cl.invokeStatic(object3, object4));
    }

    public final long invokePrim(Object object, Object object2) {
        Object object3 = object;
        object = null;
        Object object4 = object2;
        object2 = null;
        return common$cl.invokeStatic(object3, object4);
    }
}

