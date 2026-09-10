//! Versioned native process-boundary grammar, separate from request hashing
//! and immutable database values. Reuses their scalar/form representations.
use super::*;
use crate::peer::ExactEndpoint;
use crate::{DatabaseIdentity, ServiceTransactionReport, TransactionRequest};

const REQUEST: u8 = 11;
const RESPONSE: u8 = 12;
const VERSION: u8 = 1;

pub(crate) struct WireReport {
    pub before: ExactEndpoint,
    pub after: ExactEndpoint,
    pub before_manifest: Digest,
    pub after_manifest: Digest,
    pub tx_data: Vec<Datom>,
    pub tempids: BTreeMap<String, u64>,
    pub replayed: bool,
}

pub(crate) enum WireOutcome {
    Committed(Box<WireReport>),
    Rejected(SemanticError),
}

pub(crate) fn encode_submission(
    identity: &DatabaseIdentity,
    request: &TransactionRequest,
) -> Result<Vec<u8>, SemanticError> {
    let mut body = vec![VERSION];
    put_string(&mut body, identity.database_id())?;
    put_string(&mut body, identity.lineage_id())?;
    put_string(&mut body, &request.request_key)?;
    body.push(u8::from(request.compare_basis_t.is_some()));
    if let Some(value) = request.compare_basis_t {
        put_u64(&mut body, value);
    }
    body.push(u8::from(request.tx_instant_override.is_some()));
    if let Some(value) = request.tx_instant_override {
        put_i64(&mut body, value);
    }
    put_len(&mut body, request.forms.len())?;
    for form in &request.forms {
        let mut bytes = Vec::new();
        encode_persistent_tx_form(&mut bytes, form)?;
        put_framed_bytes(&mut body, &bytes)?;
    }
    // Form encoding has already enforced shape/depth limits before this walk.
    if forms_have_fulltext_attributes(&request.forms) {
        body[0] = 4;
    } else if crate::transaction::forms_have_partition_directives(&request.forms) {
        body[0] = 3;
    } else if crate::transaction::forms_have_extended_inputs(&request.forms) {
        body[0] = 2;
    }
    encode_blob(REQUEST, &body)
}

pub(crate) fn decode_submission(
    bytes: &[u8],
) -> Result<(DatabaseIdentity, TransactionRequest), SemanticError> {
    let mut cursor = Cursor::new(decode_blob(bytes, REQUEST)?);
    let version = cursor.u8()?;
    if ![1, 2, 3, 4].contains(&version) {
        return Err(fault(
            "transport/version",
            "unsupported native submission version",
        ));
    }
    let identity = DatabaseIdentity::new(cursor.string()?, cursor.string()?);
    let request_key = cursor.string()?;
    let compare_basis_t = if cursor.boolean()? {
        Some(cursor.u64()?)
    } else {
        None
    };
    let tx_instant_override = if cursor.boolean()? {
        Some(cursor.i64()?)
    } else {
        None
    };
    let count = cursor.collection_len()?;
    let mut forms = Vec::new();
    for _ in 0..count {
        let mut form = Cursor::new(framed(&mut cursor)?);
        forms.push(decode_form(&mut form)?);
        form.finish()?;
    }
    cursor.finish()?;
    if version < 4 && forms_have_fulltext_attributes(&forms) {
        return Err(fault(
            "transport/version",
            "fulltext attribute descriptors require native submission version 4",
        ));
    }
    if version < 3 && crate::transaction::forms_have_partition_directives(&forms) {
        return Err(fault(
            "transport/version",
            "partition directives require native submission version 3",
        ));
    }
    if version == 1 && crate::transaction::forms_have_extended_inputs(&forms) {
        return Err(fault(
            "transport/version",
            "structured transaction inputs require native submission version 2",
        ));
    }
    Ok((
        identity,
        TransactionRequest {
            request_key,
            forms,
            compare_basis_t,
            tx_instant_override,
        },
    ))
}

fn forms_have_fulltext_attributes(forms: &[TxForm]) -> bool {
    forms.iter().any(|form| matches!(form,
        TxForm::Op(TxOp::InstallAttribute(attribute) | TxOp::AlterAttribute(attribute)) if attribute.fulltext))
}

