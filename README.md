# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
![version](https://img.shields.io/badge/java-%3E%3D6-red.svg)
![java lifecycle](https://img.shields.io/badge/java%20lifecycle-compile-yellow.svg)
[![Maven Central](https://img.shields.io/badge/maven%20central-0.1.2-brightgreen.svg)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)

A Java annotation processor, generate verification of method input by configuration rule.

[中文说明](README_CN.md)

## Features

* By modify the syntax tree at `compile time`, to adding verify logic of method input parameter.

* Supported types are primitives (such as int and Integer), String, Array, Collection, Map.

* Support JDK 1.6 or higher.

## Quick start

### Adding dependencies to your project 

#### Maven

```
<dependencies>
    <dependency>
        <groupId>io.github.moyada</groupId>
        <artifactId>medivh</artifactId>
        <version>0.1.2</version>
        <scope>provided</scope>
    </dependency>
<dependencies/>
```

#### Gradle

```
dependencies {
  compileOnly 'io.github.moyada:medivh:0.1.2'
  // or previous version
  // provided 'io.github.moyada:medivh:0.1.2'
}
```

#### Without build tool

You can download last jar from 
[![release](https://img.shields.io/badge/release-v0.1.2-blue.svg)](https://github.com/moyada/medivh/releases/latest) 
or
[![Maven Central](https://img.shields.io/maven-central/v/io.github.moyada/medivh.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
.

### Configure annotation in you program

   The usage of annotation is [here](#Annotation-description).

```
import io.moyada.medivh.annotation.*;
import java.util.HashMap;
import java.util.List;

public class MyApp {

    @Verify
    public void run(@Check(invalid = RuntimeException.class) Args args,
                    @Check(message = "something error", nullable = true) Info info,
                    @Check String name, // can be check null value for normal Object
                    @Check int num // ineffective type
                    ) {
        // process
        ...
    }

    class Args {

        @Rule(min = 10, max = 2000) int id;

        @Rule HashMap<String, Object> param;

        @Rule(maxLength = 5, nullable = true) String[] value;
    }

    class Info {

        @Rule(maxLength = 20) String name;

        @Rule(min = -250, max = 500, nullable = true) Double price;
        
        @Rule(maxLength = 10) List<String> extra;
    }
}
```

### Compile project

Use compile commands of build tool, like `mvn compile` or `gradle build`.
 
Or use java compile command, like `javac -cp medivh.jar MyApp.java`.

After compilation, the verification logic will be generated.

As the sample code，the compiled content will be:

```
public void run(Args args, Info info, String name, int num) {
    if (args == null) {
        throw new RuntimeException("invalid argument while attempting to access io.moyada.MyApp.go(), because ".concat("args is null"));
    } else {
        String mvar_0 = args.invalid0();
        if (mvar_0 != null) {
            throw new RuntimeException("invalid argument while attempting to access io.moyada.MyApp.go(), because ".concat(mvar_0));
        } else {
            if (info != null) {
                mvar_0 = info.invalid0();
                if (mvar_0 != null) {
                    throw new IllegalArgumentException("something error while attempting to access io.moyada.MyApp.go(), because ".concat(mvar_0));
                }
            }

            if (name == null) {
                throw new IllegalArgumentException("invalid argument while attempting to access io.moyada.MyApp.go(), because ".concat("name is null"));
            } else {
                // process
                ...
            }
        }
    }
}

class Info {
    String name;
    Double price;
    List<String> extra;

    Info() {
    }

    public String invalid0() {
        if (this.name == null) {
            return "name is null";
        } else if (this.name.length() > 20) {
            return "name.length() great than 20";
        } else if (this.extra == null) {
            return "extra is null";
        } else if (this.extra.size() > 10) {
            return "extra.size() great than 10";
        } else {
            if (this.price != null) {
                if (this.price < 0.0D) {
                    return "price less than 0";
                }

                if (this.price > 500.0D) {
                    return "price great than 500";
                }
            }

            return null;
        }
    }
}

class Args {
    int id;
    HashMap<String, Object> param;
    String[] values;

    Args() {
    }

    public String invalid0() {
        if (this.param == null) {
            return "param is null";
        } else if (this.id < 10) {
            return "id less than 10";
        } else {
            return this.id > 2000 ? "id great than 2000" : null;
        }
    }
}
``` 

## Annotation description

| Annotation Class | Action Scope | Effect |
| :---- | :----- | :---- |
| io.moyada.medivh.annotation.Rule | field | provide validate rule for enclose class. |
| io.moyada.medivh.annotation.Verify | non-abstract method | enable verification function for this method. |
| io.moyada.medivh.annotation.Check | method parameter | configure check logic for method parameter，except the primitive type. |

## Annotation attribute

| Attribute | Effect |
| :--- | :--- |
| Rule.nullable() | indicates whether or not this field allowed to be null，except primitive type. |
| Rule.min() | set the minimum allowed value of number type. |
| Rule.max() | set the maximum allowed value of number type. |
| Rule.maxLength() | set the maximum allowed length or capacity of String, Array, Collection, Map. |
| Check.invalid() | set the exception thrown when the argument is invalid, the exception type must have a String constructor. |
| Check.message() | message head of thrown exception. |
| Check.nullable() | indicates whether or not this parameter allowed to be null. |
| Verify.value() | configure the name of temporary variable, generated by the verify logic. |

## system properties

| Property | Effect |
| :--- | :--- |
| -Dmedivh.method | configure the name of validate method，default is invalid0 . |
| -Dmedivh.var | configure the name of default temporary variable，default is mvar_0 . |

