---
title: "Hosting a Private Maven Repository"
site: "Datomic"
source: "https://docs.datomic.com/tech-notes/hosting-private-maven.html"
description: "Learn how to host a Maven repository for Datomic with this guide. Get step-by-step instructions and best practices for secure and efficient hosting."
word_count: 611
---

## Overview

[My Datomic](https://my.datomic.com/) provides a [Maven](https://maven.apache.org/) repository for acquiring the datomic-pro dependency. You may wish to host your own *private* Maven repository to maintain control over the availability of the dependencies and to take ownership of the availability of the artifacts.

A Maven repository can be as simple as remotely accessible storage with your dependencies, [pom(s)](https://maven.apache.org/guides/introduction/introduction-to-the-pom.html) and checksums of those files in the correct directory. This document will describe how to gather the necessary files and where to put them. Choosing a storage and managing access to that storage is not discussed and is the responsibility of the user. Storage permissions must be restricted to licensees and breaches of this boundary may result in termination of your license.

> Datomic may be redistributed after version [1.0.6711](https://docs.datomic.com/changes/pro.html). Familiarize yourself [with the Apache 2.0 agreement](https://www.apache.org/licenses/LICENSE-2.0.html) before proceeding with this guide. These instructions are solely for the purpose of internal and private distribution of the dependencies.

## Gathering Dependencies

You will need to gather the dependencies, their `pom` and then generate checksums for the `jar` and `pom`.

In the instructions below either replace `$THIS` with the correct information or export that to an environment variable before continuing with the instructions.

The instructions are listed here to give an overview of what needs to be done for this process. These instructions can be integrated into your environment through the automation systems that you have in place for consuming new versions of Datomic.

- Login to [My Datomic](https://my.datomic.com/).
	- Note the e-mail address used to login to your account.
		- Note the `Download Key` under "License information".
- Download the datomic-pro dependency. Instructions for this are listed on the [My Datomic](https://my.datomic.com/) account page.
	```
	wget --http-user=your@email.com --http-password=$YOUR_DOWNLOAD_KEY https://my.datomic.com/repo/com/datomic/datomic-pro/$VERSION/datomic-pro-$VERSION.zip -O datomic-pro-$VERSION.zip
	```
- Substitute `$YOUR_DOWNLOAD_KEY` and `$VERSION` with the appropriate values, or export those to your environment and assign them the appropriate value.
- Unzip `datomic-pro-$VERSION.zip` in your preferred location.
- Navigate to that path in your shell.
- Download `memcache-asg-java-client` artifacts. Then:
	- Make a directory for the artifacts: `mkdir memcache-asg-java-client`.
		- Note the latest version of the memcache-asg-java-client in the [release notes](https://docs.datomic.com/releases.html) for your version of Datomic. You may need to look at previous versions to check when the last update was to this dependency.
		- From the path where datomic-pro-$VERSION.zip was unzipped to: download the `memcache-asg-java-client-$MAJC_VERSION.jar`:
		```
		wget --http-user=your@email.com --http-password=$YOUR_DOWNLOAD_KEY https://my.datomic.com/repo/com/datomic/memcache-asg-java-client/$MAJC_VERSION/memcache-asg-java-client-$MAJC-VERSION.jar -O memcache-asg-java-client/memcache-asg-java-client-$MAJC_VERSION.jar
		```
		- From the path where datomic-pro-$VERSION.zip was unzipped to: download the `pom` to `memcache-asg-java-client-$MAJC_VERSION.pom`:
		```
		wget --http-user=your@email.com --http-password=$YOUR_DOWNLOAD_KEY https://my.datomic.com/repo/com/datomic/memcache-asg-java-client/$MAJC_VERSION/memcache-asg-java-client-$MAJC-VERSION.pom -O memcache-asg-java-client/memcache-asg-java-client-$MAJC-VERSION.pom
		```
- Generate the files that will be deployed to your storage.
	- Make the repository directory: `mkdir repo`.
		- Install datomic-pro to the above local repo. Replace $VERSION with your version if you're not using an environment variable for $VERSION:
		```
		mvn install:install-file -DgroupId=com.datomic -DartifactId=datomic-pro -Dfile=datomic-pro-$VERSION.jar -DpomFile=pom.xml -DlocalRepositoryPath=$(pwd)/repo -DcreateChecksum=true~
		```
		- Install memcache-asg-java-client to the above local repo. Replace $VERSION with your version if you're not using an environment variable for $VERSION:
		```
		mvn install:install-file -DgroupId=com.datomic -DartifactId=memcache-asg-java-client -Dfile=memcache-asg-java-client/memcache-asg-java-client-$MAJC_VERSION.jar -DpomFile=memcache-asg-java-client/memcache-asg-java-client-$MAJC_VERSION.pom -DlocalRepositoryPath=$(pwd)/repo -DcreateChecksum=true~
		```

You should now have a `/repo` directory with the necessary `datomic-pro` and `memcache-asg-java-client` files to act as a Maven repository for Datomic Pro.

## Deploy

Deploying your private Maven repository only requires that you copy the `repo` directory you just created to your preferred restricted remote storage.

If you use a dependency caching system then you may be required to have a Maven index. [Follow the directions for the Maven Indexer](https://maven.apache.org/maven-indexer/indexer-cli/index.html) and deploy this to the root of your Maven repository.

## Usage

Once your storage has your `/repo` directory copied to it, access is limited and you're able to access the storage from trusted environments then you can use the repository.

Set up your dependency management to use your storage location as a Maven repository. An example for [tools.deps](https://clojure.org/guides/deps_and_cli):

```
{:mvn/repos {"my.private.repo" {:url "protocol://path/to/storage"}}
 :deps {com.datomic/datomic-pro {:mvn/version "${VERSION}"}}}
```