fn framed<'a>(cursor: &mut Cursor<'a>) -> Result<&'a [u8], SemanticError> {
    let length = cursor.u32()? as usize;
    if length > MAX_BLOB_LEN {
        return Err(fault(
            "transport/frame-limit",
            "compound form exceeds envelope limit",
        ));
    }
    cursor.take(length)
}

fn decode_form(cursor: &mut Cursor<'_>) -> Result<TxForm, SemanticError> {
    Ok(match cursor.u8()? {
        0 => TxForm::Op(decode_op(cursor)?),
        1 => TxForm::EntityMap(decode_map(cursor, 0)?),
        2 => {
            let function = match cursor.u8()? {
                0 => CallableRef::Database(decode_entity_ref(cursor)?),
                1 => CallableRef::ExactHash(cursor.digest()?),
                2 => CallableRef::Local(decode_symbol(cursor)?),
                tag => return Err(invalid_tag("callable", tag)),
            };
            let count = cursor.collection_len()?;
            let mut arguments = Vec::new();
            for _ in 0..count {
                arguments.push(decode_runtime(cursor, 0)?);
            }
            TxForm::ProgramCall(ProgramCall {
                function,
                arguments,
            })
        }
        tag => return Err(invalid_tag("transaction form", tag)),
    })
}

fn decode_op(cursor: &mut Cursor<'_>) -> Result<TxOp, SemanticError> {
    Ok(match cursor.u8()? {
        0 => TxOp::Add {
            entity: decode_entity_ref(cursor)?,
            attribute: cursor.u32()?,
            value: decode_tx_value(cursor)?,
        },
        1 => TxOp::Retract {
            entity: decode_entity_ref(cursor)?,
            attribute: cursor.u32()?,
            value: if cursor.boolean()? {
                Some(decode_tx_value(cursor)?)
            } else {
                None
            },
        },
        2 => TxOp::Cas {
            entity: decode_entity_ref(cursor)?,
            attribute: cursor.u32()?,
            old: if cursor.boolean()? {
                Some(decode_tx_value(cursor)?)
            } else {
                None
            },
            new: decode_tx_value(cursor)?,
        },
        3 => TxOp::RetractEntity(decode_entity_ref(cursor)?),
        4 => TxOp::Ensure {
            entity: decode_entity_ref(cursor)?,
            spec: decode_entity_ref(cursor)?,
        },
        5 => TxOp::InstallAttribute(decode_attribute(cursor)?),
        6 => TxOp::AlterAttribute(decode_attribute(cursor)?),
        9 => TxOp::InstallAttribute(decode_attribute(cursor)?.fulltext()),
        10 => TxOp::AlterAttribute(decode_attribute(cursor)?.fulltext()),
        7 => TxOp::ForcePartition {
            tempid: cursor.string()?,
            partition: decode_entity_ref(cursor)?,
        },
        8 => TxOp::MatchPartition {
            tempid: cursor.string()?,
            entity: decode_entity_ref(cursor)?,
        },
        tag => return Err(invalid_tag("transaction operation", tag)),
    })
}

fn decode_map(cursor: &mut Cursor<'_>, depth: usize) -> Result<EntityMap, SemanticError> {
    depth_limit(depth, 32)?;
    let id = if cursor.boolean()? {
        Some(decode_entity_ref(cursor)?)
    } else {
        None
    };
    let count = cursor.collection_len()?;
    let mut attributes = Vec::new();
    for _ in 0..count {
        let mut entry = Cursor::new(framed(cursor)?);
        let attribute = match entry.u8()? {
            0 => AttributeRef::Id(entry.u32()?),
            1 => AttributeRef::Ident(decode_keyword(&mut entry)?),
            2 => AttributeRef::ReverseId(entry.u32()?),
            3 => AttributeRef::ReverseIdent(decode_keyword(&mut entry)?),
            tag => return Err(invalid_tag("attribute reference", tag)),
        };
        attributes.push((attribute, decode_map_value(&mut entry, depth + 1)?));
        entry.finish()?;
    }
    Ok(EntityMap { id, attributes })
}

