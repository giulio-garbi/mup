# μP: A Development Framework for Accurate Performance Predictions in Microservices Systems

This repository contains the source code of a μP Framework implementation in Java together with four case studies taken from the microservices literature. The repository is organized as a [Maven](https://maven.apache.org/) with submodules.

## Organization of this repository

This section describes the components of the repository's root directory. It contains three files: 
* .gitignore : used to avoid pushing to the repository the files generated from the build phase.
* README.md : this file
* pom.xml : POM module used to build the whole repository, including all the case studies and the supporting tools.

The following subsections present the content of the directories, each being one module of the main pom.xml file. We group them byfunctional groups.

### MicroservicesFramework

This directory contains the source code of the framework. Developers can download this module and add it as a dependency in the pom.xml's `dependencies` node as follows:
```
<dependency>
    <groupId>MicroservicesFramework</groupId>
    <artifactId>MicroservicesFramework</artifactId>
</dependency>
```
We remark that the actual API of this implementation slightly differs from the one outlined in the paper, the latter being a simplified version for presentation reasons.

### AcmeAir, JPetStore, TeaStore and Tms

These four directories correspond to the case studies used in the experimental section.

AcmeAir directory contains the implementation of the [AcmeAir](https://github.com/acmeair/acmeair) case study. It contains two microservices: 
* `main` : handles the principal function of the website, i.e., the web pages, flight search and booking, check-in;
* `auth` : manages the user (customer) accounts.

JPetStore directory contains the implementation of the [JPetStore](https://oceanrep.geomar.de/46706/) case study. It contains four microservices: 
* `frontend` : handles the user interface with the customer, i.e., the web pages;
* `account` : manages the user (customer) accounts;
* `catalog` : manages the pet catalog of the shop, i.e., product description, availability, and price;
* `cart` : handles the online cart functionality, used when the customer browses the catalog and chooses to buy pets.

TeaStore directory contains the implementation of the [TeaStore](https://ieeexplore.ieee.org/abstract/document/8526888) case study. It contains five microservices: 
* `web`
`image`, `auth`, , `persistence`, and `cart`.

#### TMS
