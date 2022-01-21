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
* `web` : handles the user interface with the customer, i.e., the web pages;
* `auth` : manages the user (customer) accounts and cart;
* `image` : resizes the product images to the most appropriate resolution according to the device used to browse the shop;
* `persistence` : interacts with the database to retrieve information needed by other microservices;
* `recommender` : analyses the user profile and history to suggest products to buy.

Tms directory contains the implementation of the TMS case study, used in [this](https://www.mdpi.com/2076-3417/10/21/7800) paper. It contains four microservices: 
* `cms` : handles the user interface and provides access to the internal functions;
* `ems` : handles the examination templates;
* `qms` : manages the questions repository;
* `ums` : manages the user (examinees) accounts.

### MakeModelTraces, WhatifEngine, LqnxSim, and LQNExecutorModel

These four directories provide supporting tools that allow the performance prediction part of the μP Framework.

MakeModelTraces contains a tool that generates the LQN model of a MSA by parsing the running example log. It also computes the average user response time observed in the log, together with the microservice utilization.

WhatifEngine contains the whatif tool: given a system model as obtained by MakeModelTraces and the description of the proposed modifications (i.e., the what-if), it produces a new model that represents the new system.

LqnxSim contains the model simulator, to obtain the expected response time and microservice utilization of the system described in the model file (produced either by MakeModelTraces or WhatifEngine). The LQNExecutorModel directory contains the description of the model file structure.