fn decode_map_value(cursor: &mut Cursor<'_>, depth: usize) -> Result<MapValue, SemanticError> {
    depth_limit(depth, 32)?;
    Ok(match cursor.u8()? {
        0 => MapValue::Value(decode_tx_value(cursor)?),
        1 => MapValue::Nested(Box::new(decode_map(cursor, depth + 1)?)),
        2 => {
            let count = cursor.collection_len()?;
            let mut values = Vec::new();
            for _ in 0..count {
                let mut value = Cursor::new(framed(cursor)?);
                values.push(decode_map_value(&mut value, depth + 1)?);
                value.finish()?;
            }
            MapValue::Many(values)
        }
        tag => return Err(invalid_tag("map value", tag)),
    })
}

fn decode_attribute(cursor: &mut Cursor<'_>) -> Result<Attribute, SemanticError> {
    let mut attribute = Attribute::new(
        cursor.u32()?,
        decode_keyword(cursor)?,
        decode_value_type(cursor)?,
        match cursor.u8()? {
            0 => Cardinality::One,
            1 => Cardinality::Many,
            tag => return Err(invalid_tag("cardinality", tag)),
        },
    );
    attribute.unique = match cursor.u8()? {
        0 => None,
        1 => Some(Unique::Identity),
        2 => Some(Unique::Value),
        tag => return Err(invalid_tag("uniqueness", tag)),
    };
    attribute.indexed = cursor.boolean()?;
    attribute.component = cursor.boolean()?;
    attribute.no_history = cursor.boolean()?;
    attribute.tuple = match cursor.u8()? {
        0 => None,
        1 => Some(TupleSpec::Homogeneous(decode_value_type(cursor)?)),
        2 => {
            let count = cursor.collection_len()?;
            let mut values = Vec::new();
            for _ in 0..count {
                values.push(decode_value_type(cursor)?);
            }
            Some(TupleSpec::Heterogeneous(values))
        }
        3 => {
            let count = cursor.collection_len()?;
            let mut values = Vec::new();
            for _ in 0..count {
                values.push(cursor.u32()?);
            }
            Some(TupleSpec::Composite(values))
        }
        tag => return Err(invalid_tag("tuple specification", tag)),
    };
    attribute.tuple_discontinued = cursor.boolean()?;
    let count = cursor.collection_len()?;
    for _ in 0..count {
        attribute.predicates.push(cursor.string()?);
    }
    Ok(attribute)
}

fn decode_value_type(cursor: &mut Cursor<'_>) -> Result<ValueType, SemanticError> {
    Ok(match cursor.u8()? {
        0 => ValueType::BigDec,
        1 => ValueType::BigInt,
        2 => ValueType::Boolean,
        3 => ValueType::Bytes,
        4 => ValueType::Double,
        5 => ValueType::Float,
        6 => ValueType::Instant,
        7 => ValueType::Keyword,
        8 => ValueType::Long,
        9 => ValueType::Ref,
        10 => ValueType::String,
        11 => ValueType::Symbol,
        12 => ValueType::Tuple,
        13 => ValueType::Uuid,
        14 => ValueType::Uri,
        15 => ValueType::Function,
        tag => return Err(invalid_tag("value type", tag)),
    })
}

fn depth_limit(depth: usize, maximum: usize) -> Result<(), SemanticError> {
    if depth > maximum {
        return Err(fault(
            "transport/value-depth",
            "native request nesting limit exceeded",
        ));
    }
    Ok(())
}

