//! Disposable Linux network boundary for executable acceptance. The caller
//! must enter a fresh user+network namespace first; never touch host interfaces.
use std::process::{Child,Command,Stdio};
use std::os::unix::fs::MetadataExt;
use std::time::{Duration,Instant};

fn checked(command:&mut Command) {
    let output=command.output().expect("network fixture command");
    assert!(output.status.success(),"network fixture failed: {}",String::from_utf8_lossy(&output.stderr));
}
pub struct NetworkLab { peer_namespace:Child }
impl NetworkLab {
    pub fn new()->Self {
        assert_eq!(std::env::var("ATOMIC_ISOLATED_NETWORK").as_deref(),Ok("1"),"run via isolated namespace helper");
        assert_ne!(std::fs::metadata("/proc/self/ns/net").unwrap().ino(),std::fs::metadata("/proc/1/ns/net").unwrap().ino(),"refusing to configure the original network namespace");
        // The outer user namespace grants capabilities only within this
        // disposable network hierarchy. No host routes or namespaces change.
        checked(Command::new("ip").args(["link","set","lo","up"]));
        let peer_namespace=Command::new("unshare").args(["--net","sleep","300"])
            .stdin(Stdio::null()).stdout(Stdio::null()).spawn().unwrap();
        let lab=Self{peer_namespace};
        let own=std::fs::metadata("/proc/self/ns/net").unwrap().ino();
        let deadline=Instant::now()+Duration::from_secs(5);
        while std::fs::metadata(format!("/proc/{}/ns/net",lab.peer_namespace.id())).unwrap().ino()==own {
            assert!(Instant::now()<deadline,"peer network namespace did not start");
            std::thread::sleep(Duration::from_millis(5));
        }
        checked(Command::new("ip").args(["link","add","atomic-left","type","veth","peer","name","atomic-right"]));
        checked(Command::new("ip").args(["link","set","atomic-right","netns",&lab.peer_namespace.id().to_string()]));
        checked(Command::new("ip").args(["addr","add","192.0.2.1/30","dev","atomic-left"]));
        checked(Command::new("ip").args(["link","set","atomic-left","up"]));
        checked(lab.peer_command("ip").args(["link","set","lo","up"]));
        checked(lab.peer_command("ip").args(["addr","add","192.0.2.2/30","dev","atomic-right"]));
        checked(lab.peer_command("ip").args(["link","set","atomic-right","up"]));
        lab
    }
    pub fn peer_command(&self,program:impl AsRef<std::ffi::OsStr>)->Command {
        let mut command=Command::new("nsenter");
        command.args(["--target",&self.peer_namespace.id().to_string(),"--net","--"]).arg(program);
        command
    }
}
impl Drop for NetworkLab {
    fn drop(&mut self) {
        let _=self.peer_namespace.kill();let _=self.peer_namespace.wait();
    }
}

/// Reexecute this one test inside an isolated outer network namespace. Missing
/// privileges are a failure, not evidence of a network test that never ran.
pub fn enter(test_name:&str)->bool {
    if std::env::var("ATOMIC_ISOLATED_NETWORK").as_deref()==Ok("1") {return false;}
    let status=Command::new("unshare").args(["--user","--map-root-user","--net"])
        .arg(std::env::current_exe().unwrap()).args(["--exact",test_name,"--nocapture"])
        .env("ATOMIC_ISOLATED_NETWORK","1").status().unwrap();
    assert!(status.success(),"isolated network child failed");true
}
