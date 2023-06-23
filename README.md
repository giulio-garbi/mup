# μP: A Development Framework for Performance Predictable Microservices

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

### MakeModelTraces, WhatifEngine, and LqnxSim

These four directories provide supporting tools that allow the performance prediction part of the μP Framework.

MakeModelTraces contains a tool that generates the LQN model of a MSA by parsing the running example log. It also computes the average user response time observed in the log, together with the microservice utilization.

WhatifEngine contains the whatif tool: given a system model as obtained by MakeModelTraces and the description of the proposed modifications (i.e., the what-if), it produces a new model that represents the new system.

LqnxSim contains the model simulator, to obtain the expected response time and microservice utilization of the system described in the model file (produced either by MakeModelTraces or WhatifEngine).

## Setup

### Prerequisites

* Java JVM (at least v. 16)
* [Maven](https://maven.apache.org/) (at least v. 3.8.4)
* [MongoDB](https://www.mongodb.com) (at least v. 5.0.1)

### Build and installation instruction

1. Clone this repository: `git clone https://github.com/giulio-garbi/mup`.
2. Move into the `mup` directory: `cd mup`.
3. Build the Maven module: `mvn install`.
4. Move into the built objects directory (`bin`): `cd bin`.
5. Install the databases (needed to run the case studies): `./setupDb.sh`.

## Usage

The Maven module generates seven .jar archives, one for each case study or tool, together with their dependencies. 

### Case studies

Each case study corresponds to a runnable .jar archive. When launched, they run an execution of the case study lasting 600 seconds (after a discarded 50 second warmup phase) with the specified arguments, and then save the MSA log. AcmeAir, JPetStore, TeaStore and TMS correspond, respectively, to `aair.jar`, `jps.jar`, `ts.jar`, and `tms.jar`.

The .jar archives accept the following parameters:
* `java -jar aair.jar <log.json> <n_clients> <tp_main> <rep_main> <tp_auth> <rep_auth>`
* `java -jar jps.jar <log.json> <n_clients> <tp_account> <rep_account> <tp_cart> <rep_cart><tp_catalog> <rep_catalog> <tp_frontend> <rep_frontend>`
* `java -jar ts.jar <log.json> <n_clients> <tp_auth> <rep_auth> <tp_image> <rep_image><tp_persistence> <rep_persistence> <tp_recommender> <rep_recommender> <tp_web> <rep_web>`
* `java -jar tms.jar <log.json> <n_clients> <tp_cms> <rep_cms> <tp_ems> <rep_ems><tp_qms> <rep_qms> <tp_ums> <rep_ums>`
where
* `<log.json>` specifies the destination of the log file;
* `<n_clients>` is the number of clients circulating the system; 
* `<tp_x>` is the threadpool size of microservice `x`;
* `<rep_x>` is the number of independent replicas of microservice `x`.

To improve the quality of the prediction, the user must disable Turbo Boost and Frequency Scaling on the test machine. On CentOs, the root user must run those commands:
```
modprobe msr
for i in `seq 0 $NCORES`; do wrmsr -p${i} 0x1a0 0x4000850089; done
for CPUFREQ in /sys/devices/system/cpu/cpu*/cpufreq/scaling_governor; do [ -f $CPUFREQ ] || continue; echo -n performance > $CPUFREQ; done
```
where `$NCORES` is the number of CPU virtual cores. To do so, you will need to install the `msr-tools` package on Ubuntu.


### MakeModelTraces

The MakeModelTraces tool (`modeltraces.jar`) analyses a MSA log file.

##### Model generation

The `java -jar modeltraces.jar make <src-dir> <log.json> <src-dir> <model.lqnx>` invocation reads the log in `<log.json>` and the source code directory `<src-dir>` (i.e., the `src` directory of Eclipse projects) to produce the LQN model saved in `<model.lqnx>`. Currently, the tool saves the model in a customized version of the LQNS XML language: future versions will export the model in a fully-compatible format.

##### Response Time calculation

The `java -jar modeltraces.jar rt <log.json> <rt.txt>` invocation reads the log in `<log.json>`, computes the average observed response time of the clients, and saves the result in `<rt.txt>`.

##### Utilization calculation

The `java -jar modeltraces.jar util <log.json> <util.csv>` invocation reads the log in `<log.json>`, computes the average observed utilization of each microservice, and saves the result in `<util.csv>`. The tool exports, for each microservice `x`, the absolute utilization: to obtain the utilization observed in the paper it must be divided by `tp_x * rep_x`.

### Whatif Engine

The Whatif Engine (`whatif.jar`) reads a model file and applies the required whatif alterations.

The syntax is as follows: `java -jar whatif.jar <inputmodel> (<whatif_decl>)* <outputmodel>` where :
* `<inputmodel>` is the original model;
* `(<whatif_decl>)*` contains zero or more whatif declarations;
* `<outputmodel>` is the resulting model after applying the `(<whatif_decl>)*`.
We implemented the following whatif declarations:
* `c <clients>`: the system load is scaled to `<clients>` clients (i.e., W1 in Section VI of the paper);
* `v <msname> <tpool>`: microservice `<msname>` is vertically scaled to `<tpool>` threads (i.e., W2 in Section VI of the paper);
* `h <msname> <tpool>`: microservice `<msname>` is horizontally scaled to `<tpool>` replicas (i.e., W3 in Section VI of the paper).

### The simulator

The simulator (`lqnexec.jar`)  simulates a model file (either original or obtained after a whatif) to produce the statistics of the system described therein.

The `java -jar lqnexec.jar <model.lqnx> <simtime_s> <rt.txt> <util.csv>` invocation reads the model in `<model.lqnx>`, simulates it for `<simtime_s>` (simulated) seconds, and produces `<rt.txt>` and `<util.csv>` as described for MakeModelTraces using the data observed in the simulation.

### Model statistics

The `java -jar lqnexec.jar <model.lqnx> stats` invocation reads the model in `<model.lqnx>` and produces the model statistics, i.e., how many tasks, entries, activities, nodes, OR-nodes, arcs and paths are therein.

## Paper supplementary material

To make the paper easier to understand, we provide a [replication package](https://doi.org/10.5281/zenodo.8075174), i.e., the logs used to generate the models and of the whatif cases (W1, W2, and W3) used in Section 4 for each considered case study, together with the observed and simulated response times and utilizations;

For presentation purposes only (i.e., renaming and lightening of the implementation details), the API of this implementation slightly differs from the one outlined in the paper.
