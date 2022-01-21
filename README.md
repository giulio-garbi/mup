# μP: A Development Framework for Accurate Performance Predictions in Microservices Systems

This repository contains the source code of μP Framework together with four case studies taken from the microservices literature. The repository is organized as a [Maven](https://maven.apache.org/) with submodule.

## Organization of this repository

This section describes the components of the repository's root directory. It contains three files: 
* .gitignore : used to avoid pushing to the repository the files generated from the build phase.
* README.md : this file
* pom.xml : POM module used to build the whole repository, including all the case studies and the supporting tools.

The following subsections present the content of the directories, each being one module of the main pom.xml file.

### MicroservicesFramework

This directory contains the source code of the framework. Developers can download this module and add it as a dependency in the pom.xml's `dependencies` node as follows:
```
<dependency>
    <groupId>MicroservicesFramework</groupId>
    <artifactId>MicroservicesFramework</artifactId>
</dependency>
```
