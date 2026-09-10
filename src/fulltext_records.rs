//! Logical payloads for the generic immutable fulltext key/value tree. Posting
//! construction streams tokens into the external sorter; duplicate occurrence
//! keys coalesce there. Text is stored once per historical assertion, not once
//! per posting. Candidate TF/phrase positions are derived from that document.
use crate::fulltext_analysis::next_token;
use crate::fulltext_store::FulltextRecord;
use crate::{
    DatabaseValue, DatabaseValuePrefixCursor, Datom, ErrorCategory, SemanticError, Value, sha256,
};
use std::collections::VecDeque;

pub(super) type DocId = [u8; 48];
pub(super) fn doc_id(datom: &Datom) -> Result<DocId, SemanticError> {
    let Value::String(text) = &datom.value else {
        return Err(corrupt("document is not a string"));
    };
    let mut id = [0; 48];
    id[..8].copy_from_slice(&datom.entity.to_be_bytes());
    id[8..16].copy_from_slice(&datom.tx.to_be_bytes());
    id[16..].copy_from_slice(&sha256(text.as_bytes()));
    Ok(id)
}
pub(super) fn doc_key(attribute: u32, id: &DocId) -> Vec<u8> {
    let mut key = attribute.to_be_bytes().to_vec();
    key.push(0);
    key.extend_from_slice(id);
    key
}
pub(super) fn term_prefix(attribute: u32, term: &str, exact: bool) -> Vec<u8> {
    let mut key = attribute.to_be_bytes().to_vec();
    key.push(1);
    key.extend_from_slice(term.as_bytes());
    if exact {
        key.push(0);
    }
    key
}
pub(super) fn stats_key(attribute: u32) -> Vec<u8> {
    let mut key = attribute.to_be_bytes().to_vec();
    key.push(2);
    key
}
pub(super) fn posting(
    record: &FulltextRecord,
    attribute: u32,
) -> Result<(String, DocId, u32), SemanticError> {
    let key = &record.key;
    if key.len() < 55
        || key[..4] != attribute.to_be_bytes()
        || key[4] != 1
        || key[key.len() - 49] != 0
        || record.value.len() != 4
    {
        return Err(corrupt("invalid posting record"));
    }
    let term =
        std::str::from_utf8(&key[5..key.len() - 49]).map_err(|_| corrupt("invalid term UTF-8"))?;
    if term.is_empty() || term.contains('\0') {
        return Err(corrupt("invalid term key"));
    }
    Ok((
        term.to_owned(),
        key[key.len() - 48..].try_into().unwrap(),
        u32::from_be_bytes(record.value[..].try_into().unwrap()),
    ))
}
pub(super) fn document(
    record: FulltextRecord,
    attribute: u32,
    id: &DocId,
) -> Result<(Datom, u32), SemanticError> {
    if record.key != doc_key(attribute, id) || record.value.len() < 4 {
        return Err(corrupt("invalid document record"));
    }
    let length = u32::from_be_bytes(record.value[..4].try_into().unwrap());
    let text =
        std::str::from_utf8(&record.value[4..]).map_err(|_| corrupt("invalid document UTF-8"))?;
    if sha256(text.as_bytes()) != id[16..] {
        return Err(corrupt("document text identity mismatch"));
    }
    let entity = u64::from_be_bytes(id[..8].try_into().unwrap());
    let tx = u64::from_be_bytes(id[8..16].try_into().unwrap());
    crate::eid_to_eidx(entity)?;
    crate::tx_to_t(tx)?;
    Ok((
        Datom {
            entity,
            attribute,
            tx,
            value: Value::String(text.into()),
            added: true,
        },
        length,
    ))
}
pub(super) fn statistics(
    record: &FulltextRecord,
    attribute: u32,
) -> Result<(u64, u64), SemanticError> {
    if record.key != stats_key(attribute) || record.value.len() != 16 {
        return Err(corrupt("invalid corpus statistics"));
    }
    Ok((
        u64::from_be_bytes(record.value[..8].try_into().unwrap()),
        u64::from_be_bytes(record.value[8..].try_into().unwrap()),
    ))
}
fn corrupt(message: &str) -> SemanticError {
    SemanticError::new(ErrorCategory::Fault, "fulltext/invalid-record", message)
}

