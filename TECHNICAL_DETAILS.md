# Technical Details

There project requires Gradle 7+, Kotlin 1.7+  and Java version specified in the `gradle.properties`.

Once a pull request is merged a new version will be automatically built along with a draft release.

### Testing
Essentially we need evidence that the change works as intended and does not cause undesired changes elsewhere.

* Unit tests within the module.
* Integration tests using [bst-ui-tests](https://github.com/Backbase/bst-ui-tests).
