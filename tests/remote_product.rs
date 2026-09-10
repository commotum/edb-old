#![cfg(target_os="linux")]
mod common;
#[path="common/network.rs"] mod network;
use common::product_support::*;
use std::path::{Path,PathBuf};
use std::process::Command;
use std::os::unix::fs::PermissionsExt;
use std::time::{Duration,Instant};

struct Credentials {
    _directory:tempfile::TempDir,
    certificate:PathBuf,
    key:PathBuf,
    token:PathBuf,
    wrong_token:PathBuf,
}
impl Credentials {
    fn new()->Self {
        let directory=tempfile::tempdir().unwrap();
        let certificate=directory.path().join("certificate.pem");
        let key=directory.path().join("private.pem");
        let token=directory.path().join("token.hex");
        let wrong_token=directory.path().join("wrong-token.hex");
        success(Command::new("openssl").args(["req","-x509","-newkey","rsa:2048","-sha256","-days","1","-nodes","-subj","/CN=atomic.test","-addext","subjectAltName=DNS:atomic.test","-keyout"])
            .arg(&key).arg("-out").arg(&certificate).output().unwrap());
        for path in [&token,&wrong_token] {
            success(Command::new("openssl").args(["rand","-hex","-out"]).arg(path).arg("32").output().unwrap());
            std::fs::set_permissions(path,std::fs::Permissions::from_mode(0o600)).unwrap();
        }
        std::fs::set_permissions(&key,std::fs::Permissions::from_mode(0o600)).unwrap();
        Self{_directory:directory,certificate,key,token,wrong_token}
    }
    fn client(&self,command:&mut Command) {
        command.env("ATOMIC_REMOTE_TLS_ROOT",&self.certificate).env("ATOMIC_REMOTE_TOKEN_FILE",&self.token);
    }
    fn server(&self,command:&mut Command) {
        command.env("ATOMIC_REMOTE_TLS_CERT",&self.certificate).env("ATOMIC_REMOTE_TLS_KEY",&self.key)
            .env("ATOMIC_REMOTE_TOKEN_FILE",&self.token);
    }
}
fn writer(fixture:&Fixture,credentials:&Credentials)->Server {
    let mut command=atomic();configured(&mut command,&fixture.writer_url);credentials.server(&mut command);
    command.args(["transactor","--database","remote-application","--listen","192.0.2.1:0","--advertise","192.0.2.1:0","--tls-server-name","atomic.test","--index-threshold-bytes","1","--lease-ms","3000","--renew-ms","500"]);
    Server::spawn(command)
}
fn app(lab:&network::NetworkLab,fixture:&Fixture,credentials:&Credentials)->Command {
    let mut command=lab.peer_command(application_binary());
    configured(&mut command,&fixture.peer_url);credentials.client(&mut command);
    command.args(["--database","remote-application"]);command
}
fn reference(lab:&network::NetworkLab,fixture:&Fixture,credentials:&Credentials,path:&Path)->String {
    success(app(lab,fixture,credentials).arg("--reference-in").arg(path).output().unwrap())
}

#[test]
fn isolated_network_application_retry_replacement_and_reference_handoff() {
    let Ok(url)=std::env::var("ATOMIC_POSTGRES_URL") else {
        eprintln!("SKIPPED real isolated-network application: ATOMIC_POSTGRES_URL unset");return;
    };
    if network::enter("isolated_network_application_retry_replacement_and_reference_handoff") {return;}
    let lab=network::NetworkLab::new();
    let fixture=Fixture::new(&url);
    let (writer_role,peer_role)=fixture.roles.as_ref().expect("network security witness requires disposable restricted roles");
    cli(&fixture.admin_url,&["migrate","--writer-role",writer_role,"--peer-role",peer_role]);
    cli(&fixture.admin_url,&["create","--database","remote-application"]);
    let credentials=Credentials::new();
    let directory=tempfile::tempdir().unwrap();
    let reference_path=directory.path().join("snapshot.reference");
    let mut server=writer(&fixture,&credentials);
    let started=Instant::now();
    let output=success(app(&lab,&fixture,&credentials).args(["--remote","--reference-out"]).arg(&reference_path).output().unwrap());
    for marker in ["APPLICATION_OK","PLANNING_OK","PARTITIONS_OK","FULLTEXT_OK","REFERENCE_WRITTEN"] {
        assert!(output.contains(marker),"missing {marker}: {output}");
    }
    assert!(output.contains("basis_t=11"));
    assert!(output.contains("QUERY_SQL sql_calls=0"));
    assert!(reference(&lab,&fixture,&credentials,&reference_path).contains("REFERENCE_OK basis_t=2"));
    let bad=app(&lab,&fixture,&credentials).env("ATOMIC_REMOTE_TOKEN_FILE",&credentials.wrong_token).arg("--remote").output().unwrap();
    assert!(!bad.status.success(),"wrong token admitted application transactions");
    let unauthorized=app(&lab,&fixture,&credentials).env("ATOMIC_POSTGRES_URL",parameter(&fixture.peer_url,"user","atomic-no-such-fixture-role"))
        .arg("--reference-in").arg(&reference_path).output().unwrap();
    assert!(!unauthorized.status.success(),"reference was treated as an access capability");
    let process_before=server.child.id();
    server.child.kill().unwrap();server.child.wait().unwrap();drop(server);
    // Wait beyond the deliberately short lease; no authoritative row is edited
    // to manufacture takeover. Discovery of the replacement uses a new port.
    std::thread::sleep(Duration::from_millis(3300));
    let mut replacement=writer(&fixture,&credentials);
    assert_ne!(process_before,replacement.child.id());
    let replay=success(app(&lab,&fixture,&credentials).arg("--remote").output().unwrap());
    assert!(replay.contains("seed_replayed=true") && replay.contains("FULLTEXT_OK"));
    assert!(reference(&lab,&fixture,&credentials,&reference_path).contains("exact_key=true"));
    replacement.stop();
    // Snapshot opening and calculation are independent of transactor uptime.
    assert!(reference(&lab,&fixture,&credentials,&reference_path).contains("hours=8"));
    println!("REMOTE_PRODUCT_OK isolated_networks=2 veth=true separate_application=true verified_tls=true restricted_roles=true invalid_token_rejected=true reference_authorized=true original_basis=2 later_basis=11 writer_crash_replacement=true exact_retry=true total_ms={}",started.elapsed().as_millis());
}
