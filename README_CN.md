# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
![version](https://img.shields.io/badge/java-%3E%3D6-red.svg)
![java lifecycle](https://img.shields.io/badge/java%20lifecycle-compile-yellow.svg)
[![Maven Central](https://img.shields.io/badge/maven%20central-1.2.0-brightgreen.svg)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)

[English](README_CN.md) | 简体中文

Medivh是一个注解处理器，根据配置规则生成方法的入参校验逻辑。

## 愿景 

在日常开发中经常需要对方法的入参做校验，特别是在远程调用的方法中。
这款工具可以节省在这方面所花费的精力，通过配置的注解，在 `编译期` 对语法树进行修改，为方法加入参数的校验逻辑。

## 特性

* 支持 对象类型的非空校验。

* 支持 byte、short、int、long、float、double 的范围校验。

* 支持 String、数组的长度校验。

* 支持 集合、Map 的容量校验。

* 校验失败时可以选择抛出异常或返回对象。

## 要求

JDK 1.6 及以上版本。

对接口类型定义校验规则需要 JDK 1.8 及以上版本。

## 快速开始

### 1. 添加依赖

使用 Maven

```
<dependencies>
    <dependency>
        <groupId>io.github.moyada</groupId>
        <artifactId>medivh</artifactId>
        <version>1.2.0</version>
        <scope>provided</scope>
    </dependency>
<dependencies/>
```

使用 Gradle

```
dependencies {
  compileOnly 'io.github.moyada:medivh:1.2.0'
  // 2.12版本以前
  // provided 'io.github.moyada:medivh:1.2.0'
}
```

普通工程可以通过
[![release](https://img.shields.io/badge/release-v1.2.0-blue.svg)](https://github.com/moyada/medivh/releases/latest) 
或
[![Maven Central](https://img.shields.io/maven-central/v/io.github.moyada/medivh.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22io.github.moyada%22%20AND%20a:%22medivh%22)
下载最新 jar 包。

### 2. 在程序中配置注解

注解描述

| 注解 | 作用域 | 作用 |
| :---- | :----- | :---- |
| @NotNull | 类字段、无参方法、方法参数 | 为对象类型的字段或返回值提供非空校验，默认定义了规则都会进行非空校验。 |
| @Nullable | 类字段、无参方法、方法参数 | 不进行非空校验。 |
| @NotBlank | 类字段、无参方法、方法参数 | 对 String 类型提供不可为空白字符串检测。 |
| @SizeRule | 类字段、无参方法、方法参数 | 为 String 或 数组 或 集合 类型提供长度或大小校验。 |
| @NumberRule | 类字段、无参方法、方法参数 | 为基础数字类型提供大小校验。 |
| @Throw | 类、非静态方法、方法参数 | 指定参数校验失败时抛出异常。 |
| @Return | 非静态方法、方法参数 | 指定参数校验失败时返回数据。 |
| @Exclusive | 方法、方法参数 | 禁用校验逻辑。 |
| @Variable | 非静态方法、类 | 修改当前作用域下校验逻辑产生的变量名和方法名。 |

* 使用示例见 _[这里](#示例)_ 和 _[Wiki](https://github.com/moyada/medivh/wiki)_。

属性说明

| 属性 | 作用 |
| :--- | :--- |
| NumberRule.min() | 设置允许的最小值。 |
| NumberRule.max() | 设置允许的最大值。 |
| SizeRule.min() | 设置允许的最小长度或容量。 |
| SizeRule.max() | 设置允许的最大长度或容量。 |
| Throw.value() | 指定抛出异常类，异常类需要拥有字符串构造方法，默认为 `IllegalArgumentException` 。 |
| Throw.message() | 修改异常信息头。 |
| Return.type() | 指定返回数据的类型，需为方法返回类型或子类或实现类。 |
| Return.value() | 设置返回值，当返回类型为对象时需要有对应构造函数。 |

### 3. 编译项目

使用构建工具的编译命令, 如 `mvn compile` 或 `gradle build`。
 
或者使用 Java 命令进行编译，如 `javac -cp medivh.jar MyApp.java`

经过编译期后，即可生成校验逻辑。

#### 系统可选参数

| 参数 | 作用 |
| :--- | :--- |
| medivh.message | 配置默认异常信息头，默认为 `Invalid input parameter` 。 |
| medivh.method | 配置默认校验方法名，默认为 `invalid0` 。 |
| medivh.var | 配置默认临时变量名称，默认为 `mvar_0` 。 |
| medivh.info.null | 配置非空校验信息，默认为 `is null` 。 |
| medivh.info.equals | 配置相等校验信息，默认为 `cannot equals` 。 |
| medivh.info.less | 配置小于校验信息，默认为 `less than` 。 |
| medivh.info.great | 配置大于校验信息，默认为 `great than` 。 |
| medivh.info.blank | 配置空白字符串校验信息，默认为 `is blank` 。 |
| medivh.method.blank | 指定空白字符串校验方法，格式为 `<package>.<className>.<methodName>` ，不指定将创建 `io.moyada.medivh.support.Util` 提供校验方法。 |

## 示例

_[更多用法](https://github.com/moyada/medivh/wiki)_

```
public class MyApp {

    @Throw
    public Info run(Args args,
                    @Nullable Info info,
                    @Return({"test", "0"}) @NotBlank String name,
                    @Return("null") @NumberRule(min = "1") int num) {
        // process
        return new Info();
    }

    class Args {

        @NumberRule(max = "1000") int id;

        @NotNull HashMap<String, Object> param;

        @Nullable @SizeRule(min = 5) boolean[] value;
    }

    static class Info {

        @SizeRule(min = 50) String name;

        @Nullable @NumberRule(min = "-25.02", max = "200") Double price;

        @SizeRule(min = 10, max = 10) List<String> extra;

        public Info() {
        }

        Info(String name, Double price) {
            this.name = name;
            this.price = price;
        }
    }
}
```

如示例中的代码，编译后的内容将会为:

```
public class MyApp {
    public MyApp() {
    }

    public MyApp.Info run(MyApp.Args args, MyApp.Info info, String name, int num) {
        if (args == null) {
            throw new IllegalArgumentException("Invalid input parameter, cause args is null");
        } else {
            String mvar_0 = args.invalid0();
            if (mvar_0 != null) {
                throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
            } else {
                if (info != null) {
                    mvar_0 = info.invalid0();
                    if (mvar_0 != null) {
                        throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
                    }
                }

                if (name == null) {
                    return new MyApp.Info("test", 0.0D);
                } else if (Util.isBlank(name)) {
                    return new MyApp.Info("test", 0.0D);
                } else {
                    return num < 1 ? null : new MyApp.Info();
                }
            }
        }
    }

    static class Info {
        String name;
        Double price;
        List<String> extra;

        public Info() {
        }

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
            } else if (this.extra.size() != 10) {
                return "extra cannot equals 10";
            } else {
                return this.price != null && this.price > 200.0D ? "price great than 200.0" : null;
            }
        }
    }

    class Args {
        int id;
        HashMap<String, Object> param;
        boolean[] value;

        Args() {
        }

        public String invalid0() {
            if (this.param == null) {
                return "param is null";
            } else if (this.value != null && this.value.length < 5) {
                return "value.length less than 5";
            } else {
                return this.id > 1000 ? "id great than 1000" : null;
            }
        }
    }
}
``` 
