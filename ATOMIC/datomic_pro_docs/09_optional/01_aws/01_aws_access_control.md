---
title: "AWS Access Control"
site: "Datomic"
source: "https://docs.datomic.com/operation/aws-access-control.html"
word_count: 287
---

Datomic peers and transactors requiring access to services provided by AWS (DDB, S3, Cloudwatch) must be authorized to do so. Datomic supports using roles to grant access to specific AWS resources. Use of roles is based on [AWS identity and access management](https://docs.aws.amazon.com/IAM/latest/UserGuide/introduction.html).

> The use of IAM user access keys is deprecated except when used for the initial configuration and deployment steps (e.g. *ensure-transactor* or *create-cf-stack*). Do not use user access keys to provide resource access to peer and transactor processes directly.

## Using IAM Roles

Using IAM roles is the preferred method of granting resource access to Datomic peers and transactors running in EC2. Using roles obviates the need for Datomic processes to store sensitive user credentials in memory, and the need for users to store these credentials in plain text in source code or config files.

EC2 instances can run as an IAM role specified at launch time. Each role can be given a set of policies granting access to specific AWS resources.

### Required Role Policies

The specific role policies required by the transactor and peer library are documented in [setting up storage services](https://docs.datomic.com/overview/storage.html) and [backup and restore](https://docs.datomic.com/operation/backup.html).

## Deprecated: Using IAM User Access Keys

AWS credentials can still be provided as environment variables (AWS\_ACCESS\_KEY\_ID, AWS\_SECRET\_ACCESS\_KEY) or as Java properties (aws.accessKeyId, aws.secretAccessKey). Transactor and peer processes, including backup and restore, will implicitly use credentials provided this way.

Datomic also continues to support passing AWS credentials in URIs as query parameters (e.g. in ddb database and S3 backup URIs), in transactor properties files, and in the S3 URIs passed as arguments to the *bin/backup* and *bin/restore* commands, though these methods are deprecated.

## Migrating from User Access Keys to IAM Roles

Check [migrating to IAM roles](https://docs.datomic.com/legacy/migrate-to-roles.html).