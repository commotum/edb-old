/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  clojure.lang.IFn
 *  clojure.lang.IObj
 *  clojure.lang.IPersistentVector
 *  clojure.lang.IType
 *  clojure.lang.RT
 *  clojure.lang.Symbol
 *  clojure.lang.Tuple
 */
package datomic.btset;

import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentVector;
import clojure.lang.IType;
import clojure.lang.RT;
import clojure.lang.Symbol;
import clojure.lang.Tuple;
import datomic.btset.BTSetIter$fn__11822;
import datomic.btset.BTSetIter$fn__11824;
import datomic.btset.BTSetIterLink;
import datomic.btset.IBTSetBranch;
import datomic.btset.IBTSetLeaf;
import datomic.iter.Iter;

public final class BTSetIter
implements Iter,
IType {
    Object branches;
    Object leaf;
    long offset;

    public BTSetIter(Object object, Object object2, long l) {
        this.branches = object;
        this.leaf = object2;
        this.offset = l;
    }

    public static IPersistentVector getBasis() {
        return Tuple.create((Object)((IObj)Symbol.intern(null, (String)"branches")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"BTSetIterLink"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), (Object)((IObj)Symbol.intern(null, (String)"leaf")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"IBTSetLeaf"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})), (Object)((IObj)Symbol.intern(null, (String)"offset")).withMeta(RT.map((Object[])new Object[]{RT.keyword(null, (String)"tag"), Symbol.intern(null, (String)"long"), RT.keyword(null, (String)"unsynchronized-mutable"), Boolean.TRUE})));
    }

    public Object get() {
        return ((IBTSetLeaf)this.leaf).keyAt(this.offset);
    }

    public Object prev() {
        BTSetIter bTSetIter;
        if (this.offset > 0L) {
            --this.offset;
            bTSetIter = this;
        } else {
            Object bpath;
            Object object = bpath = ((IFn)new BTSetIter$fn__11824(this.branches)).invoke();
            if (object != null && object != Boolean.FALSE) {
                Object node;
                ((BTSetIterLink)bpath).decOffset();
                Object object2 = bpath;
                bpath = null;
                Object bpath2 = object2;
                while ((node = ((IBTSetBranch)((BTSetIterLink)bpath2).branch).childAt(((BTSetIterLink)bpath2).offset())) instanceof IBTSetBranch) {
                    Object object3 = node;
                    Object object4 = node;
                    node = null;
                    Object object5 = bpath2;
                    bpath2 = null;
                    bpath2 = new BTSetIterLink(object3, ((IBTSetBranch)object4).count() - 1L, object5);
                }
                Object object6 = bpath2;
                bpath2 = null;
                this.branches = object6;
                Object object7 = node;
                node = null;
                this.leaf = object7;
                this.offset = ((IBTSetLeaf)this.leaf).count() - 1L;
                bTSetIter = this;
            } else {
                bTSetIter = null;
            }
        }
        return bTSetIter;
    }

    public Object next() {
        BTSetIter bTSetIter;
        if (this.offset + 1L < ((IBTSetLeaf)this.leaf).count()) {
            ++this.offset;
            bTSetIter = this;
        } else {
            Object bpath;
            Object object = bpath = ((IFn)new BTSetIter$fn__11822(this.branches)).invoke();
            if (object != null && object != Boolean.FALSE) {
                Object node;
                ((BTSetIterLink)bpath).incOffset();
                Object object2 = bpath;
                bpath = null;
                Object bpath2 = object2;
                while ((node = ((IBTSetBranch)((BTSetIterLink)bpath2).branch).childAt(((BTSetIterLink)bpath2).offset())) instanceof IBTSetBranch) {
                    Object object3 = node;
                    node = null;
                    Object object4 = bpath2;
                    bpath2 = null;
                    bpath2 = new BTSetIterLink(object3, 0L, object4);
                }
                Object object5 = bpath2;
                bpath2 = null;
                this.branches = object5;
                Object object6 = node;
                node = null;
                this.leaf = object6;
                this.offset = 0L;
                bTSetIter = this;
            } else {
                bTSetIter = null;
            }
        }
        return bTSetIter;
    }
}

