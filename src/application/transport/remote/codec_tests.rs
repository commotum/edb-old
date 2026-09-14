use super::*;

#[test]
fn endpoint_descriptor_is_bounded_versioned_and_preserves_the_tls_identity() {
    let endpoint = RemoteWriterEndpoint {
        identity: DatabaseIdentity::new(
            "01990000-0000-7000-8000-000000000001",
            "01990000-0000-7000-8000-000000000001",
        ),
        address: "127.0.0.1:4801".parse().unwrap(),
        tls_server_name: "localhost".into(),
        holder_id: "holder".into(),
        lease_epoch: 3,
        instance_id: [7; 32],
    };
    let bytes = encode_endpoint(&endpoint).unwrap();
    assert_eq!(decode_endpoint(&bytes).unwrap(), endpoint);
    for n in [0, 4, bytes.len() - 1] {
        assert!(decode_endpoint(&bytes[..n]).is_err());
    }
    let mut trailing = bytes.clone();
    trailing.push(0);
    assert!(decode_endpoint(&trailing).is_err());
    let mut version = bytes;
    version[4] = 2;
    assert!(decode_endpoint(&version).is_err());
    assert!(decode_endpoint(&vec![0; MAX_ENDPOINT_BYTES + 1]).is_err());
}