struct PendingDocument {
    attribute: u32,
    id: DocId,
    text: String,
    length: u32,
    offset: usize,
    position: u32,
}
struct Records<'a> {
    db: &'a DatabaseValue,
    attributes: VecDeque<u32>,
    active: Option<(u32, DatabaseValuePrefixCursor<'a>)>,
    document: Option<PendingDocument>,
    documents: u64,
    total_length: u64,
    failed: bool,
}

impl Records<'_> {
    fn next_record(&mut self) -> Result<Option<FulltextRecord>, SemanticError> {
        loop {
            if let Some(doc) = &mut self.document {
                if let Some(token) = next_token(&doc.text, &mut doc.offset, &mut doc.position) {
                    let mut key = term_prefix(doc.attribute, &token.term, true);
                    key.extend_from_slice(&doc.id);
                    return Ok(Some(FulltextRecord {
                        key,
                        value: doc.length.to_be_bytes().to_vec(),
                    }));
                }
                self.document = None;
            }
            if let Some((attribute, cursor)) = &mut self.active {
                if let Some(datom) = cursor.next() {
                    let datom = datom?;
                    if !datom.added {
                        continue;
                    }
                    let id = doc_id(&datom)?;
                    let Value::String(text) = datom.value else {
                        unreachable!()
                    };
                    let (mut offset, mut position) = (0, 0);
                    let length =
                        std::iter::from_fn(|| next_token(&text, &mut offset, &mut position))
                            .count();
                    let length = u32::try_from(length)
                        .map_err(|_| corrupt("document token count overflow"))?;
                    self.documents = self
                        .documents
                        .checked_add(1)
                        .ok_or_else(|| corrupt("corpus document count overflow"))?;
                    self.total_length = self
                        .total_length
                        .checked_add(u64::from(length))
                        .ok_or_else(|| corrupt("corpus token count overflow"))?;
                    let mut value = length.to_be_bytes().to_vec();
                    value.extend_from_slice(text.as_bytes());
                    let key = doc_key(*attribute, &id);
                    self.document = Some(PendingDocument {
                        attribute: *attribute,
                        id,
                        text,
                        length,
                        offset: 0,
                        position: 0,
                    });
                    return Ok(Some(FulltextRecord { key, value }));
                }
                let key = stats_key(*attribute);
                let mut value = self.documents.to_be_bytes().to_vec();
                value.extend_from_slice(&self.total_length.to_be_bytes());
                self.active = None;
                self.documents = 0;
                self.total_length = 0;
                return Ok(Some(FulltextRecord { key, value }));
            }
            let Some(attribute) = self.attributes.pop_front() else {
                return Ok(None);
            };
            self.active = Some((attribute, self.db.fulltext_history_cursor(attribute)?));
        }
    }
}
impl Iterator for Records<'_> {
    type Item = Result<FulltextRecord, SemanticError>;
    fn next(&mut self) -> Option<Self::Item> {
        if self.failed {
            return None;
        }
        match self.next_record() {
            Ok(Some(record)) => Some(Ok(record)),
            Ok(None) => None,
            Err(error) => {
                self.failed = true;
                Some(Err(error))
            }
        }
    }
}

/// Caller owns an authenticated, unfiltered native source and retains its pin
/// throughout building. No full database or expanded posting list is collected.
pub(crate) fn fulltext_records(
    db: &DatabaseValue,
) -> Result<Box<dyn Iterator<Item = Result<FulltextRecord, SemanticError>> + '_>, SemanticError> {
    Ok(Box::new(Records {
        db,
        attributes: db
            .schema()
            .attributes()
            .filter(|a| a.fulltext)
            .map(|a| a.id)
            .collect(),
        active: None,
        document: None,
        documents: 0,
        total_length: 0,
        failed: false,
    }))
}
