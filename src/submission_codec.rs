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
    encode_blob(REQUEST, &body)
}

pub(crate) fn decode_submission(
    bytes: &[u8],
) -> Result<(DatabaseIdentity, TransactionRequest), SemanticError> {
    let mut cursor = versioned(bytes, REQUEST)?;
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

fn versioned(bytes: &[u8], kind: u8) -> Result<Cursor<'_>, SemanticError> {
    let mut cursor = Cursor::new(decode_blob(bytes, kind)?);
    if cursor.u8()? != VERSION {
        return Err(fault(
            "transport/version",
            "unsupported native transport version",
        ));
    }
    Ok(cursor)
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
        tag => return Err(invalid_tag("transaction operation", tag)),
    })
}

fn decode_tx_value(cursor: &mut Cursor<'_>) -> Result<TxValue, SemanticError> {
    Ok(match cursor.u8()? {
        0 => TxValue::Scalar(decode_value(cursor, 0)?),
        1 => TxValue::Entity(decode_entity_ref(cursor)?),
        tag => return Err(invalid_tag("transaction value", tag)),
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
    let mut cursor = versioned(bytes, RESPONSE)?;
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
