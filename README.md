NexusData
===========
**Core Data for Android**

NexusData is an object graph persistence framework for Android. Essentially, it brings Core Data functionality from
iOS to Android. The goal is not intended to be a straight port of Core Data. Instead, it aims to bring the best from
Core Data's while having the flexibility to evolve independently, leveraging modern software architecture developments.

NexusData supports Android API 10+. This library follows [semantic versioning](http://semver.org/). Note that this
library is still active in development, and is missing a lot of features. New releases might introduce
interface-breaking changes, which will be indicated in the changelog. NexusData 1.0.0 will be the first stable release.

A Simple Example
----------------
TODO

For more examples, browse the [samples](http://github.com/dkharrat/NexusData/tree/master/samples) directory.

Features
--------
TODO

Apps Using NexusData
----------------------
Do you have an app that's utilizing NexusData? [Let me know](mailto:dkharrat@gmail.com) and I'll add a link to it here!

Limitations
----------------
The framework is constantly being improved and new features are being implemented. Since it's very early stage, it
currently has some limitations:

* TODO

How to Add NexusData to Your Project
--------------------------------------
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
----------------------
TODO

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

