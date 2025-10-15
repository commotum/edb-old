use std::collections::HashMap;

use crate::model::TempId;

pub trait EntidAllocator {
    fn allocate(&mut self) -> i64;
}

pub struct SimpleAllocator {
    next: i64,
}

impl SimpleAllocator {
    pub fn new(start: i64) -> Self { Self { next: start } }
}

impl EntidAllocator for SimpleAllocator {
    fn allocate(&mut self) -> i64 { let e = self.next; self.next += 1; e }
}

pub struct TempResolver {
    pub map: HashMap<TempId, i64>,
}

impl TempResolver {
    pub fn new() -> Self { Self { map: HashMap::new() } }
    pub fn resolve_or_alloc<A: EntidAllocator + ?Sized>(&mut self, t: &TempId, alloc: &mut A) -> i64 {
        if let Some(e) = self.map.get(t) { *e } else { let e = alloc.allocate(); self.map.insert(t.clone(), e); e }
    }
    pub fn bind(&mut self, t: TempId, e: i64) { self.map.insert(t, e); }
}
