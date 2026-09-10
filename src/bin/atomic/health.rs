//! A deliberately small, bounded status-only HTTP listener. No database I/O,
//! credentials, payloads, administration or query gateway live here.
use atomic_core::{ErrorCategory, SemanticError};
use std::io::{Read, Write};
use std::net::{SocketAddr, TcpListener, TcpStream};
use std::sync::{
    Arc, Mutex,
    atomic::{AtomicBool, AtomicU8, Ordering},
    mpsc,
};
use std::thread::{self, JoinHandle};
use std::time::{Duration, Instant};

#[derive(Clone, Copy)]
pub(super) enum Phase {
    Starting,
    Standby,
    Active,
    Stopping,
    Failed,
}

pub(super) struct HealthServer {
    address: SocketAddr,
    phase: Arc<AtomicU8>,
    stop: Arc<AtomicBool>,
    acceptor: Option<JoinHandle<()>>,
    workers: Vec<JoinHandle<()>>,
}

impl HealthServer {
    pub fn bind(address: SocketAddr) -> Result<Self, SemanticError> {
        let listener = TcpListener::bind(address).map_err(error)?;
        let address = listener.local_addr().map_err(error)?;
        listener.set_nonblocking(true).map_err(error)?;
        let phase = Arc::new(AtomicU8::new(Phase::Starting as u8));
        let stop = Arc::new(AtomicBool::new(false));
        // Two fixed workers, eight admitted sockets, at most1KiB request per
        // worker. Overload closes excess connections; it never spawns threads.
        let (sender, receiver) = mpsc::sync_channel::<TcpStream>(8);
        let receiver = Arc::new(Mutex::new(receiver));
        let mut workers = Vec::new();
        for _ in 0..2 {
            let receiver = receiver.clone();
            let phase = phase.clone();
            let stopping = stop.clone();
            match thread::Builder::new()
                .name("atomic-health".into())
                .spawn(move || {
                    loop {
                        let received =
                            { receiver.lock().unwrap_or_else(|p| p.into_inner()).recv() };
                        let Ok(stream) = received else { break };
                        if stopping.load(Ordering::Acquire) {
                            break;
                        }
                        let _ = respond(stream, &phase);
                    }
                }) {
                Ok(worker) => workers.push(worker),
                Err(e) => {
                    drop(sender);
                    for worker in workers {
                        let _ = worker.join();
                    }
                    return Err(error(e));
                }
            }
        }
        let stopping = stop.clone();
        let acceptor = match thread::Builder::new()
            .name("atomic-health-accept".into())
            .spawn(move || {
                while !stopping.load(Ordering::Acquire) {
                    match listener.accept() {
                        Ok((stream, _)) => {
                            let _ = sender.try_send(stream);
                        }
                        Err(e) if e.kind() == std::io::ErrorKind::WouldBlock => {
                            thread::sleep(Duration::from_millis(10))
                        }
                        Err(e) if e.kind() == std::io::ErrorKind::Interrupted => continue,
                        Err(_) => break,
                    }
                }
            }) {
            Ok(thread) => thread,
            Err(e) => {
                // A failed spawn drops its closure/sender, waking receivers.
                for worker in workers {
                    let _ = worker.join();
                }
                return Err(error(e));
            }
        };
        Ok(Self {
            address,
            phase,
            stop,
            acceptor: Some(acceptor),
            workers,
        })
    }
    pub fn address(&self) -> SocketAddr {
        self.address
    }
    pub fn set(&self, phase: Phase) {
        self.phase.store(phase as u8, Ordering::Release);
    }
    pub fn is_available(&self) -> bool {
        self.acceptor
            .as_ref()
            .is_some_and(|thread| !thread.is_finished())
            && self.workers.iter().all(|thread| !thread.is_finished())
    }
}
impl Drop for HealthServer {
    fn drop(&mut self) {
        self.set(Phase::Stopping);
        self.stop.store(true, Ordering::Release);
        if let Some(thread) = self.acceptor.take() {
            let _ = thread.join();
        }
        for thread in self.workers.drain(..) {
            let _ = thread.join();
        }
    }
}

