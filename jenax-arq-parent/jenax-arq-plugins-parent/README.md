---
title: RDF/SPARQL Plugins
#parent: ARQ Extensions
has_children: true
nav_order: 30
---

# RDF/SPARQL Plugins
The source code of the SPARQL plugins are located under [jenax-arq-parent/jenax-arq-plugins-parent](https://github.com/Scaseco/jenax/edit/develop/jenax-arq-parent/jenax-arq-plugins-parent).

The module `jenax-arq-plugins-bundle` bundles the function extensions and all other plugins. The individual plugins of the bundle can also be used separatedly if desired.

## Available Plugins

* `jenax-arq-plugins-bundle`: Provides all functions extensions and the plugins below.
* `jenax-arq-plugins-datatypes`: Introduces datatypes for XML, JSON, as well as Jena objects, such as `Query`, `Binding` and `Expr`.
* `jenax-arq-plugins-service-vfs`: Virtual filesystem plugin that enhances the service clause to enable access to access resources on virtual filesystems using the `SERVICE <vfs:...>` protocol.

## Programmatic Usage

The plugin bundles can be directly used in Java projects using Maven dependencies, such as:

```xml
<dependency>
  <groupId>org.aksw.jenax</groupId>
  <artifactId>jenax-arq-plugins-bundle</artifactId>
  <version><!-- See the published versions link below--></version>
</dependency>
```

[Published versions](https://search.maven.org/artifact/org.aksw.jenax/jenax-arq-plugins-bundle).

## As Fuseki Plugins
For this the desired bundle needs to be build `mvn -P ext package`.

```xml
# Clone this repository, then
mvn -P ext -pl :jenax-arq-plugins-bundle -am package
```

**Explanation:**

* `-P ext`: Actives the profile `ext` which includes the build of the bundles
* `-pl :jenax-arq-plugins-bundle`: Set the "project list" to build to the plugins bundle.
* `-am`: Also make. Build any module required by the projects specified in `-pl`.
* `packace`: Run the maven build up to the package phase which creates the bundle.

The output file is located under: `jenax-arq-parent/jenax-arq-plugins-parent/jenax-arq-plugins-bundle/target/jenax-arq-plugins-bundle-VERSION.jar`




