# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
![version](https://img.shields.io/badge/java-%3E%3D6-red.svg)
![java lifecycle](https://img.shields.io/badge/java%20lifecycle-compile-yellow.svg)
[![Maven Central](https://img.shields.io/badge/maven%20central-1.1.0-brightgreen.svg)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)

简体中文 | [English](README.md)

Java 的注解处理器，根据配置规则生成方法的入参校验逻辑。

## 特性

* 通过在 `编译期` 对语法树进行修改，增加方法入参的校验逻辑。

* 支持 对象类型的非空校验。

* 支持 byte、short、int、long、float、double 的范围校验。

* 支持 String、数组的长度校验。

* 支持 集合、Map 的容量校验。

* 校验失败时可以选择抛出异常或返回对象。

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
        <version>1.1.0</version>
        <scope>provided</scope>
    </dependency>
<dependencies/>
```

使用 Gradle

```
dependencies {
  compileOnly 'io.github.moyada:medivh:1.1.0'
  // 2.12版本以前
  // provided 'io.github.moyada:medivh:1.1.0'
}
```

普通工程可以通过
[![release](https://img.shields.io/badge/release-v1.1.0-blue.svg)](https://github.com/moyada/medivh/releases/latest) 
或
[![Maven Central](https://img.shields.io/maven-central/v/io.github.moyada/medivh.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
下载最新 jar 包。

### 在程序中配置注解

注解描述

| 注解类 | 作用域 | 作用 |
| :---- | :----- | :---- |
| io.moyada.medivh.annotation.Nullable | 类字段、无参方法、方法参数 | 设置非基础类型字段可以为空，优先于 NotNull。 |
| io.moyada.medivh.annotation.NotNull | 类字段、无参方法 | 设置非基础类型字段不可为空，当使用任意 Rule 时默认设置。 |
| io.moyada.medivh.annotation.NotBlank | 类字段、无参方法、方法参数 | 定义 String 字段不可为空白字符串。 |
| io.moyada.medivh.annotation.SizeRule | 类字段、无参方法、方法参数 | 为类提供长度或容量类型字段校验规则。 |
| io.moyada.medivh.annotation.NumberRule | 类字段、无参方法 | 为类提供数字类型字段校验规则。 |
| io.moyada.medivh.annotation.Throw | 方法参数 | 配置参数的校验逻辑，校验失败抛出异常，基础类型无效。 |
| io.moyada.medivh.annotation.Return | 方法参数 | 配置参数的校验逻辑，校验失败返回数据，基础类型无效。 |
| io.moyada.medivh.annotation.Variable | 非静态方法 | 修改当前方法临时变量名。 |

属性说明

| 属性 | 作用 |
| :--- | :--- |
| NumberRule.min() | 设置数字类型字段允许的最小值。 |
| NumberRule.max() | 设置数字类型字段允许的最大值。 |
| SizeRule.min() | 设置 String、数组、集合 类型允许的最小长度或容量。 |
| SizeRule.max() | 设置 String、数组、集合 类型允许的最大长度或容量。 |
| Throw.value() | 设置抛出异常类，异常类需要拥有字符串构造方法。 |
| Throw.message() | 异常信息头。 |
| Return.value() | 设置返回值，支持返回类型为基本类型或对象，当返回类型为对象时需要有对应构造函数。 |
| Return.type() | 设置返回值的类型，需为方法返回类型的子类或实现类。 |
| Variable.value() | 配置方法生成逻辑时产生的临时变量名称。 |

使用示例见[这里](#示例)。

### 编译项目

使用构建工具的编译命令, 如 `mvn compile` 或 `gradle build`。
 
或者使用 Java 命令进行编译，如 `javac -cp medivh.jar MyApp.java`。

#### 系统可选参数

| 参数 | 作用 |
| :--- | :--- |
| -Dmedivh.method | 配置校验方法名，默认为 `invalid0` 。 |
| -Dmedivh.var | 配置默认临时变量名称，默认为 `mvar_0` 。 |
| -Dmedivh.message | 配置默认异常信息头，默认为 `Invalid input parameter` 。 |
| -Dmedivh.info.null | 配置默认非空校验信息，默认为 `is null` 。 |
| -Dmedivh.info.equals | 配置默认相等校验信息，默认为 `cannot equals` 。 |
| -Dmedivh.info.less | 配置默认小于校验信息，默认为 `less than` 。 |
| -Dmedivh.info.great | 配置默认大于校验信息，默认为 `great than` 。 |

经过编译期后，即可生成校验逻辑。

## 示例

[更多用法](https://github.com/moyada/medivh/tree/master/src/test/java/cn/moyada/test)

```
import io.moyada.medivh.annotation.*;
import java.util.HashMap;
import java.util.List;

public class MyApp {
   
    public Info run(@Throw(RuntimeException.class) Args args,
                    @Throw(message = "something error") @Nullable Info info,
                    @Return({"test", "-0.503"}) String name, // 支持其他对象的非空校验
                    @Return("null") int num // 不支持对象
                    ) {
        // process
        ...
    }

    class Args {
    
        @NumberRule(max = "1000") int id;

        @NotNull HashMap<String, Object> param;

        @Nullable @SizeRule(min = 5) boolean[] value;
    }
    
    class Info {

        @SizeRule(min = 50) String name;

        @Nullable @NumberRule(min = "-25.02", max = "200") Double price;

        @SizeRule(min = 10, max = 10) List<String> extra;

        Info(String name, Double price) {
            this.name = name;
            this.price = price;
        }
    }
}
```

如示例中的代码，编译后的内容将会为:

```
public Info run(Args args, Info info, String name, int num) {
    if (args == null) {
        throw new RuntimeException("Invalid input parameter, cause args is null"));
    } else {
        String mvar_0 = args.invalid0();
        if (mvar_0 != null) {
            throw new RuntimeException("Invalid input parameter, cause " + mvar_0);
        } else {
            if (info != null) {
                mvar_0 = info.invalid0();
                if (mvar_0 != null) {
                    throw new IllegalArgumentException("something error, cause " + mvar_0);
                }
            }

            if (name == null) {
                return new ProcessorTest.Info("test", -0.503D);
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

    Info(String name, Double price) {
        this.name = name;
        this.price = price;
    }

    public String invalid0() {
        if (this.name == null) {
            return "name is null";
        } else if (this.name.length() < 50) {
            return "name.length() less than 50";
        } else if (this.extra == null) {
            return "extra is null";
        } else if (this.extra.size() == 10) {
            return "extra.size() cannot equals 10";
        } else {
            if (this.price != null) {
                if (this.price > 200.0D) {
                    return "price great than 200.0";
                }

                if (this.price < -25.02D) {
                    return "price less than -25.02";
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
        if (this.id > 1000) {
            return "id great than 1000";
        } else if (this.param == null) {
            return "param is null";
        } else {
            return this.value != null && this.value.length < 5 ? "value.length less than 5" : null;
        }
    }
}
``` 