fn decode_runtime(cursor: &mut Cursor<'_>, depth: usize) -> Result<RuntimeValue, SemanticError> {
    depth_limit(depth, 16)?;
    Ok(match cursor.u8()? {
        0 => RuntimeValue::Scalar(decode_value(cursor, 0)?),
        1 => RuntimeValue::Entity(decode_entity_ref(cursor)?),
        2 => RuntimeValue::Null,
        3 => {
            let count = cursor.collection_len()?;
            let mut values = Vec::new();
            for _ in 0..count {
                values.push(decode_runtime(cursor, depth + 1)?);
            }
            RuntimeValue::Vector(values)
        }
        4 => {
            let count = cursor.collection_len()?;
            let mut values = Vec::new();
            for _ in 0..count {
                values.push((decode_value(cursor, 0)?, decode_runtime(cursor, depth + 1)?));
            }
            RuntimeValue::Map(values)
        }
        tag => return Err(invalid_tag("runtime value", tag)),
    })
}

fn put_endpoint(bytes: &mut Vec<u8>, endpoint: ExactEndpoint) {
    put_u64(bytes, endpoint.generation);
    put_u64(bytes, endpoint.basis_t);
    bytes.extend_from_slice(&endpoint.tx_hash);
    bytes.extend_from_slice(&endpoint.state_hash);
    put_u64(bytes, endpoint.eidx_frontier);
}

fn endpoint(cursor: &mut Cursor<'_>) -> Result<ExactEndpoint, SemanticError> {
    Ok(ExactEndpoint {
        generation: cursor.u64()?,
        basis_t: cursor.u64()?,
        tx_hash: cursor.digest()?,
        state_hash: cursor.digest()?,
        eidx_frontier: cursor.u64()?,
    })
}

pub(crate) fn encode_submission_outcome(
    result: &Result<ServiceTransactionReport, SemanticError>,
) -> Result<Vec<u8>, SemanticError> {
    let mut bytes = vec![VERSION];
    match result {
        Ok(report) => {
            bytes.push(0);
            for database in [&report.db_before, &report.db_after] {
                let snapshot = database.native_tiered_snapshot().ok_or_else(|| {
                    fault(
                        "transport/non-native-report",
                        "report must contain native values",
                    )
                })?;
                put_endpoint(&mut bytes, snapshot.endpoint());
                bytes.extend_from_slice(&snapshot.required_manifest_hash()?);
            }
            bytes.push(u8::from(report.replayed));
            put_len(&mut bytes, report.tx_data.len())?;
            for datom in &report.tx_data {
                encode_datom(&mut bytes, datom)?;
            }
            put_len(&mut bytes, report.tempids.len())?;
            for (name, entity) in &report.tempids {
                put_string(&mut bytes, name)?;
                put_u64(&mut bytes, *entity);
            }
        }
        Err(error) => {
            bytes.push(1);
            let categories = categories();
            bytes.push(
                categories
                    .iter()
                    .position(|category| *category == error.category)
                    .expect("all categories covered") as u8,
            );
            put_string(&mut bytes, error.code)?;
            put_string(&mut bytes, &error.message)?;
            put_len(&mut bytes, error.details.len())?;
            for (key, value) in &error.details {
                put_string(&mut bytes, key)?;
                put_string(&mut bytes, value)?;
            }
            bytes.push(u8::from(error.anomaly.is_some()));
            if let Some(anomaly) = &error.anomaly {
                encode_runtime_value(&mut bytes, anomaly, 0)?;
                if crate::transaction::runtime_has_extended_inputs(anomaly) {
                    bytes[0] = 2;
                }
            }
        }
    }
    encode_blob(RESPONSE, &bytes)
}

fn categories() -> [ErrorCategory; 10] {
    use ErrorCategory::*;
    [
        Incorrect,
        Forbidden,
        Unsupported,
        NotFound,
        Conflict,
        Busy,
        Unavailable,
        Interrupted,
        Fault,
        UnknownOutcome,
    ]
}