fn respond(mut stream: TcpStream, phase: &AtomicU8) -> std::io::Result<()> {
    let deadline = Instant::now() + Duration::from_millis(250);
    let mut bytes = [0; 1024];
    let mut used = 0;
    while !bytes[..used].windows(4).any(|w| w == b"\r\n\r\n") {
        if used == bytes.len() {
            return reply(&mut stream, 431, "request too large\n", false);
        }
        let remaining = deadline
            .checked_duration_since(Instant::now())
            .filter(|d| !d.is_zero())
            .ok_or_else(|| {
                std::io::Error::new(std::io::ErrorKind::TimedOut, "health request deadline")
            })?;
        stream.set_read_timeout(Some(remaining))?;
        let read = stream.read(&mut bytes[used..])?;
        if read == 0 {
            return Ok(());
        }
        used += read;
    }
    let Some(line) = bytes[..used]
        .split(|byte| *byte == b'\n')
        .next()
        .and_then(|line| std::str::from_utf8(line).ok())
    else {
        return reply(&mut stream, 400, "bad request\n", false);
    };
    let mut parts = line.trim_end_matches('\r').split(' ');
    let method = parts.next().unwrap_or("");
    let path = parts.next().unwrap_or("");
    if !matches!(parts.next(), Some("HTTP/1.0" | "HTTP/1.1")) || parts.next().is_some() {
        return reply(&mut stream, 400, "bad request\n", false);
    }
    if !matches!(method, "GET" | "HEAD") {
        return reply(&mut stream, 405, "method not allowed\n", false);
    }
    let current = phase.load(Ordering::Acquire);
    let (live, ready, state) = match current {
        n if n == Phase::Starting as u8 => (true, false, "starting"),
        n if n == Phase::Standby as u8 => (true, false, "standby"),
        n if n == Phase::Active as u8 => (true, true, "active"),
        n if n == Phase::Stopping as u8 => (false, false, "stopping"),
        _ => (false, false, "failed"),
    };
    let available = match path {
        "/health" => live,
        "/ready" => ready,
        _ => return reply(&mut stream, 404, "not found\n", method == "HEAD"),
    };
    let body = format!("{{\"live\":{live},\"ready\":{ready},\"state\":\"{state}\"}}\n");
    reply(
        &mut stream,
        if available { 200 } else { 503 },
        &body,
        method == "HEAD",
    )
}
fn reply(stream: &mut TcpStream, status: u16, body: &str, head: bool) -> std::io::Result<()> {
    stream.set_write_timeout(Some(Duration::from_millis(100)))?;
    let reason = match status {
        200 => "OK",
        503 => "Service Unavailable",
        404 => "Not Found",
        405 => "Method Not Allowed",
        431 => "Request Header Fields Too Large",
        _ => "Bad Request",
    };
    write!(
        stream,
        "HTTP/1.1 {status} {reason}\r\nConnection: close\r\nCache-Control: no-store\r\nContent-Type: {}\r\nContent-Length: {}\r\n{}\r\n",
        if matches!(status, 200 | 503) {
            "application/json"
        } else {
            "text/plain"
        },
        body.len(),
        if status == 405 {
            "Allow: GET, HEAD\r\n"
        } else {
            ""
        }
    )?;
    if !head {
        stream.write_all(body.as_bytes())?;
    }
    Ok(())
}
fn error(_: std::io::Error) -> SemanticError {
    SemanticError::new(
        ErrorCategory::Unavailable,
        "cli/health-listener",
        "health listener could not start",
    )
}

#[cfg(test)]
mod tests {
    use super::*;
    use std::net::Shutdown;

    fn start() -> HealthServer {
        HealthServer::bind("127.0.0.1:0".parse().unwrap()).unwrap()
    }

    fn request(address: SocketAddr, bytes: &[u8]) -> (String, String) {
        let mut client = TcpStream::connect_timeout(&address, Duration::from_secs(2)).unwrap();
        client
            .set_read_timeout(Some(Duration::from_secs(2)))
            .unwrap();
        client
            .set_write_timeout(Some(Duration::from_secs(2)))
            .unwrap();
        client.write_all(bytes).unwrap();
        client.shutdown(Shutdown::Write).unwrap();
        let mut response = String::new();
        client.read_to_string(&mut response).unwrap();
        let (header, body) = response
            .split_once("\r\n\r\n")
            .expect("complete HTTP response");
        (header.to_owned(), body.to_owned())
    }

    #[test]
    fn get_and_head_distinguish_liveness_readiness_and_all_lifecycle_phases() {
        let server = start();
        for (phase, state, live, ready) in [
            (Phase::Starting, "starting", true, false),
            (Phase::Standby, "standby", true, false),
            (Phase::Active, "active", true, true),
            (Phase::Stopping, "stopping", false, false),
            (Phase::Failed, "failed", false, false),
        ] {
            server.set(phase);
            for (path, available) in [("/health", live), ("/ready", ready)] {
                let body = format!("{{\"live\":{live},\"ready\":{ready},\"state\":\"{state}\"}}\n");
                for method in ["GET", "HEAD"] {
                    let (header, actual_body) = request(
                        server.address(),
                        format!("{method} {path} HTTP/1.1\r\nHost: localhost\r\n\r\n").as_bytes(),
                    );
                    assert!(header.starts_with(if available {
                        "HTTP/1.1 200 "
                    } else {
                        "HTTP/1.1 503 "
                    }));
                    assert!(header.contains("Connection: close\r\n"));
                    assert!(header.contains("Cache-Control: no-store\r\n"));
                    assert!(header.contains("Content-Type: application/json\r\n"));
                    assert!(
                        header
                            .lines()
                            .any(|line| line == format!("Content-Length: {}", body.len()))
                    );
                    assert_eq!(actual_body, if method == "GET" { &body } else { "" });
                }
            }
        }
        assert!(
            server.is_available(),
            "failed application phase is not a dead listener"
        );
    }

