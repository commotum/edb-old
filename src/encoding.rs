use crate::{
    Attribute, Cardinality, Datom, EntityRef, ErrorCategory, IndexOrder, Keyword, Schema,
    SchemaChange, SemanticError, Symbol, TupleSpec, TxOp, TxValue, Unique, Value, ValueType,
};
use bigdecimal::BigDecimal;
use num_bigint::BigInt;
use sha2::{Digest as _, Sha256};
use std::collections::BTreeMap;

pub type Digest = [u8; 32];

const MAGIC: &[u8; 4] = b"ATMC";
const FORMAT_VERSION: u16 = 1;
const HEADER_LEN: usize = 16;
const CHECKSUM_LEN: usize = 32;
const MAX_BLOB_LEN: usize = 64 * 1024 * 1024;
const MAX_VALUE_LEN: usize = 16 * 1024 * 1024;
const MAX_COLLECTION_LEN: usize = 1_000_000;
const KIND_SCHEMA: u8 = 1;
const KIND_TRANSACTION: u8 = 2;
const KIND_REQUEST: u8 = 3;
const KIND_INDEX_SEGMENT: u8 = 4;
const KIND_INDEX_MANIFEST: u8 = 5;

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct DurableTransaction {
    pub database_id: String,
    pub basis_t: u64,
    pub previous_hash: Digest,
    pub next_eid: u64,
    pub tempids: BTreeMap<String, u64>,
    pub tx_data: Vec<Datom>,
    pub schema_changes: Vec<SchemaChange>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct IndexSegment {
    pub order: IndexOrder,
    pub history: bool,
    pub datoms: Vec<Datom>,
}

#[derive(Clone, Debug, Eq, PartialEq)]
pub struct SegmentRef {
    pub order: IndexOrder,
    pub history: bool,
    pub ordinal: u32,
    pub hash: Digest,
    pub count: u32,
}

#[derive(Clone, Debug)]
pub struct IndexManifest {
    pub database_id: String,
    pub basis_t: u64,
    pub tx_hash: Digest,
    pub next_eid: u64,
    pub schema: Schema,
    pub schema_history: Vec<Vec<SchemaChange>>,
    pub segments: Vec<SegmentRef>,
}

pub fn sha256(bytes: &[u8]) -> Digest {
    Sha256::digest(bytes).into()
}

pub fn encode_schema(schema: &Schema) -> Result<Vec<u8>, SemanticError> {
    let body = schema_body(schema)?;
    encode_blob(KIND_SCHEMA, &body)
}

pub fn decode_schema(bytes: &[u8]) -> Result<Schema, SemanticError> {
    let body = decode_blob(bytes, KIND_SCHEMA)?;
    let mut cursor = Cursor::new(body);
    let count = cursor.collection_len()?;
    let mut schema = Schema::new();
    for _ in 0..count {
        schema.install(decode_attribute(&mut cursor)?)?;
    }
    let alias_count = cursor.collection_len()?;
    for _ in 0..alias_count {
        let ident = decode_keyword(&mut cursor)?;
        let attribute = cursor.u32()?;
        schema.install_ident_alias(ident, attribute)?;
    }
    cursor.finish()?;
    if schema_body(&schema)? != body {
        return Err(fault(
            "encoding/noncanonical-schema",
            "schema payload is not in canonical order or representation",
        ));
    }
    Ok(schema)
}

pub fn encode_transaction(transaction: &DurableTransaction) -> Result<Vec<u8>, SemanticError> {
    validate_transaction(transaction)?;
    let body = transaction_body(transaction)?;
    encode_blob(KIND_TRANSACTION, &body)
}

pub fn decode_transaction(bytes: &[u8]) -> Result<DurableTransaction, SemanticError> {
    let body = decode_blob(bytes, KIND_TRANSACTION)?;
    let mut cursor = Cursor::new(body);
    let database_id = cursor.string()?;
    let basis_t = cursor.u64()?;
    let previous_hash = cursor.digest()?;
    let next_eid = cursor.u64()?;
    let tempid_count = cursor.collection_len()?;
    let mut tempids = BTreeMap::new();
    for _ in 0..tempid_count {
        let name = cursor.string()?;
        let entity = cursor.u64()?;
        if tempids.insert(name, entity).is_some() {
            return Err(fault(
                "encoding/duplicate-tempid",
                "durable transaction contains a duplicate tempid name",
            ));
        }
    }
    let datom_count = cursor.collection_len()?;
    let mut tx_data = Vec::with_capacity(datom_count);
    for _ in 0..datom_count {
        tx_data.push(decode_datom(&mut cursor)?);
    }
    let change_count = cursor.collection_len()?;
    let mut schema_changes = Vec::with_capacity(change_count);
    for _ in 0..change_count {
        schema_changes.push(decode_schema_change(&mut cursor)?);
    }
    cursor.finish()?;
    let transaction = DurableTransaction {
        database_id,
        basis_t,
        previous_hash,
        next_eid,
        tempids,
        tx_data,
        schema_changes,
    };
    validate_transaction(&transaction)?;
    if transaction_body(&transaction)? != body {
        return Err(fault(
            "encoding/noncanonical-transaction",
            "transaction payload is not in canonical order or representation",
        ));
    }
    Ok(transaction)
}

pub fn transaction_hash(encoded_transaction: &[u8]) -> Digest {
    sha256(encoded_transaction)
}

pub fn encode_index_segment(segment: &IndexSegment) -> Result<Vec<u8>, SemanticError> {
    validate_index_segment(segment)?;
    let mut body = Vec::new();
    body.push(index_order_tag(segment.order));
    put_bool(&mut body, segment.history);
    put_len(&mut body, segment.datoms.len())?;
    for datom in &segment.datoms {
        encode_datom(&mut body, datom)?;
    }
    encode_blob(KIND_INDEX_SEGMENT, &body)
}

pub fn decode_index_segment(bytes: &[u8]) -> Result<IndexSegment, SemanticError> {
    let body = decode_blob(bytes, KIND_INDEX_SEGMENT)?;
    let mut cursor = Cursor::new(body);
    let order = decode_index_order(cursor.u8()?)?;
    let history = cursor.boolean()?;
    let count = cursor.collection_len()?;
    let mut datoms = Vec::with_capacity(count);
    for _ in 0..count {
        datoms.push(decode_datom(&mut cursor)?);
    }
    cursor.finish()?;
    let segment = IndexSegment {
        order,
        history,
        datoms,
    };
    validate_index_segment(&segment)?;
    if encode_index_segment(&segment)? != bytes {
        return Err(fault(
            "encoding/noncanonical-index-segment",
            "index segment is not canonical",
        ));
    }
    Ok(segment)
}

pub fn encode_index_manifest(manifest: &IndexManifest) -> Result<Vec<u8>, SemanticError> {
    validate_index_manifest(manifest)?;
    let mut body = Vec::new();
    put_string(&mut body, &manifest.database_id)?;
    put_u64(&mut body, manifest.basis_t);
    body.extend_from_slice(&manifest.tx_hash);
    put_u64(&mut body, manifest.next_eid);
    put_bytes(&mut body, &encode_schema(&manifest.schema)?)?;
    put_len(&mut body, manifest.schema_history.len())?;
    for changes in &manifest.schema_history {
        put_len(&mut body, changes.len())?;
        for change in changes {
            encode_schema_change(&mut body, change)?;
        }
    }
    put_len(&mut body, manifest.segments.len())?;
    for reference in &manifest.segments {
        body.push(index_order_tag(reference.order));
        put_bool(&mut body, reference.history);
        put_u32(&mut body, reference.ordinal);
        body.extend_from_slice(&reference.hash);
        put_u32(&mut body, reference.count);
    }
    encode_blob(KIND_INDEX_MANIFEST, &body)
}

pub fn decode_index_manifest(bytes: &[u8]) -> Result<IndexManifest, SemanticError> {
    let body = decode_blob(bytes, KIND_INDEX_MANIFEST)?;
    let mut cursor = Cursor::new(body);
    let database_id = cursor.string()?;
    let basis_t = cursor.u64()?;
    let tx_hash = cursor.digest()?;
    let next_eid = cursor.u64()?;
    let schema = decode_schema(cursor.bytes()?)?;
    let history_len = cursor.collection_len()?;
    let mut schema_history = Vec::with_capacity(history_len);
    for _ in 0..history_len {
        let count = cursor.collection_len()?;
        let mut changes = Vec::with_capacity(count);
        for _ in 0..count {
            changes.push(decode_schema_change(&mut cursor)?);
        }
        schema_history.push(changes);
    }
    let count = cursor.collection_len()?;
    let mut segments = Vec::with_capacity(count);
    for _ in 0..count {
        segments.push(SegmentRef {
            order: decode_index_order(cursor.u8()?)?,
            history: cursor.boolean()?,
            ordinal: cursor.u32()?,
            hash: cursor.digest()?,
            count: cursor.u32()?,
        });
    }
    cursor.finish()?;
    let manifest = IndexManifest {
        database_id,
        basis_t,
        tx_hash,
        next_eid,
        schema,
        schema_history,
        segments,
    };
    validate_index_manifest(&manifest)?;
    if encode_index_manifest(&manifest)? != bytes {
        return Err(fault(
            "encoding/noncanonical-index-manifest",
            "index manifest is not canonical",
        ));
    }
    Ok(manifest)
}

fn validate_index_segment(segment: &IndexSegment) -> Result<(), SemanticError> {
    if segment.datoms.is_empty() {
        return Err(fault(
            "encoding/empty-index-segment",
            "index segments cannot be empty",
        ));
    }
    if segment
        .datoms
        .windows(2)
        .any(|pair| pair[0].cmp_in(&pair[1], segment.order).is_gt())
    {
        return Err(fault(
            "encoding/unsorted-index-segment",
            "index segment datoms must be ordered",
        ));
    }
    Ok(())
}

fn validate_index_manifest(manifest: &IndexManifest) -> Result<(), SemanticError> {
    if manifest.database_id.is_empty() || manifest.basis_t == 0 {
        return Err(fault(
            "encoding/invalid-index-manifest",
            "index manifest needs a database id and positive basis",
        ));
    }
    if manifest.schema_history.len() != usize::try_from(manifest.basis_t).unwrap_or(usize::MAX) {
        return Err(fault(
            "encoding/index-schema-history-basis",
            "manifest schema history must have one chunk per basis",
        ));
    }
    let mut expected = std::collections::BTreeMap::<(bool, u8), u32>::new();
    let mut last_key = None;
    for reference in &manifest.segments {
        if reference.count == 0 {
            return Err(fault(
                "encoding/empty-index-reference",
                "manifest segment count must be positive",
            ));
        }
        let key = (
            reference.history,
            index_order_tag(reference.order),
            reference.ordinal,
        );
        if last_key.is_some_and(|last| last >= key) {
            return Err(fault(
                "encoding/noncanonical-index-manifest",
                "segment references must be strictly ordered",
            ));
        }
        let ordinal = expected
            .entry((reference.history, index_order_tag(reference.order)))
            .or_default();
        if reference.ordinal != *ordinal {
            return Err(fault(
                "encoding/index-segment-gap",
                "segment ordinals must be contiguous",
            ));
        }
        *ordinal += 1;
        last_key = Some(key);
    }
    Ok(())
}

fn index_order_tag(order: IndexOrder) -> u8 {
    match order {
        IndexOrder::Eavt => 0,
        IndexOrder::Aevt => 1,
        IndexOrder::Avet => 2,
        IndexOrder::Vaet => 3,
    }
}

fn decode_index_order(tag: u8) -> Result<IndexOrder, SemanticError> {
    match tag {
        0 => Ok(IndexOrder::Eavt),
        1 => Ok(IndexOrder::Aevt),
        2 => Ok(IndexOrder::Avet),
        3 => Ok(IndexOrder::Vaet),
        _ => Err(invalid_tag("index order", tag)),
    }
}

/// Canonical digest of an unordered primitive transaction request.
pub fn request_digest(
    ops: &[TxOp],
    tx_instant: i64,
    expected_basis_t: u64,
) -> Result<Digest, SemanticError> {
    let mut encoded_ops = Vec::with_capacity(ops.len());
    for op in ops {
        let mut bytes = Vec::new();
        encode_tx_op(&mut bytes, op)?;
        encoded_ops.push(bytes);
    }
    encoded_ops.sort();
    let mut body = Vec::new();
    put_u64(&mut body, expected_basis_t);
    put_i64(&mut body, tx_instant);
    put_len(&mut body, encoded_ops.len())?;
    for encoded in encoded_ops {
        put_bytes(&mut body, &encoded)?;
    }
    Ok(sha256(&encode_blob(KIND_REQUEST, &body)?))
}

fn schema_body(schema: &Schema) -> Result<Vec<u8>, SemanticError> {
    let mut attributes: Vec<_> = schema.attributes().cloned().collect();
    attributes.sort_by_key(|attribute| attribute.id);
    let mut body = Vec::new();
    put_len(&mut body, attributes.len())?;
    for attribute in &attributes {
        encode_attribute(&mut body, attribute)?;
    }
    let aliases: Vec<_> = schema.ident_aliases().collect();
    put_len(&mut body, aliases.len())?;
    for (ident, attribute) in aliases {
        encode_keyword(&mut body, ident)?;
        put_u32(&mut body, attribute);
    }
    Ok(body)
}

fn transaction_body(transaction: &DurableTransaction) -> Result<Vec<u8>, SemanticError> {
    let mut datoms = transaction
        .tx_data
        .iter()
        .map(|datom| {
            let mut encoded = Vec::new();
            encode_datom(&mut encoded, datom)?;
            Ok((datom, encoded))
        })
        .collect::<Result<Vec<_>, SemanticError>>()?;
    datoms.sort_by(|(left, left_bytes), (right, right_bytes)| {
        left.cmp_in(right, crate::IndexOrder::Eavt)
            .then_with(|| left_bytes.cmp(right_bytes))
    });
    let mut changes = transaction.schema_changes.clone();
    changes.sort_by_key(schema_change_key);
    let mut body = Vec::new();
    put_string(&mut body, &transaction.database_id)?;
    put_u64(&mut body, transaction.basis_t);
    body.extend_from_slice(&transaction.previous_hash);
    put_u64(&mut body, transaction.next_eid);
    put_len(&mut body, transaction.tempids.len())?;
    for (name, entity) in &transaction.tempids {
        put_string(&mut body, name)?;
        put_u64(&mut body, *entity);
    }
    put_len(&mut body, datoms.len())?;
    for (_, encoded) in datoms {
        body.extend_from_slice(&encoded);
    }
    put_len(&mut body, changes.len())?;
    for change in &changes {
        encode_schema_change(&mut body, change)?;
    }
    Ok(body)
}

fn validate_transaction(transaction: &DurableTransaction) -> Result<(), SemanticError> {
    if transaction.database_id.is_empty() {
        return Err(fault(
            "encoding/empty-database-id",
            "durable transaction database id cannot be empty",
        ));
    }
    if transaction.basis_t == 0 {
        return Err(fault(
            "encoding/invalid-basis",
            "durable transaction basis must be positive",
        ));
    }
    if transaction
        .tx_data
        .iter()
        .any(|datom| datom.tx != transaction.basis_t)
    {
        return Err(fault(
            "encoding/datom-transaction-mismatch",
            "every datom must name the transaction envelope basis",
        ));
    }
    for (offset, datom) in transaction.tx_data.iter().enumerate() {
        if transaction.tx_data[offset + 1..].iter().any(|other| {
            datom.entity == other.entity
                && datom.attribute == other.attribute
                && datom.value.stored_eq(&other.value)
        }) {
            return Err(fault(
                "encoding/duplicate-datom",
                "durable transaction contains duplicate or contradictory datoms",
            ));
        }
    }
    let mut changed_attributes = std::collections::BTreeSet::new();
    for change in &transaction.schema_changes {
        let attribute = match change {
            SchemaChange::Install(attribute) | SchemaChange::Alter(attribute) => attribute.id,
        };
        if !changed_attributes.insert(attribute) {
            return Err(fault(
                "encoding/duplicate-schema-change",
                "durable transaction changes one attribute more than once",
            ));
        }
    }
    Ok(())
}

fn encode_blob(kind: u8, body: &[u8]) -> Result<Vec<u8>, SemanticError> {
    if body.len() > MAX_BLOB_LEN {
        return Err(SemanticError::incorrect(
            "encoding/blob-too-large",
            format!("encoded body exceeds {MAX_BLOB_LEN} bytes"),
        ));
    }
    let mut bytes = Vec::with_capacity(HEADER_LEN + body.len() + CHECKSUM_LEN);
    bytes.extend_from_slice(MAGIC);
    bytes.push(kind);
    bytes.extend_from_slice(&FORMAT_VERSION.to_be_bytes());
    bytes.push(0);
    bytes.extend_from_slice(&(body.len() as u64).to_be_bytes());
    bytes.extend_from_slice(body);
    let checksum = sha256(&bytes);
    bytes.extend_from_slice(&checksum);
    Ok(bytes)
}

fn decode_blob(bytes: &[u8], expected_kind: u8) -> Result<&[u8], SemanticError> {
    if bytes.len() < HEADER_LEN + CHECKSUM_LEN {
        return Err(fault(
            "encoding/truncated-blob",
            "durable blob is shorter than its header and checksum",
        ));
    }
    if &bytes[..4] != MAGIC {
        return Err(fault("encoding/bad-magic", "durable blob magic is invalid"));
    }
    if bytes[4] != expected_kind {
        return Err(fault(
            "encoding/unexpected-kind",
            format!("expected blob kind {expected_kind}, got {}", bytes[4]),
        ));
    }
    let version = u16::from_be_bytes([bytes[5], bytes[6]]);
    if version != FORMAT_VERSION {
        return Err(SemanticError::new(
            ErrorCategory::Unsupported,
            "encoding/unsupported-version",
            format!("unsupported durable format version {version}"),
        ));
    }
    if bytes[7] != 0 {
        return Err(fault(
            "encoding/nonzero-reserved",
            "durable blob reserved header byte must be zero",
        ));
    }
    let body_len = u64::from_be_bytes(bytes[8..16].try_into().expect("fixed header slice"));
    let body_len = usize::try_from(body_len).map_err(|_| {
        fault(
            "encoding/blob-too-large",
            "durable blob length does not fit this platform",
        )
    })?;
    if body_len > MAX_BLOB_LEN {
        return Err(fault(
            "encoding/blob-too-large",
            format!("durable blob declares more than {MAX_BLOB_LEN} bytes"),
        ));
    }
    let expected_len = HEADER_LEN
        .checked_add(body_len)
        .and_then(|length| length.checked_add(CHECKSUM_LEN))
        .ok_or_else(|| fault("encoding/blob-too-large", "durable blob length overflow"))?;
    if bytes.len() != expected_len {
        return Err(fault(
            "encoding/length-mismatch",
            "durable blob length does not match its header",
        ));
    }
    let checksum_at = HEADER_LEN + body_len;
    if sha256(&bytes[..checksum_at]).as_slice() != &bytes[checksum_at..] {
        return Err(fault(
            "encoding/checksum-mismatch",
            "durable blob checksum does not match its contents",
        ));
    }
    Ok(&bytes[HEADER_LEN..checksum_at])
}

fn encode_datom(output: &mut Vec<u8>, datom: &Datom) -> Result<(), SemanticError> {
    put_u64(output, datom.entity);
    put_u32(output, datom.attribute);
    encode_value(output, &datom.value)?;
    put_u64(output, datom.tx);
    output.push(u8::from(datom.added));
    Ok(())
}

fn decode_datom(cursor: &mut Cursor<'_>) -> Result<Datom, SemanticError> {
    let entity = cursor.u64()?;
    let attribute = cursor.u32()?;
    let value = decode_value(cursor, 0)?;
    let tx = cursor.u64()?;
    let added = cursor.boolean()?;
    Ok(Datom {
        entity,
        attribute,
        value,
        tx,
        added,
    })
}

fn encode_schema_change(output: &mut Vec<u8>, change: &SchemaChange) -> Result<(), SemanticError> {
    match change {
        SchemaChange::Install(attribute) => {
            output.push(0);
            encode_attribute(output, attribute)
        }
        SchemaChange::Alter(attribute) => {
            output.push(1);
            encode_attribute(output, attribute)
        }
    }
}

fn decode_schema_change(cursor: &mut Cursor<'_>) -> Result<SchemaChange, SemanticError> {
    match cursor.u8()? {
        0 => Ok(SchemaChange::Install(decode_attribute(cursor)?)),
        1 => Ok(SchemaChange::Alter(decode_attribute(cursor)?)),
        tag => Err(invalid_tag("schema change", tag)),
    }
}

fn schema_change_key(change: &SchemaChange) -> (u32, u8) {
    match change {
        SchemaChange::Install(attribute) => (attribute.id, 0),
        SchemaChange::Alter(attribute) => (attribute.id, 1),
    }
}

fn encode_attribute(output: &mut Vec<u8>, attribute: &Attribute) -> Result<(), SemanticError> {
    put_u32(output, attribute.id);
    encode_keyword(output, &attribute.ident)?;
    output.push(value_type_tag(attribute.value_type));
    output.push(match attribute.cardinality {
        Cardinality::One => 0,
        Cardinality::Many => 1,
    });
    output.push(match attribute.unique {
        None => 0,
        Some(Unique::Identity) => 1,
        Some(Unique::Value) => 2,
    });
    put_bool(output, attribute.indexed);
    put_bool(output, attribute.component);
    put_bool(output, attribute.no_history);
    match &attribute.tuple {
        None => output.push(0),
        Some(TupleSpec::Homogeneous(value_type)) => {
            output.push(1);
            output.push(value_type_tag(*value_type));
        }
        Some(TupleSpec::Heterogeneous(types)) => {
            output.push(2);
            put_len(output, types.len())?;
            for value_type in types {
                output.push(value_type_tag(*value_type));
            }
        }
        Some(TupleSpec::Composite(attributes)) => {
            output.push(3);
            put_len(output, attributes.len())?;
            for attribute in attributes {
                put_u32(output, *attribute);
            }
        }
    }
    put_bool(output, attribute.tuple_discontinued);
    let mut predicates = attribute.predicates.clone();
    predicates.sort();
    predicates.dedup();
    put_len(output, predicates.len())?;
    for predicate in predicates {
        put_string(output, &predicate)?;
    }
    Ok(())
}

fn decode_attribute(cursor: &mut Cursor<'_>) -> Result<Attribute, SemanticError> {
    let id = cursor.u32()?;
    let ident = decode_keyword(cursor)?;
    let value_type = decode_value_type(cursor.u8()?)?;
    let cardinality = match cursor.u8()? {
        0 => Cardinality::One,
        1 => Cardinality::Many,
        tag => return Err(invalid_tag("cardinality", tag)),
    };
    let unique = match cursor.u8()? {
        0 => None,
        1 => Some(Unique::Identity),
        2 => Some(Unique::Value),
        tag => return Err(invalid_tag("unique", tag)),
    };
    let indexed = cursor.boolean()?;
    let component = cursor.boolean()?;
    let no_history = cursor.boolean()?;
    let tuple = match cursor.u8()? {
        0 => None,
        1 => Some(TupleSpec::Homogeneous(decode_value_type(cursor.u8()?)?)),
        2 => {
            let count = cursor.collection_len()?;
            let mut types = Vec::with_capacity(count);
            for _ in 0..count {
                types.push(decode_value_type(cursor.u8()?)?);
            }
            Some(TupleSpec::Heterogeneous(types))
        }
        3 => {
            let count = cursor.collection_len()?;
            let mut attributes = Vec::with_capacity(count);
            for _ in 0..count {
                attributes.push(cursor.u32()?);
            }
            Some(TupleSpec::Composite(attributes))
        }
        tag => return Err(invalid_tag("tuple specification", tag)),
    };
    let tuple_discontinued = cursor.boolean()?;
    let predicate_count = cursor.collection_len()?;
    let mut predicates = Vec::with_capacity(predicate_count);
    for _ in 0..predicate_count {
        predicates.push(cursor.string()?);
    }
    Ok(Attribute {
        id,
        ident,
        value_type,
        cardinality,
        unique,
        indexed,
        component,
        no_history,
        tuple,
        tuple_discontinued,
        predicates,
    })
}

fn encode_value(output: &mut Vec<u8>, value: &Value) -> Result<(), SemanticError> {
    match value {
        Value::BigDec(value) => {
            output.push(0);
            let (integer, scale) = value.as_bigint_and_exponent();
            put_i64(output, scale);
            put_bigint(output, &integer)?;
        }
        Value::BigInt(value) => {
            output.push(1);
            put_bigint(output, value)?;
        }
        Value::Bool(value) => {
            output.push(2);
            put_bool(output, *value);
        }
        Value::Bytes(value) => {
            output.push(3);
            put_bytes(output, value)?;
        }
        Value::Double(value) => {
            output.push(4);
            put_u64(output, canonical_f64_bits(*value));
        }
        Value::Float(value) => {
            output.push(5);
            put_u32(output, canonical_f32_bits(*value));
        }
        Value::Instant(value) => {
            output.push(6);
            put_i64(output, *value);
        }
        Value::Keyword(value) => {
            output.push(7);
            encode_keyword(output, value)?;
        }
        Value::Long(value) => {
            output.push(8);
            put_i64(output, *value);
        }
        Value::Ref(value) => {
            output.push(9);
            put_u64(output, *value);
        }
        Value::String(value) => {
            output.push(10);
            put_string(output, value)?;
        }
        Value::Symbol(value) => {
            output.push(11);
            encode_symbol(output, value)?;
        }
        Value::Tuple(values) => {
            output.push(12);
            put_len(output, values.len())?;
            for value in values {
                match value {
                    None => output.push(0),
                    Some(value) => {
                        output.push(1);
                        encode_value(output, value)?;
                    }
                }
            }
        }
        Value::Uuid(value) => {
            output.push(13);
            output.extend_from_slice(&value.to_be_bytes());
        }
        Value::Uri(value) => {
            output.push(14);
            put_string(output, value)?;
        }
    }
    Ok(())
}

fn decode_value(cursor: &mut Cursor<'_>, depth: usize) -> Result<Value, SemanticError> {
    if depth > 16 {
        return Err(fault(
            "encoding/value-depth",
            "encoded value nesting exceeds 16 levels",
        ));
    }
    match cursor.u8()? {
        0 => {
            let scale = cursor.i64()?;
            Ok(Value::BigDec(BigDecimal::new(cursor.bigint()?, scale)))
        }
        1 => Ok(Value::BigInt(cursor.bigint()?)),
        2 => Ok(Value::Bool(cursor.boolean()?)),
        3 => Ok(Value::Bytes(cursor.bytes()?.to_vec())),
        4 => {
            let bits = cursor.u64()?;
            let value = f64::from_bits(bits);
            if bits != canonical_f64_bits(value) {
                return Err(fault(
                    "encoding/noncanonical-float",
                    "double uses a noncanonical zero or NaN representation",
                ));
            }
            Ok(Value::Double(value))
        }
        5 => {
            let bits = cursor.u32()?;
            let value = f32::from_bits(bits);
            if bits != canonical_f32_bits(value) {
                return Err(fault(
                    "encoding/noncanonical-float",
                    "float uses a noncanonical zero or NaN representation",
                ));
            }
            Ok(Value::Float(value))
        }
        6 => Ok(Value::Instant(cursor.i64()?)),
        7 => Ok(Value::Keyword(decode_keyword(cursor)?)),
        8 => Ok(Value::Long(cursor.i64()?)),
        9 => Ok(Value::Ref(cursor.u64()?)),
        10 => Ok(Value::String(cursor.string()?)),
        11 => Ok(Value::Symbol(decode_symbol(cursor)?)),
        12 => {
            let count = cursor.collection_len()?;
            let mut values = Vec::with_capacity(count);
            for _ in 0..count {
                values.push(match cursor.u8()? {
                    0 => None,
                    1 => Some(decode_value(cursor, depth + 1)?),
                    tag => return Err(invalid_tag("tuple slot", tag)),
                });
            }
            Ok(Value::Tuple(values))
        }
        13 => Ok(Value::Uuid(u128::from_be_bytes(cursor.array()?))),
        14 => Ok(Value::Uri(cursor.string()?)),
        tag => Err(invalid_tag("value", tag)),
    }
}

fn encode_tx_op(output: &mut Vec<u8>, op: &TxOp) -> Result<(), SemanticError> {
    match op {
        TxOp::Add {
            entity,
            attribute,
            value,
        } => {
            output.push(0);
            encode_entity_ref(output, entity)?;
            put_u32(output, *attribute);
            encode_tx_value(output, value)?;
        }
        TxOp::Retract {
            entity,
            attribute,
            value,
        } => {
            output.push(1);
            encode_entity_ref(output, entity)?;
            put_u32(output, *attribute);
            match value {
                None => output.push(0),
                Some(value) => {
                    output.push(1);
                    encode_tx_value(output, value)?;
                }
            }
        }
        TxOp::Cas {
            entity,
            attribute,
            old,
            new,
        } => {
            output.push(2);
            encode_entity_ref(output, entity)?;
            put_u32(output, *attribute);
            match old {
                None => output.push(0),
                Some(value) => {
                    output.push(1);
                    encode_tx_value(output, value)?;
                }
            }
            encode_tx_value(output, new)?;
        }
        TxOp::RetractEntity(entity) => {
            output.push(3);
            encode_entity_ref(output, entity)?;
        }
        TxOp::Ensure { entity, required } => {
            output.push(4);
            encode_entity_ref(output, entity)?;
            let mut required = required.clone();
            required.sort_unstable();
            required.dedup();
            put_len(output, required.len())?;
            for attribute in required {
                put_u32(output, attribute);
            }
        }
        TxOp::InstallAttribute(attribute) => {
            output.push(5);
            encode_attribute(output, attribute)?;
        }
        TxOp::AlterAttribute(attribute) => {
            output.push(6);
            encode_attribute(output, attribute)?;
        }
    }
    Ok(())
}

fn encode_entity_ref(output: &mut Vec<u8>, entity: &EntityRef) -> Result<(), SemanticError> {
    match entity {
        EntityRef::Id(id) => {
            output.push(0);
            put_u64(output, *id);
        }
        EntityRef::Ident(ident) => {
            output.push(1);
            encode_keyword(output, ident)?;
        }
        EntityRef::Temp(tempid) => {
            output.push(2);
            put_string(output, tempid)?;
        }
        EntityRef::Lookup { attribute, value } => {
            output.push(3);
            put_u32(output, *attribute);
            encode_value(output, value)?;
        }
        EntityRef::Tx => output.push(4),
    }
    Ok(())
}

fn encode_tx_value(output: &mut Vec<u8>, value: &TxValue) -> Result<(), SemanticError> {
    match value {
        TxValue::Scalar(value) => {
            output.push(0);
            encode_value(output, value)
        }
        TxValue::Entity(entity) => {
            output.push(1);
            encode_entity_ref(output, entity)
        }
    }
}

fn encode_keyword(output: &mut Vec<u8>, value: &Keyword) -> Result<(), SemanticError> {
    put_optional_string(output, value.namespace.as_deref())?;
    put_string(output, &value.name)
}

fn decode_keyword(cursor: &mut Cursor<'_>) -> Result<Keyword, SemanticError> {
    Ok(Keyword {
        namespace: cursor.optional_string()?,
        name: cursor.string()?,
    })
}

fn encode_symbol(output: &mut Vec<u8>, value: &Symbol) -> Result<(), SemanticError> {
    put_optional_string(output, value.namespace.as_deref())?;
    put_string(output, &value.name)
}

fn decode_symbol(cursor: &mut Cursor<'_>) -> Result<Symbol, SemanticError> {
    Ok(Symbol {
        namespace: cursor.optional_string()?,
        name: cursor.string()?,
    })
}

fn put_optional_string(output: &mut Vec<u8>, value: Option<&str>) -> Result<(), SemanticError> {
    match value {
        None => output.push(0),
        Some(value) => {
            output.push(1);
            put_string(output, value)?;
        }
    }
    Ok(())
}

fn put_bigint(output: &mut Vec<u8>, value: &BigInt) -> Result<(), SemanticError> {
    put_bytes(output, &value.to_signed_bytes_be())
}

fn put_string(output: &mut Vec<u8>, value: &str) -> Result<(), SemanticError> {
    put_bytes(output, value.as_bytes())
}

fn put_bytes(output: &mut Vec<u8>, bytes: &[u8]) -> Result<(), SemanticError> {
    if bytes.len() > MAX_VALUE_LEN {
        return Err(SemanticError::incorrect(
            "encoding/value-too-large",
            format!("encoded value exceeds {MAX_VALUE_LEN} bytes"),
        ));
    }
    put_len(output, bytes.len())?;
    output.extend_from_slice(bytes);
    Ok(())
}

fn put_len(output: &mut Vec<u8>, length: usize) -> Result<(), SemanticError> {
    let length = u32::try_from(length).map_err(|_| {
        SemanticError::incorrect(
            "encoding/collection-too-large",
            "encoded collection length exceeds u32",
        )
    })?;
    put_u32(output, length);
    Ok(())
}

fn put_bool(output: &mut Vec<u8>, value: bool) {
    output.push(u8::from(value));
}

fn put_u32(output: &mut Vec<u8>, value: u32) {
    output.extend_from_slice(&value.to_be_bytes());
}

fn put_u64(output: &mut Vec<u8>, value: u64) {
    output.extend_from_slice(&value.to_be_bytes());
}

fn put_i64(output: &mut Vec<u8>, value: i64) {
    output.extend_from_slice(&value.to_be_bytes());
}

fn canonical_f32_bits(value: f32) -> u32 {
    if value.is_nan() {
        0x7fc0_0000
    } else if value == 0.0 {
        0
    } else {
        value.to_bits()
    }
}

fn canonical_f64_bits(value: f64) -> u64 {
    if value.is_nan() {
        0x7ff8_0000_0000_0000
    } else if value == 0.0 {
        0
    } else {
        value.to_bits()
    }
}

fn value_type_tag(value_type: ValueType) -> u8 {
    match value_type {
        ValueType::BigDec => 0,
        ValueType::BigInt => 1,
        ValueType::Boolean => 2,
        ValueType::Bytes => 3,
        ValueType::Double => 4,
        ValueType::Float => 5,
        ValueType::Instant => 6,
        ValueType::Keyword => 7,
        ValueType::Long => 8,
        ValueType::Ref => 9,
        ValueType::String => 10,
        ValueType::Symbol => 11,
        ValueType::Tuple => 12,
        ValueType::Uuid => 13,
        ValueType::Uri => 14,
    }
}

fn decode_value_type(tag: u8) -> Result<ValueType, SemanticError> {
    match tag {
        0 => Ok(ValueType::BigDec),
        1 => Ok(ValueType::BigInt),
        2 => Ok(ValueType::Boolean),
        3 => Ok(ValueType::Bytes),
        4 => Ok(ValueType::Double),
        5 => Ok(ValueType::Float),
        6 => Ok(ValueType::Instant),
        7 => Ok(ValueType::Keyword),
        8 => Ok(ValueType::Long),
        9 => Ok(ValueType::Ref),
        10 => Ok(ValueType::String),
        11 => Ok(ValueType::Symbol),
        12 => Ok(ValueType::Tuple),
        13 => Ok(ValueType::Uuid),
        14 => Ok(ValueType::Uri),
        tag => Err(invalid_tag("value type", tag)),
    }
}

fn invalid_tag(kind: &str, tag: u8) -> SemanticError {
    fault("encoding/invalid-tag", format!("invalid {kind} tag {tag}"))
}

fn fault(code: &'static str, message: impl Into<String>) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, code, message)
}

struct Cursor<'a> {
    bytes: &'a [u8],
    position: usize,
}

