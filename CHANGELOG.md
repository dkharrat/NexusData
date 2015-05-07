Changelog
=========

0.2.1
-----
* Support entity inheritence
* Defining the inverse for a relationship is now optional
* modelgen now supports file includes, which allows breaking up model into multiple files.
* Bug fixes and performance improvements

0.1.3
-----
* Fixed exception when using 'null' in comparisons (Issue #6).
* Update project to use latest android-gradle settings.
* Use Java generics for a ManagedObject-based collection.
* Fixed crash when model does not define relationships.
* Sources are now published to Maven repo too.

0.1.2
-----
* Support 'null' keyword in predicates (e.g. "name == null")
* Support querying for a specific object instance (currently, predicate can
  only be built via `ExpressionBuilder`, but not yet via expression parser)

0.1.1
-----
* Add support for Double / Float types (be sure to use latest version of modelgen)

0.1.0
-----
* Initial release.
