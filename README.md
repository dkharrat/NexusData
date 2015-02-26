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
  * Short / Integer / Long
  * Boolean
  * Float / Double
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
* Entity inheritance is not supported yet.

Apps Using NexusData
--------------------
Do you have an app that's utilizing NexusData? [Let me know](mailto:dkharrat@gmail.com) and I'll add a link to it here!

How to Add NexusData to Your Project
------------------------------------
There are multiple ways to include your project, depending on your build environment:

#### Gradle

Add the following dependency to your build.gradle file for your project:

    dependencies {
      compile 'com.github.dkharrat.nexusdata:nexusdata:0.1.2'
    }

#### Maven

Add the following dependency to your pom.xml file for your project:

    <dependency>
        <groupId>com.github.dkharrat.nexusdata</groupId>
        <artifactId>nexusdata</artifactId>
        <version>0.1.2</version>
        <type>jar</type>
    </dependency>

#### Android Studio or IntelliJ 13+

Add the appropriate dependency in your build.gradle file and refresh your project.

#### Eclipse

TBD

How to Get Started
--------------------
For a complete example of how NexusData can be used, please browse through
[the samples](https://github.com/dkharrat/NexusData/tree/master/samples) included with the project.

### Defining the model
The model is used to provide NexusData with information about the entities and their properties. A model can be defined
either programmatically or via JSON file. Here's an example of a JSON-based model file for a ToDo app:

todo.model.json:
```json
{
  "metaVersion": 1,
  "model": {
    "name": "Todo",
    "version": 3,
    "packageName": "org.example.todo",
    "entities": [{
      "name": "Task",
      "enums": [{
        "name": "Priority",
        "values": ["HIGH", "MEDIUM", "LOW"]
      }],
      "attributes": [{
        "name": "title",
        "type": "String"
      }, {
        "name": "notes",
        "type": "String"
      }, {
        "name": "dueBy",
        "type": "Date"
      }, {
         "name": "completed",
         "type": "Bool",
         "required": true,
         "default": false
       }, {
         "name": "priority",
         "type": "Priority"
       }],
      "relationships": [{
        "name": "assignedTo",
        "destinationEntity": "User",
        "inverseName": "tasks",
        "toMany": false
      }]
    }, {
      "name": "User",
      "attributes": [{
        "name": "name",
        "type": "String"
      }],
      "relationships": [{
        "name": "tasks",
        "destinationEntity": "Task",
        "inverseName": "assignedTo",
        "toMany": true
      }]
    }]
  }
}
```

This model (named "Todo") defines two entities: `Task` and `User`. A `Task` belongs to a `User`, and a `User` has many
`Task`s. Also, each entity has some attributes.

### Generating classes from a model file
NexusData comes with a Model Generator that allows you to generate an appropriate class for each entity. Though using
the generator is not necessary to use NexusData, the Model Generator reduces the need to write a lot of repetitive and
boilerplate code for each entity (getter, setters, etc.). If you choose not to use the Model Generator, you may either
create the classes yourself or use `ManagedObject` directly.

A pre-built binary of the Model Generator is available [here](http://dkharrat.github.io/NexusData/bin/modelgen/modelgen-0.1.0.jar)
or you can build it from source yourself.

To generate the appropriate classes from the above model file, run this command:

    java -jar modelgen-0.1.0.jar -f todo.model.json -O src/main/java/org/example/todo

This will parse the `todo.model.json` file and generate the corresponding classes in the
`src/main/java/org/example/todo` directory. The output of the generator will look something like this:

    03:43:41.970 [main] INFO  c.g.d.n.modelgen.ModelGenerator - Setting up model generator
    03:43:42.028 [main] INFO  c.g.d.n.modelgen.ModelGenerator - Parsing model file 'todo.model.json'
    03:43:42.098 [main] INFO  c.g.d.n.modelgen.ModelGenerator - Generating class files for 'Todo' model (version 1)
    03:43:42.152 [main] INFO  c.g.d.n.modelgen.ModelGenerator - Generating class Task.java
    03:43:42.161 [main] INFO  c.g.d.n.modelgen.ModelGenerator - Generating class _Task.java
    03:43:42.184 [main] INFO  c.g.d.n.modelgen.ModelGenerator - Generating class User.java
    03:43:42.184 [main] INFO  c.g.d.n.modelgen.ModelGenerator - Generating class _User.java

For each entity, two classes will be generated. For example, for the `Task` entity, `Task.java` and `_Task.java` are
generated. The `_Task.java` file contains all the accessors, enums, and relationships based on the model file. The
other file, `Task.java`, is an empty class that inherits from `_Task`. The `Task` class can be used to define any custom
code or derived properties. The reason for creating two classes is to allow you to re-generate the entity classes if
the model changes, while maintaining any custom code associated with the entity in a separate file. The generator will
not overwrite your custom class (e.g. `Task.java` in this example) if it already exists, but it will overwrite the
base class (e.g. `_Task.java`).

Here's how the generated files look like for the `Task` entity:

_Task.java:
```java
// THIS IS AN AUTO-GENERATED CLASS FILE. DO NOT EDIT DIRECTLY.

package org.example.todo;

import java.util.Date;
import com.github.dkharrat.nexusdata.core.ManagedObject;

class _Task extends ManagedObject {

    public interface Property {
        final static String TITLE = "title";
        final static String NOTES = "notes";
        final static String DUE_BY = "dueBy";
        final static String COMPLETED = "completed";
        final static String PRIORITY = "priority";
        final static String ASSIGNED_TO = "assignedTo";
    }

    public enum Priority {
        HIGH,
        MEDIUM,
        LOW,
    }

    public String getTitle() {
        return (String)getValue(Property.TITLE);
    }

    public void setTitle(String title) {
        setValue(Property.TITLE, title);
    }

    public String getNotes() {
        return (String)getValue(Property.NOTES);
    }

    public void setNotes(String notes) {
        setValue(Property.NOTES, notes);
    }

    public Date getDueBy() {
        return (Date)getValue(Property.DUE_BY);
    }

    public void setDueBy(Date dueBy) {
        setValue(Property.DUE_BY, dueBy);
    }

    public boolean isCompleted() {
        return (Boolean)getValue(Property.COMPLETED);
    }

    public void setCompleted(boolean completed) {
        setValue(Property.COMPLETED, completed);
    }

    public Priority getPriority() {
        return (Priority)getValue(Property.PRIORITY);
    }

    public void setPriority(Priority priority) {
        setValue(Property.PRIORITY, priority);
    }


    public User getAssignedTo() {
        return (User)getValue(Property.ASSIGNED_TO);
    }

    public void setAssignedTo(User assignedTo) {
        setValue(Property.ASSIGNED_TO, assignedTo);
    }
}
```

Task.java:
```java
package org.example.todo;

public class Task extends _Task {
    public Task() {
    }
}
```

### Initializing NexusData Stack

To setup NexusData in your application, you'll need three main parts: `ObjectModel`, `PersistentStoreCoordinator`,
and a `ObjectContext`. These can typically be initialized once at startup and used throughout the lifetime of the
application.

```java
// create an ObjectModel that describes the meta model
ObjectModel model = new ObjectModel(app.getAssets().open("todo.model.json"));

// create the persistent store coordinator and its associated store
PersistentStoreCoordinator storeCoordinator = new PersistentStoreCoordinator(model);
Context ctx = getApplicationContext(); // the Android context
PersistentStore cacheStore = new AndroidSqlPersistentStore(ctx, ctx.getDatabasePath("todo"));
storeCoordinator.addStore(cacheStore);

// create an ObjectContext that will be used to retrieve or save our objects
ObjectContext mainObjectContext = new ObjectContext(storeCoordinator);
```

### Creating/Updating Objects

```java
ObjectContext objCtx = getMainObjectContext();
Task task1 = objCtx.newObject(Task.class);
task1.setTitle("Get groceries");

Task task2 = objCtx.newObject(Task.class);
task2.setTitle("File taxes");

objCtx.save();
```

### Deleting Objects

```java
objCtx.delete(task1);
objCtx.save();
```

### Querying All Objects of Specific Type

```java
List<Task> tasks = objCtx.findAll(Task.class)
```

### Querying Objects Satisfying a Predicate

For example, to query all Tasks that are complete:

```java
FetchRequest<Task> fetchRequest = objCtx.newFetchRequestBuilder(Task.class)
    .predicate("completed == true")
    .build();
List<Task> tasks = objCtx.executeFetchOperation(fetchRequest);
```

### Use `ObjectContext` and `ManagedObject`s in multiple threads

Similar to Core Data, `ManagedObject` and `ObjectContext` are not thread-safe, and therefore, should not be used in
other threads. Each thread must use its own instance of `ObjectContext`. To pass objects between multiple threads,
pass the object's `ObjectID` to the other thread, which can then retrieve the object from it's own `ObjectContext`.

To synchronize multiple `ObjectContext` with any changes, register a listener and then merge the changes when receiving
a notification, as follows:

```java
ObjectContextNotifier.registerListener(new ObjectContextNotifier.DefaultObjectContextListener() {
    @Override public void onPostSave(ObjectContext context, ChangedObjectsSet changedObjects) {
        // ensure that the notification we just got is not from our own context, and that it's from a context using a
        // persistence store that our context is also using.
        if (context != mainObjectContext && context.getPersistentStoreCoordinator() == mainObjectContext.getPersistentStoreCoordinator()) {
            mainObjectContext.mergeChangesFromSaveNotification(changedObjects);
        }
    }
});
```

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