impl<'a> Cursor<'a> {
    fn new(bytes: &'a [u8]) -> Self {
        Self { bytes, position: 0 }
    }

    fn finish(&self) -> Result<(), SemanticError> {
        if self.position == self.bytes.len() {
            Ok(())
        } else {
            Err(fault(
                "encoding/trailing-bytes",
                "durable payload contains trailing bytes",
            ))
        }
    }

    fn take(&mut self, length: usize) -> Result<&'a [u8], SemanticError> {
        let end = self.position.checked_add(length).ok_or_else(|| {
            fault(
                "encoding/length-overflow",
                "durable payload length overflow",
            )
        })?;
        let value = self.bytes.get(self.position..end).ok_or_else(|| {
            fault(
                "encoding/truncated-payload",
                "durable payload ended before its declared value",
            )
        })?;
        self.position = end;
        Ok(value)
    }

    fn array<const N: usize>(&mut self) -> Result<[u8; N], SemanticError> {
        self.take(N)?
            .try_into()
            .map_err(|_| fault("encoding/truncated-payload", "invalid fixed-size value"))
    }

    fn u8(&mut self) -> Result<u8, SemanticError> {
        Ok(self.take(1)?[0])
    }

    fn u32(&mut self) -> Result<u32, SemanticError> {
        Ok(u32::from_be_bytes(self.array()?))
    }

    fn u64(&mut self) -> Result<u64, SemanticError> {
        Ok(u64::from_be_bytes(self.array()?))
    }

    fn i64(&mut self) -> Result<i64, SemanticError> {
        Ok(i64::from_be_bytes(self.array()?))
    }

    fn digest(&mut self) -> Result<Digest, SemanticError> {
        self.array()
    }

    fn collection_len(&mut self) -> Result<usize, SemanticError> {
        let length = self.u32()? as usize;
        if length > MAX_COLLECTION_LEN {
            return Err(fault(
                "encoding/collection-too-large",
                format!("encoded collection exceeds {MAX_COLLECTION_LEN} entries"),
            ));
        }
        Ok(length)
    }

    fn bytes(&mut self) -> Result<&'a [u8], SemanticError> {
        let length = self.collection_len()?;
        if length > MAX_VALUE_LEN {
            return Err(fault(
                "encoding/value-too-large",
                format!("encoded value exceeds {MAX_VALUE_LEN} bytes"),
            ));
        }
        self.take(length)
    }

    fn string(&mut self) -> Result<String, SemanticError> {
        let bytes = self.bytes()?;
        String::from_utf8(bytes.to_vec())
            .map_err(|_| fault("encoding/invalid-utf8", "encoded string is not valid UTF-8"))
    }

    fn optional_string(&mut self) -> Result<Option<String>, SemanticError> {
        match self.u8()? {
            0 => Ok(None),
            1 => Ok(Some(self.string()?)),
            tag => Err(invalid_tag("optional string", tag)),
        }
    }

    fn boolean(&mut self) -> Result<bool, SemanticError> {
        match self.u8()? {
            0 => Ok(false),
            1 => Ok(true),
            tag => Err(invalid_tag("boolean", tag)),
        }
    }

    fn bigint(&mut self) -> Result<BigInt, SemanticError> {
        let bytes = self.bytes()?;
        let value = BigInt::from_signed_bytes_be(bytes);
        if value.to_signed_bytes_be() != bytes {
            return Err(fault(
                "encoding/noncanonical-bigint",
                "BigInteger contains redundant sign bytes",
            ));
        }
        Ok(value)
    }
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::str::FromStr;

    fn schema() -> Schema {
        let mut schema = Schema::new();
        schema
            .install(Attribute::new(
                1,
                Keyword::new("db", "txInstant"),
                ValueType::Instant,
                Cardinality::One,
            ))
            .unwrap();
        schema
            .install(
                Attribute::new(
                    10,
                    Keyword::new("item", "key"),
                    ValueType::Tuple,
                    Cardinality::One,
                )
                .tuple(TupleSpec::Heterogeneous(vec![
                    ValueType::Long,
                    ValueType::String,
                ]))
                .unique(Unique::Identity)
                .predicate("valid-key"),
            )
            .unwrap();
        schema.rename(10, Keyword::new("item", "key-v2")).unwrap();
        schema
    }

    #[test]
    fn schema_encoding_is_stable_and_round_trips() {
        let schema = schema();
        let encoded = encode_schema(&schema).unwrap();
        assert_eq!(
            hex(&sha256(&encoded)),
            "25359d1f1ad2e77fc21fe496c48564868f3840e4f2876fb0b0b51fc1a179af62"
        );
        let decoded = decode_schema(&encoded).unwrap();
        assert_eq!(encode_schema(&decoded).unwrap(), encoded);
        assert_eq!(
            decoded.attributes().collect::<Vec<_>>(),
            schema.attributes().collect::<Vec<_>>()
        );
    }

    #[test]
    fn every_value_variant_round_trips_in_a_transaction() {
        let values = vec![
            Value::BigDec(BigDecimal::from_str("-12.3400").unwrap()),
            Value::BigInt(BigInt::from(-123456789_i64)),
            Value::Bool(true),
            Value::Bytes(vec![0, 128, 255]),
            Value::Double(f64::NAN),
            Value::Float(-0.0),
            Value::Instant(-1),
            Value::Keyword(Keyword::new("a", "b")),
            Value::Long(i64::MIN),
            Value::Ref(u64::MAX),
            Value::String("\u{10000}\u{e000}".into()),
            Value::Symbol(Symbol::unqualified("symbol")),
            Value::Tuple(vec![None, Some(Value::Long(7))]),
            Value::Uuid(u128::MAX),
            Value::Uri("https://example.test/a".into()),
        ];
        let transaction = DurableTransaction {
            database_id: "encoding-test".into(),
            basis_t: 1,
            previous_hash: [0; 32],
            next_eid: 1_000,
            tempids: BTreeMap::new(),
            tx_data: values
                .into_iter()
                .enumerate()
                .map(|(offset, value)| Datom {
                    entity: offset as u64 + 1,
                    attribute: 10,
                    value,
                    tx: 1,
                    added: true,
                })
                .collect(),
            schema_changes: vec![],
        };
        let encoded = encode_transaction(&transaction).unwrap();
        let decoded = decode_transaction(&encoded).unwrap();
        assert_eq!(encode_transaction(&decoded).unwrap(), encoded);
        assert_eq!(decoded.tx_data.len(), transaction.tx_data.len());
    }

    #[test]
    fn checksum_version_truncation_and_wrong_kind_fail_closed() {
        let encoded = encode_schema(&schema()).unwrap();
        let mut corrupt = encoded.clone();
        corrupt[HEADER_LEN] ^= 1;
        assert_eq!(
            decode_schema(&corrupt).unwrap_err().code,
            "encoding/checksum-mismatch"
        );
        let mut unsupported = encoded.clone();
        unsupported[5..7].copy_from_slice(&2_u16.to_be_bytes());
        assert_eq!(
            decode_schema(&unsupported).unwrap_err().code,
            "encoding/unsupported-version"
        );
        assert_eq!(
            decode_schema(&encoded[..encoded.len() - 1])
                .unwrap_err()
                .code,
            "encoding/length-mismatch"
        );
        assert_eq!(
            decode_transaction(&encoded).unwrap_err().code,
            "encoding/unexpected-kind"
        );

        let mut oversized = encoded.clone();
        oversized[8..16].copy_from_slice(&((MAX_BLOB_LEN as u64) + 1).to_be_bytes());
        assert_eq!(
            decode_schema(&oversized).unwrap_err().code,
            "encoding/blob-too-large"
        );
    }

    #[test]
    fn noncanonical_transaction_order_fails_closed() {
        let transaction = DurableTransaction {
            database_id: "encoding-test".into(),
            basis_t: 1,
            previous_hash: [0; 32],
            next_eid: 1_000,
            tempids: BTreeMap::new(),
            tx_data: vec![
                Datom {
                    entity: 2,
                    attribute: 1,
                    value: Value::Instant(2),
                    tx: 1,
                    added: true,
                },
                Datom {
                    entity: 1,
                    attribute: 1,
                    value: Value::Instant(1),
                    tx: 1,
                    added: true,
                },
            ],
            schema_changes: vec![],
        };
        let mut body = Vec::new();
        put_string(&mut body, &transaction.database_id).unwrap();
        put_u64(&mut body, transaction.basis_t);
        body.extend_from_slice(&transaction.previous_hash);
        put_u64(&mut body, transaction.next_eid);
        put_len(&mut body, transaction.tempids.len()).unwrap();
        put_len(&mut body, transaction.tx_data.len()).unwrap();
        for datom in &transaction.tx_data {
            encode_datom(&mut body, datom).unwrap();
        }
        put_len(&mut body, 0).unwrap();
        let encoded = encode_blob(KIND_TRANSACTION, &body).unwrap();
        assert_eq!(
            decode_transaction(&encoded).unwrap_err().code,
            "encoding/noncanonical-transaction"
        );
    }

    #[test]
    fn stored_distinctions_break_semantic_index_ties_canonically() {
        let datom = |value| Datom {
            entity: 1,
            attribute: 10,
            value: Value::BigDec(BigDecimal::from_str(value).unwrap()),
            tx: 1,
            added: true,
        };
        let mut transaction = DurableTransaction {
            database_id: "encoding-test".into(),
            basis_t: 1,
            previous_hash: [0; 32],
            next_eid: 1_000,
            tempids: BTreeMap::new(),
            tx_data: vec![datom("1.0"), datom("1.00")],
            schema_changes: vec![],
        };
        let forward = encode_transaction(&transaction).unwrap();
        transaction.tx_data.reverse();
        assert_eq!(encode_transaction(&transaction).unwrap(), forward);
    }

    #[test]
    fn request_digest_is_independent_of_operation_order() {
        let mut ops = vec![
            TxOp::Add {
                entity: EntityRef::Temp("x".into()),
                attribute: 10,
                value: TxValue::Scalar(Value::Long(1)),
            },
            TxOp::RetractEntity(EntityRef::Id(42)),
        ];
        let forward = request_digest(&ops, 1_000, 7).unwrap();
        ops.reverse();
        assert_eq!(request_digest(&ops, 1_000, 7).unwrap(), forward);
        assert_ne!(request_digest(&ops, 1_001, 7).unwrap(), forward);
        assert_ne!(request_digest(&ops, 1_000, 8).unwrap(), forward);
    }

    fn hex(bytes: &[u8]) -> String {
        bytes.iter().map(|byte| format!("{byte:02x}")).collect()
    }
}