    #[test]
    fn malformed_methods_paths_and_oversized_requests_are_bounded_and_do_not_poison_listener() {
        let server = start();
        for invalid in [
            b"GET /health\r\n\r\n".as_slice(),
            b"GET /health HTTP/2.0\r\n\r\n".as_slice(),
            b"GET /health HTTP/1.1 extra\r\n\r\n".as_slice(),
            b"\xff /health HTTP/1.1\r\n\r\n".as_slice(),
        ] {
            let (header, body) = request(server.address(), invalid);
            assert!(header.starts_with("HTTP/1.1 400 "));
            assert_eq!(body, "bad request\n");
        }
        let (header, body) = request(server.address(), b"POST /health HTTP/1.1\r\n\r\n");
        assert!(header.starts_with("HTTP/1.1 405 "));
        assert!(header.contains("Allow: GET, HEAD"));
        assert_eq!(body, "method not allowed\n");
        let (header, body) = request(server.address(), b"HEAD /not-a-probe HTTP/1.0\r\n\r\n");
        assert!(header.starts_with("HTTP/1.1 404 "));
        assert!(body.is_empty());
        let (header, body) = request(server.address(), &[b'x'; 1024]);
        assert!(header.starts_with("HTTP/1.1 431 "));
        assert_eq!(body, "request too large\n");
        server.set(Phase::Active);
        assert!(
            request(server.address(), b"GET /ready HTTP/1.0\r\n\r\n")
                .0
                .starts_with("HTTP/1.1 200 ")
        );
        assert!(server.is_available());
    }

    #[test]
    fn stalled_and_excess_connections_use_fixed_workers_and_shutdown_releases_every_socket() {
        let server = start();
        server.set(Phase::Active);
        assert_eq!(server.workers.len(), 2);
        let mut clients = Vec::new();
        for _ in 0..2 {
            clients.push(TcpStream::connect(server.address()).unwrap());
        }
        let started = Instant::now();
        assert!(
            request(server.address(), b"GET /health HTTP/1.1\r\n\r\n")
                .0
                .starts_with("HTTP/1.1 200 ")
        );
        assert!(
            started.elapsed() < Duration::from_secs(2),
            "stalled readers must yield to their read deadline"
        );
        for _ in 0..24 {
            clients.push(TcpStream::connect(server.address()).unwrap());
        }
        for client in &clients[2..] {
            client.set_nonblocking(true).unwrap();
        }
        let saturated_by = Instant::now() + Duration::from_secs(1);
        loop {
            let still_stalled = clients[2..4].iter().all(|client| {
                matches!(client.peek(&mut [0]), Err(error) if error.kind() == std::io::ErrorKind::WouldBlock)
            });
            let shed_excess = clients[12..]
                .iter()
                .any(|client| matches!(client.peek(&mut [0]), Ok(0)));
            if still_stalled && shed_excess {
                break;
            }
            assert!(
                Instant::now() < saturated_by,
                "did not observe admitted stalled sockets and overload shedding together"
            );
            thread::sleep(Duration::from_millis(1));
        }
        assert_eq!(
            server.workers.len(),
            2,
            "overload cannot spawn per-connection workers"
        );
        let (finished, completion) = mpsc::sync_channel(1);
        let cleanup = thread::spawn(move || {
            let started = Instant::now();
            drop(server);
            let _ = finished.send(started.elapsed());
        });
        let result = completion.recv_timeout(Duration::from_secs(2));
        // On a broken deadline, closing clients still releases the test-owned
        // cleanup thread before reporting failure rather than hanging the suite.
        if result.is_err() {
            drop(clients);
            cleanup.join().unwrap();
            panic!("health shutdown did not finish while clients remained stalled");
        }
        eprintln!("complete saturated health shutdown {:?}", result.unwrap());
        cleanup.join().unwrap();
        for mut client in clients {
            client.set_nonblocking(false).unwrap();
            client
                .set_read_timeout(Some(Duration::from_secs(1)))
                .unwrap();
            let mut byte = [0];
            match client.read(&mut byte) {
                Ok(0) => {}
                Err(error)
                    if matches!(
                        error.kind(),
                        std::io::ErrorKind::ConnectionReset | std::io::ErrorKind::ConnectionAborted
                    ) => {}
                other => panic!("health socket remained live after shutdown: {other:?}"),
            }
        }
    }
}
