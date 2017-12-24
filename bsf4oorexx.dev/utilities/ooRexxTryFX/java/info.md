## Information about jar achives

#### richtextfx-0.7-M5-custom.jar
The java class archive *richtextfx-0.7-M5-custom.jar* consists of the base [richtextfx-0.7-M5.jar](https://github.com/TomasMikula/RichTextFX/releases) with a self-compiled version of `org.fxmisc.flowless.VirtualizedScrollPane`. The latest release of flowless called SNAPSHOT 0.6 does not have the annotations `@namedArgs` in the class `VirtualizedScrollPane` which are necessary to use that class in a fxml file. Please do only overwrite that file if you are confident, that the newer version includes those annotations.

###### Code example
```java
    public VirtualizedScrollPane(@NamedArg("content") V content) {
        [...]
}
```


###### Further links
- https://stackoverflow.com/questions/43695579/passing-objects-as-arguments-using-namedarg-annotation-in-javafx-8/43695630?noredirect=1#comment74438356_43695630
- https://stackoverflow.com/questions/26823157/what-is-the-purpose-of-namedarg-annotation-in-javafx-8/26824088#26824088


#### Used Java archives and their licenses
**File**: `richtextfx-0.7-M5-custom.jar`
**Purpose**: The RichTextFX library offers a Rich Text Editor used for the CodeArea to include line numbers and a number of methods to browse through its content.
**License**: BSD 2 and BSD 3, see https://github.com/FXMisc/RichTextFX/blob/master/LICENSE

- - - -

**File**: `controlsfx-8.40.12.jar`
**Purpose**: ControlsFX extends the variety of JavaFX controls. Its CustomTextField is used in ooRexxTryFX to display a textfield with an icon inside.
**License**: BSD 3, see https://bitbucket.org/controlsfx/controlsfx/src/default/license.txt

- - - -

**Files**: `ikonli-core-1.9.0.jar`, `ikonli-javafx-1.9.0.jar`, `ikonli-octicons-pack-1.9.0.jar`, `ikonli-openiconic-pack-1.9.0.jar`
**Purpose**: The Ikonli library is implemented for many programming languages and offers a huge selection of the most popular icons, which are fairly easy to include with the `FontIcon` class.
**License**: Apache 2.0, see https://mvnrepository.com/artifact/org.kordamp.ikonli/ikonli-javafx/1.9.0
