# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
![version](https://img.shields.io/badge/java-%3E%3D6-red.svg)
![java lifecycle](https://img.shields.io/badge/java%20lifecycle-compile-yellow.svg)
[![Maven Central](https://img.shields.io/badge/maven%20central-1.0.0-brightgreen.svg)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)

简体中文 | [English](README.md)

Java 的注解处理器，根据配置规则生成方法的入参校验逻辑。

## 特性

* 通过在 `编译期` 对语法树进行修改，增加方法入参的校验逻辑。

* 支持 对象类型的非空校验。

* 支持 byte、short、int、long、float、double 的范围校验。

* 支持 String、数组的长度校验。

* 支持 集合、Map 的容量校验。

## 要求

JDK 1.6 及以上版本。

## 快速开始

### 添加依赖

使用 Maven

```
<dependencies>
    <dependency>
        <groupId>io.github.moyada</groupId>
        <artifactId>medivh</artifactId>
        <version>1.0.0</version>
        <scope>provided</scope>
    </dependency>
<dependencies/>
```

使用 Gradle

```
dependencies {
  compileOnly 'io.github.moyada:medivh:1.0.0'
  // 或历史版本方式
  // provided 'io.github.moyada:medivh:1.0.0'
}
```

普通工程可以通过
[![release](https://img.shields.io/badge/release-v1.0.0-blue.svg)](https://github.com/moyada/medivh/releases/latest) 
或
[![Maven Central](https://img.shields.io/maven-central/v/io.github.moyada/medivh.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
下载最新 jar 包。

### 在程序中配置注解

注解描述

| 注解类 | 作用域 | 作用 |
| :---- | :----- | :---- |
| io.moyada.medivh.annotation.Rule | 类字段 | 为类提供相关字段校验规则。 |
| io.moyada.medivh.annotation.Verify | 非静态方法 | 开启方法的校验功能。 |
| io.moyada.medivh.annotation.Check | 方法参数 | 配置参数的校验逻辑，基础类型无效。 |

属性说明

| 属性 | 作用 |
| :--- | :--- |
| Rule.nullable() | 设置非基础类型字段是否可以为空。 |
| Rule.min() | 设置数字类型字段允许的最小值。 |
| Rule.max() | 设置数字类型字段允许的最大值。 |
| Rule.maxLength() | 设置 String、数组、集合 类型允许的最大长度或容量。 |
| Check.invalid() | 设置参数校验失败时抛出异常，异常类需要拥有字符串构造方法。 |
| Check.message() | 异常信息头。 |
| Check.nullable() | 设置方法参数是否允许为空。 |
| Verify.var() | 配置方法生成逻辑时产生的临时变量名称。 |

使用示例见[这里](#示例)。

### 编译项目

使用构建工具的编译命令, 如 `mvn compile` 或 `gradle build`。
 
或者使用 Java 命令进行编译，如 `javac -cp medivh.jar MyApp.java`。

#### 系统可选参数

| 参数 | 作用 |
| :--- | :--- |
| -Dmedivh.method | 配置校验方法名，默认为 invalid0 。 |
| -Dmedivh.var | 配置默认临时变量名称，默认为 mvar_0 。 |
| -Dmedivh.message | 配置默认异常信息头，默认为 invalid argument 。 |

经过编译期后，即可生成校验逻辑。

## 示例

```
import io.moyada.medivh.annotation.*;
import java.util.HashMap;
import java.util.List;

public class MyApp {

    @Verify
    public void run(@Check(invalid = RuntimeException.class) Args args,
                    @Check(message = "something error", nullable = true) Info info,
                    @Check String name, // 可以对普通对象进行非空检查
                    @Check int num // 不支持基本类型
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

如示例中的代码，编译后的内容将会为:

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
