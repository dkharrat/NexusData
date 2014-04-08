NexusData
=========
**Core Data for Android**

NexusData is an object graph and persistence framework for Android. It allows for organizing and managing relational
data and serializing it to SQLite or custom stores. The data can be accessed or modified using higher level objects
representing entities and their relationships. NexusData manages all the objects in the persistence store, tracks
changes, and maintains consistency in relationships.

Essentially, it brings Core Data functionality from iOS to Android. However, the library is not intended to be a
straight port of Core Data. Instead, it aims to leverage Core Data's concepts, while having the flexibility to evolve
independently.

NexusData is not an ORM in that it's more higher-level and is not tied to a specific storage engine. The query interface
is oblivious to the underlying persistence store.

NexusData supports Android API 10+. This library follows [semantic versioning](http://semver.org/). Note that this
library is still active in development, and is missing a lot of features. New releases might introduce
interface-breaking changes, which will be indicated in the changelog. NexusData 1.0.0 will be the first stable release.

Samples
-------

For sample projects that use NexusData, browse the [samples](http://github.com/dkharrat/NexusData/tree/master/samples)
directory.

Features
--------
* Change tracking and management of objects.
* Relationship maintenance by automatically propagating related changes to maintain consistency.
* Support for one-to-one and one-to-many relationships.
* Lazy loading of the object graph to reduce memory overhead.
* Flexible query interface that is independent of the underlying storage engine.
* Model generator that generates java classes from the model.
* Support for atomic and incremental persistence stores.
* Extensible to different persistence storage. Currently, two storage engines are provided out of the box:
  * In-memory
  * SQLite
* Built-in support for basic attribute types:
  * Short
  * Integer
  * Boolean
  * String
  * Enum
  * Date

Limitations
-----------
The framework is constantly being improved and new features are being implemented. Since it's very early stage, it
currently has some limitations:

* Undo/Redo is not supported.
* Many-to-many relationships are not supported yet.
* Schema migrations are not supported yet.
* Query syntax is currently limited to comparisons and boolean logic. Operations like aggregations and joining are not
  supported yet.
* Framework is not yet optimized for large data sets in terms of performance and memory. This is due to the early
  development of the project and will be improved over time.
* Custom data types are not supported yet.

Apps Using NexusData
--------------------
Do you have an app that's utilizing NexusData? [Let me know](mailto:dkharrat@gmail.com) and I'll add a link to it here!

How to Add NexusData to Your Project
------------------------------------
There are multiple ways to include your project, depending on your build environment:

#### Gradle

Add the following dependency to your build.gradle file for your project:

    dependencies {
      compile 'com.github.dkharrat.nexusdata:nexusdata:0.1.0'
    }

#### Maven

Add the following dependency to your pom.xml file for your project:

    <dependency>
        <groupId>com.github.dkharrat.nexusdata</groupId>
        <artifactId>nexusdata</artifactId>
        <version>0.1.0</version>
        <type>jar</type>
    </dependency>

#### Android Studio or IntelliJ 13+

Add the appropriate dependency in your build.gradle file and refresh your project.

#### Eclipse

TODO
Download the jar file from [TBD] and place it under the libs/ directory in your project.

How to Use NexusData
--------------------
Please browse through the samples included with the project for examples on how NexusData can be used.

Documentation
-------------
See the current [Javadoc](http://dkharrat.github.io/NexusData/javadoc/).

Contributing
------------
Contributions via pull requests are welcome! For suggestions, feedback, or feature requests, please submit an issue.

Author
------
Dia Kharrat - dkharrat@gmail.com<br/>
Twitter: [http://twitter.com/dkharrat](http://twitter.com/dkharrat)

License
-------
    Copyright 2014 Dia Kharrat

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

