package datomic.impl.db;

/**
 * Primitive-access view of an internal datom. It exposes entity, attribute,
 * transaction, partition, and assertion state together with typed value
 * accessors so index operations can avoid unnecessary boxing.
 */
// ATOMIC-NOTE BEGIN foundation-idatum (baseline cd7192e63d883a4a34aa7de4d5bcd17e6edb692d)
// Observed: db/Datum stores tOp, derives getT by shifting it, and reconstructs
// getTx with db/make-eid in partition 3; getP extracts the entity partition.
// Its index comparator consumers use getT, while public datoms expose getTx.
// WHY: the logical transaction position and its reified entity are distinct
// coordinates even when represented by integers. Primitive access also avoids
// boxing on this JVM path. Rust src/identity.rs keeps checked t_to_tx/tx_to_t and
// partition extraction; src/datom.rs stores typed fields rather than this ABI.
// This is a native representation choice, not permission to conflate t and tx.
// ATOMIC-NOTE END foundation-idatum
public interface IDatum {
    public boolean isAssertion();

    public long getE();

    public int getA();

    public long getT();

    public long getTx();

    public int getP();

    public Object getV();

    public long getLongV();

    public double getDoubleV();

    public int getIntV();

    public float getFloatV();

    public boolean getBooleanV();
}