pub(crate) fn decode_submission_outcome(bytes: &[u8]) -> Result<WireOutcome, SemanticError> {
    let mut cursor = Cursor::new(decode_blob(bytes, RESPONSE)?);
    let version = cursor.u8()?;
    if ![1, 2].contains(&version) {
        return Err(fault(
            "transport/version",
            "unsupported native outcome version",
        ));
    }
    let result = match cursor.u8()? {
        0 => {
            let before = endpoint(&mut cursor)?;
            let before_manifest = cursor.digest()?;
            let after = endpoint(&mut cursor)?;
            let after_manifest = cursor.digest()?;
            let replayed = cursor.boolean()?;
            let count = cursor.collection_len()?;
            let mut tx_data = Vec::new();
            for _ in 0..count {
                tx_data.push(decode_datom(&mut cursor)?);
            }
            let count = cursor.collection_len()?;
            let mut tempids = BTreeMap::new();
            for _ in 0..count {
                if tempids.insert(cursor.string()?, cursor.u64()?).is_some() {
                    return Err(fault(
                        "transport/duplicate-tempid",
                        "duplicate receipt tempid",
                    ));
                }
            }
            WireOutcome::Committed(Box::new(WireReport {
                before,
                after,
                before_manifest,
                after_manifest,
                tx_data,
                tempids,
                replayed,
            }))
        }
        1 => {
            let tag = cursor.u8()?;
            let category = *categories()
                .get(usize::from(tag))
                .ok_or_else(|| invalid_tag("error category", tag))?;
            let remote_code = cursor.string()?;
            let mut error =
                SemanticError::new(category, "transport/remote-error", cursor.string()?);
            let count = cursor.collection_len()?;
            for _ in 0..count {
                error.details.insert(cursor.string()?, cursor.string()?);
            }
            error.details.insert("remote_code".into(), remote_code);
            if cursor.boolean()? {
                error.anomaly = Some(Box::new(decode_runtime(&mut cursor, 0)?));
                if version == 1
                    && error
                        .anomaly
                        .as_deref()
                        .is_some_and(crate::transaction::runtime_has_extended_inputs)
                {
                    return Err(fault(
                        "transport/version",
                        "structured reference anomalies require native outcome version 2",
                    ));
                }
            }
            WireOutcome::Rejected(error)
        }
        tag => return Err(invalid_tag("transaction outcome", tag)),
    };
    cursor.finish()?;
    Ok(result)
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn partition_directives_use_new_wire_grammar_and_preserve_request_identity() {
        let identity = DatabaseIdentity::new("catalog", "lineage");
        let forms = vec![
            TxForm::Op(TxOp::ForcePartition {
                tempid: "owner".into(),
                partition: EntityRef::Ident(Keyword::new("part", "customers")),
            }),
            TxForm::Op(TxOp::MatchPartition {
                tempid: "child".into(),
                entity: EntityRef::Temp("owner".into()),
            }),
            TxForm::Op(TxOp::MatchPartition {
                tempid: "lookup".into(),
                entity: EntityRef::LookupInput {
                    attribute: 1000,
                    value: Box::new(TxValue::Tuple(vec![Some(Value::Long(1).into()), None])),
                },
            }),
        ];
        let digest = submission_request_digest(&forms, Some(4), Some(100)).unwrap();
        let mut reordered = forms.clone();
        reordered.reverse();
        assert_eq!(
            digest,
            submission_request_digest(&reordered, Some(4), Some(100)).unwrap()
        );
        let request = TransactionRequest {
            request_key: "partition-policy".into(),
            forms: forms.clone(),
            compare_basis_t: Some(4),
            tx_instant_override: Some(100),
        };
        let bytes = encode_submission(&identity, &request).unwrap();
        assert_eq!(decode_blob(&bytes, REQUEST).unwrap()[0], 3);
        let (_, decoded) = decode_submission(&bytes).unwrap();
        assert_eq!(encode_submission(&identity, &decoded).unwrap(), bytes);
        assert_eq!(
            submission_request_digest(
                &decoded.forms,
                decoded.compare_basis_t,
                decoded.tx_instant_override
            )
            .unwrap(),
            digest
        );
        for old_version in [1, 2] {
            let mut body = decode_blob(&bytes, REQUEST).unwrap().to_vec();
            body[0] = old_version;
            assert_eq!(
                decode_submission(&encode_blob(REQUEST, &body).unwrap())
                    .err()
                    .unwrap()
                    .code,
                "transport/version"
            );
        }
        for end in 0..bytes.len() {
            assert!(decode_submission(&bytes[..end]).is_err());
        }
        let mut changed = forms;
        changed[0] = TxForm::Op(TxOp::ForcePartition {
            tempid: "owner".into(),
            partition: EntityRef::Id(4),
        });
        assert_ne!(
            submission_request_digest(&changed, Some(4), Some(100)).unwrap(),
            digest
        );
        assert_ne!(
            submission_request_digest(&changed, Some(4), Some(100)).unwrap(),
            submission_request_digest(&changed[1..], Some(4), Some(100)).unwrap()
        );
    }

    #[test]
    fn tuple_input_versions_are_explicit_and_old_receipt_hashes_remain_stable() {
        // Actual pre-extension receipt hashes from the Goal 2 independent-
        // process workflow, not expectations computed by this new encoder.
        for (name, expected) in [
            (
                "Ada",
                "71acc726c2c5243f67e09eb7afa4d8482790a579d51eec4949d9b22732e50133",
            ),
            (
                "Grace",
                "d1dc8d55e6f0dd4c14d14114486f366a503fa22dcd2265bdfaf6b14aa7ca41d9",
            ),
        ] {
            let form = TxForm::EntityMap(EntityMap {
                id: Some(EntityRef::Temp("person".into())),
                attributes: vec![(
                    AttributeRef::Ident(Keyword::new("person", "name")),
                    MapValue::Value(Value::String(name.into()).into()),
                )],
            });
            let digest = submission_request_digest(&[form], None, None).unwrap();
            assert_eq!(
                digest
                    .iter()
                    .map(|byte| format!("{byte:02x}"))
                    .collect::<String>(),
                expected
            );
        }
        let identity = DatabaseIdentity::new("catalog", "lineage");
        let old = TransactionRequest::new("old", vec![]);
        let old_bytes = encode_submission(&identity, &old).unwrap();
        assert_eq!(decode_blob(&old_bytes, REQUEST).unwrap()[0], 1);
        let new = TransactionRequest::new(
            "tuple",
            vec![TxOp::Add {
                entity: EntityRef::Temp("owner".into()),
                attribute: 1_000,
                value: TxValue::Tuple(vec![
                    Some(TxValue::Entity(EntityRef::Temp("target".into()))),
                    None,
                ]),
            }],
        );
        let bytes = encode_submission(&identity, &new).unwrap();
        assert_eq!(decode_blob(&bytes, REQUEST).unwrap()[0], 2);
        let (_, decoded) = decode_submission(&bytes).unwrap();
        assert_eq!(
            submission_request_digest(&new.forms, None, None).unwrap(),
            submission_request_digest(&decoded.forms, None, None).unwrap()
        );
        let mut downgraded = decode_blob(&bytes, REQUEST).unwrap().to_vec();
        downgraded[0] = 1;
        assert_eq!(
            decode_submission(&encode_blob(REQUEST, &downgraded).unwrap())
                .unwrap_err()
                .code,
            "transport/version"
        );
        for length in 0..bytes.len() {
            assert!(decode_submission(&bytes[..length]).is_err());
        }
    }

    #[test]
    fn native_submission_round_trip_preserves_all_form_shapes_and_identity() {
        let entity = EntityRef::Temp("item".into());
        let attribute = Attribute::new(
            1_000,
            Keyword::new("item", "tuple"),
            ValueType::Tuple,
            Cardinality::Many,
        )
        .tuple(TupleSpec::Heterogeneous(vec![
            ValueType::String,
            ValueType::Ref,
        ]));
        let operations = vec![
            TxOp::InstallAttribute(attribute.clone()),
            TxOp::AlterAttribute(attribute),
            TxOp::InstallAttribute(
                Attribute::new(
                    1_001,
                    Keyword::new("item", "text"),
                    ValueType::String,
                    Cardinality::One,
                )
                .fulltext(),
            ),
            TxOp::AlterAttribute(
                Attribute::new(
                    1_001,
                    Keyword::new("item", "text"),
                    ValueType::String,
                    Cardinality::One,
                )
                .fulltext(),
            ),
            TxOp::Add {
                entity: entity.clone(),
                attribute: 1_000,
                value: TxValue::Entity(EntityRef::Tx),
            },
            TxOp::Retract {
                entity: entity.clone(),
                attribute: 1_000,
                value: None,
            },
            TxOp::Retract {
                entity: entity.clone(),
                attribute: 1_000,
                value: Some(Value::Long(1).into()),
            },
            TxOp::Cas {
                entity: entity.clone(),
                attribute: 1_000,
                old: Some(Value::Long(1).into()),
                new: Value::Long(2).into(),
            },
            TxOp::Ensure {
                entity: entity.clone(),
                spec: EntityRef::Ident(Keyword::new("spec", "valid")),
            },
            TxOp::RetractEntity(EntityRef::Lookup {
                attribute: 1_000,
                value: Value::String("identity".into()),
            }),
        ];
        let mut request = TransactionRequest::new("key", operations)
            .comparing_basis(9)
            .with_tx_instant(42);
        request.forms.push(TxForm::EntityMap(EntityMap {
            id: Some(entity),
            attributes: vec![(
                AttributeRef::ReverseIdent(Keyword::new("item", "parent")),
                MapValue::Many(vec![
                    MapValue::Nested(Box::new(EntityMap {
                        id: None,
                        attributes: vec![(
                            AttributeRef::Id(1_001),
                            MapValue::Value(Value::String("child".into()).into()),
                        )],
                    })),
                    MapValue::Value(TxValue::Entity(EntityRef::Id(42))),
                ]),
            )],
        }));
        for function in [
            CallableRef::ExactHash([8; 32]),
            CallableRef::Database(EntityRef::Id(17)),
            CallableRef::Local(Symbol::new("app", "function")),
        ] {
            request.forms.push(TxForm::ProgramCall(ProgramCall {
                function,
                arguments: vec![
                    RuntimeValue::Vector(vec![
                        RuntimeValue::Null,
                        RuntimeValue::Entity(EntityRef::Tx),
                    ]),
                    RuntimeValue::Map(vec![(
                        Value::Long(1),
                        RuntimeValue::Scalar(Value::String("argument".into())),
                    )]),
                ],
            }));
        }
        let identity = DatabaseIdentity::new("name", "stable-lineage");
        let encoded = encode_submission(&identity, &request).unwrap();
        let (decoded_identity, decoded) = decode_submission(&encoded).unwrap();
        assert_eq!(identity, decoded_identity);
        assert_eq!(request.request_key, decoded.request_key);
        assert_eq!(
            encode_submission(&decoded_identity, &decoded).unwrap(),
            encoded
        );
        assert_eq!(
            submission_request_digest(
                &request.forms,
                request.compare_basis_t,
                request.tx_instant_override
            )
            .unwrap(),
            submission_request_digest(
                &decoded.forms,
                decoded.compare_basis_t,
                decoded.tx_instant_override
            )
            .unwrap()
        );
        for end in 0..encoded.len() {
            assert!(decode_submission(&encoded[..end]).is_err());
        }
    }

    #[test]
    fn fulltext_descriptors_require_new_wire_grammar_but_false_keeps_old_bytes() {
        let identity = DatabaseIdentity::new("catalog", "lineage");
        let attribute = Attribute::new(
            1_000,
            Keyword::new("item", "text"),
            ValueType::String,
            Cardinality::One,
        );
        let old = TransactionRequest::new("old", vec![TxOp::InstallAttribute(attribute.clone())]);
        let encoded = encode_submission(&identity, &old).unwrap();
        assert_eq!(decode_blob(&encoded, REQUEST).unwrap()[0], 1);
        let (_, decoded) = decode_submission(&encoded).unwrap();
        assert!(
            matches!(&decoded.forms[0], TxForm::Op(TxOp::InstallAttribute(attribute)) if !attribute.fulltext)
        );
        let new =
            TransactionRequest::new("new", vec![TxOp::InstallAttribute(attribute.fulltext())]);
        let encoded = encode_submission(&identity, &new).unwrap();
        let body = decode_blob(&encoded, REQUEST).unwrap();
        assert_eq!(body[0], 4);
        let (_, decoded) = decode_submission(&encoded).unwrap();
        assert!(
            matches!(&decoded.forms[0], TxForm::Op(TxOp::InstallAttribute(attribute)) if attribute.fulltext)
        );
        for version in [1, 2, 3] {
            let mut downgraded = body.to_vec();
            downgraded[0] = version;
            assert_eq!(
                decode_submission(&encode_blob(REQUEST, &downgraded).unwrap())
                    .err()
                    .unwrap()
                    .code,
                "transport/version"
            );
        }
    }

    #[test]
    fn remote_errors_preserve_categories_details_and_structured_anomalies() {
        for category in categories() {
            let mut error = SemanticError::new(category, "app/example", "structured rejection")
                .detail("context", "preserved");
            error.anomaly = Some(Box::new(RuntimeValue::Vector(vec![
                RuntimeValue::Null,
                RuntimeValue::Scalar(Value::Bool(false)),
            ])));
            let bytes = encode_submission_outcome(&Err(error.clone())).unwrap();
            let WireOutcome::Rejected(decoded) = decode_submission_outcome(&bytes).unwrap() else {
                panic!("must preserve rejection");
            };
            assert_eq!(decoded.category, category);
            assert_eq!(decoded.message, error.message);
            assert_eq!(decoded.details["context"], "preserved");
            assert_eq!(decoded.details["remote_code"], error.code);
            assert_eq!(decoded.anomaly, error.anomaly);
        }
    }

    #[test]
    fn lookup_input_request_and_anomaly_cannot_be_downgraded() {
        let reference = EntityRef::LookupInput {
            attribute: 1_000,
            value: Box::new(TxValue::Tuple(vec![
                Some(TxValue::Entity(EntityRef::Ident(Keyword::new(
                    "target", "one",
                )))),
                None,
            ])),
        };
        let request =
            TransactionRequest::new("lookup", vec![TxOp::RetractEntity(reference.clone())]);
        let bytes =
            encode_submission(&DatabaseIdentity::new("catalog", "lineage"), &request).unwrap();
        let mut body = decode_blob(&bytes, REQUEST).unwrap().to_vec();
        assert_eq!(body[0], 2);
        let (_, decoded) = decode_submission(&bytes).unwrap();
        assert_eq!(
            submission_request_digest(&request.forms, None, None).unwrap(),
            submission_request_digest(&decoded.forms, None, None).unwrap()
        );
        body[0] = 1;
        assert_eq!(
            decode_submission(&encode_blob(REQUEST, &body).unwrap())
                .unwrap_err()
                .code,
            "transport/version"
        );

        let mut error = SemanticError::incorrect("app/reference", "invalid reference");
        error.anomaly = Some(Box::new(RuntimeValue::Vector(vec![RuntimeValue::Entity(
            reference,
        )])));
        let bytes = encode_submission_outcome(&Err(error.clone())).unwrap();
        let mut body = decode_blob(&bytes, RESPONSE).unwrap().to_vec();
        assert_eq!(body[0], 2);
        let WireOutcome::Rejected(decoded) = decode_submission_outcome(&bytes).unwrap() else {
            panic!("expected rejection")
        };
        assert_eq!(decoded.anomaly, error.anomaly);
        body[0] = 1;
        assert!(
            matches!(decode_submission_outcome(&encode_blob(RESPONSE, &body).unwrap()), Err(error) if error.code == "transport/version")
        );
    }

    #[test]
    fn a_maximum_scalar_is_not_rejected_by_its_larger_compound_frame() {
        let identity = DatabaseIdentity::new("name", "lineage");
        let request = TransactionRequest::new(
            "large",
            vec![TxOp::Add {
                entity: EntityRef::Temp("item".into()),
                attribute: 1_000,
                value: Value::String("x".repeat(MAX_VALUE_LEN)).into(),
            }],
        );
        let encoded = encode_submission(&identity, &request).unwrap();
        assert!(encoded.len() > MAX_VALUE_LEN);
        let (_, decoded) = decode_submission(&encoded).unwrap();
        assert_eq!(encode_submission(&identity, &decoded).unwrap(), encoded);
    }
}
