# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)

[English](/README) | 简体中文

简单、自动、灵活的 Java 方法入参校验器。

## 愿景 

在日常开发中经常需要对方法的入参做校验，特别是在远程调用的方法中。
这款工具可以节省在这方面所花费的精力，通过配置的注解，在 `编译期` 对语法树进行修改，为方法加入参数的校验逻辑。

## 特性

* 对象类型的非空校验。

* 对基础数字类型提供大小范围校验，比如 int 和 Integer。

* 检查 String 是否为空白字符串。

* 对 String、数组的长度进行校验。

* 对集合、Map 的容量进行校验。

* 在校验失败时进行抛出异常或返回数据。

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
        <version>1.2.2</version>
        <scope>provided</scope>
    </dependency>
<dependencies/>
```

使用 Gradle

```
dependencies {
  compileOnly 'io.github.moyada:medivh:1.2.2'
}
```

普通工程可以通过
[![release](https://img.shields.io/badge/release-v1.2.2-blue.svg)](https://github.com/moyada/medivh/releases/latest) 
或
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.moyada/medivh)
下载最新 jar 包。

### 2. 在程序中配置注解

注解说明

| 注解 | 作用域 | 作用 |
| :---- | :----- | :---- |
| @NotNull | 类字段、无参方法、方法参数 | 为对象类型的字段或返回值提供非空校验，默认定义了规则都会进行非空校验。 |
| @Nullable | 类字段、无参方法、方法参数 | 不进行非空校验。 |
| @NotBlank | 类字段、无参方法、方法参数 | 对 String 类型提供空白字符串校验规则。 |
| @SizeRule | 类字段、无参方法、方法参数 | 为 String 或 数组 或 集合 类型提供长度或大小校验规则。 |
| @NumberRule | 类字段、无参方法、方法参数 | 为基础数字类型提供大小校验规则。 |
| @Throw | 类、非静态方法、方法参数 | 指定参数校验失败时抛出异常。 |
| @Return | 非静态方法、方法参数 | 指定参数校验失败时返回数据。 |
| @Exclusive | 方法、方法参数 | 禁用校验逻辑。 |
| @Variable | 类、方法 | 修改当前作用域下校验逻辑产生的变量名和方法名。 |

具体使用方式见 [示例](#示例)

### 3. 编译项目

使用构建工具的编译命令, 如 `mvn compile` 或 `gradle build`。
 
或者使用 Java 命令进行编译，如 `javac -cp medivh.jar MyApp.java`

经过编译期，配置规则注解的类文件将会创建 `验证` 方法，由参数校验逻辑调用，配置参数校验的方法则会在方法体前添加校验逻辑。

## 系统可选参数

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
| medivh.method.blank | 指定空白字符串校验方法，格式为 `<package>.<className>.<methodName>` ，不指定将随机选取个 public 标识类创建此方法。 |

## 示例

### 定义对象规则

#### @NumberRule

| 作用域 | 用途 |
| :---- | :----- |
| 类字段、无参方法、方法参数 | 为基础数字类型提供大小校验规则。 |

属性说明

| 名称 | 类型 | 作用 |
| :--- | :--- | :--- |
| min() | 字符串 | 设置允许的最小值。 |
| max() | 字符串 | 设置允许的最大值。 |

```
public class Counter {

    @NumberRule(min = "0", max = "1000")
    private int count;

    @NumberRule(min = "-20.5", max = "100")
    private Double lastest;

    @NumberRule(min = "1", max = "1")
    private byte type;
}
```

经过编译后的验证方法为:

```
public String invalid0() {
    if (this.lastest == null) {
        return "lastest is null";
    } else if (this.lastest > 100.0D) {
        return "lastest great than 100.0";
    } else if (this.type != 1) {
        return "type cannot equals 1";
    } else {
        int var$3 = this.count;
        if (var$3 > 1000) {
            return "count great than 1000";
        } else {
            return var$3 < 0 ? "count less than 0" : null;
        }
    }
}
```

#### @SizeRule

| 作用域 | 用途 |
| :---- | :----- |
| 类字段、无参方法、方法参数 | 为 String 或 数组 或 集合 类型提供长度或大小校验规则。 |

属性说明

| 名称 | 类型 | 作用 |
| :--- | :--- | :--- |
| min() | 整数 | 设置允许的最小长度或容量。 |
| max() | 整数 | 设置允许的最大长度或容量。 |

```
public class Capacity {

    public Capacity() {
    }

    public Capacity(String type, boolean counters) {
        this.type = type;
    }

    @SizeRule(min = 0, max = 50)
    private String type;

    @SizeRule(min = 1)
    private byte[] getTypes() {
        return new byte[0];
    }

    @SizeRule(max = 200)
    private List<Counter> counters;

    @SizeRule(min = 10, max = 10)
    public Map<String, Integer> getEntry() {
        return new HashMap<String, Integer>();
    }
}
```

经过编译后的验证方法为:

```
public String invalid0() {
    Map<String, Integer> getEntry = this.getEntry();
    if (getEntry == null) {
        return "getEntry is null";
    } else if (getEntry.size() != 10) {
        return "getEntry cannot equals 10";
    } else {
        byte[] getTypes = this.getTypes();
        if (getTypes == null) {
            return "getTypes is null";
        } else if (getTypes.length < 1) {
            return "getTypes.length less than 1";
        } else if (this.type == null) {
            return "type is null";
        } else {
            int var$3 = this.type.length();
            if (var$3 > 50) {
                return "type.length() great than 50";
            } else if (var$3 < 0) {
                return "type.length() less than 0";
            } else if (this.counters == null) {
                return "counters is null";
            } else {
                return this.counters.size() > 200 ? "counters.size() great than 200" : null;
            }
        }
    }
}
```

#### @NotBlank

| 作用域 | 用途 |
| :---- | :----- |
| 类字段、无参方法、方法参数 | 对 String 类型提供空白字符串校验规则。 |


```
public abstract class Person {

    @NotBlank
    public abstract String getName();
}
```

经过编译后的验证方法为:

```
public static boolean isBlank(String str) {
    int length = str.length();
    if (length == 0) {
        return true;
    } else {
        for(int i = 0; i < length; ++i) {
            char ch = str.charAt(i);
            if (ch != ' ') {
                return false;
            }
        }

        return true;
    }
}

public String invalid0() {
    String getName = this.getName();
    if (getName == null) {
        return "getName is null";
    } else {
        return isBlank(getName) ? "getName is blank" : null;
    }
}
```

#### @NotNull

| 作用域 | 用途 |
| :---- | :----- |
| 类字段、无参方法、方法参数 | 为对象类型的字段或返回值提供非空校验，默认定义了规则都会进行非空校验。 |


```
@Variable("check0")
public class Param {

    @NotNull
    private String name;

    @NotNull
    private Object value;
}
```

经过编译后的验证方法为:

```
public String check0() {
    if (this.value == null) {
        return "value is null";
    } else {
        return this.name == null ? "name is null" : null;
    }
}
```

#### @Nullable

| 作用域 | 用途 |
| :---- | :----- |
| 类字段、无参方法、方法参数 | 不进行非空校验。 |


```
public interface Product {

    String getName();

    @Nullable
    @NotBlank
    String getType();

    @Nullable
    @SizeRule(min = 0)
    List<Capacity> getStore();
}
```

经过编译后的验证方法为:

```
default String invalid0() {
    List<Capacity> getStore = this.getStore();
    if (getStore != null && getStore.size() < 0) {
        return "getStore.size() less than 0";
    } else {
        String getName = this.getType();
        if (getName != null && Person.isBlank(getName)) {
            return "getType is blank";
        } else {
            getName = this.getName();
            return getName == null ? "getName is null" : null;
        }
    }
}
```

### 配置方法校验

#### @Throw

| 作用域 | 用途 |
| :---- | :----- |
| 类、非静态方法、方法参数 | 指定参数校验失败时抛出异常。 |

属性说明

| 名称 | 类型 | 作用 |
| :--- | :--- | :--- |
| value() | 类 | 指定抛出异常类，异常类需要拥有字符串构造方法，默认为 `IllegalArgumentException` 。 |
| message() | 字符串 | 修改异常信息头。 |

```
public class CaseThrow {

    public boolean hasReturn(@Throw @NotNull String name,
                             @Throw(NumberFormatException.class) @NumberRule(min = "0.0") double price,
                             boolean putaway) {
        System.out.println("hasReturn");
        return true;
    }

    public void nonReturn(@Throw(value = IllegalStateException.class, message = "unknown error") Product product,
                          @Throw(message = "price error") @SizeRule(min = 0, max = 20) List<String> param) {
        System.out.println("nonReturn");
    }
}
```

经过编译后的方法如下

```
public boolean hasReturn(String name, double price, boolean putaway) {
    if (name == null) {
        throw new IllegalArgumentException("Invalid input parameter, cause name is null");
    } else if (price < 0.0D) {
        throw new NumberFormatException("Invalid input parameter, cause price less than 0.0");
    } else {
        System.out.println("hasReturn");
        return true;
    }
}

public void nonReturn(Product product, List<String> param) {
    if (product == null) {
        throw new IllegalStateException("unknown error, cause product is null");
    } else {
        String mvar_0 = product.invalid0();
        if (mvar_0 != null) {
            throw new IllegalStateException("unknown error, cause " + mvar_0);
        } else if (param == null) {
            throw new IllegalArgumentException("price error, cause param is null");
        } else {
            int var$3 = param.size();
            if (var$3 > 20) {
                throw new IllegalArgumentException("price error, cause param.size() great than 20");
            } else if (var$3 < 0) {
                throw new IllegalArgumentException("price error, cause param.size() less than 0");
            } else {
                System.out.println("nonReturn");
            }
        }
    }
}
```


#### @Return

| 作用域 | 用途 |
| :---- | :----- |
| 非静态方法、方法参数 | 指定参数校验失败时返回数据。 |

属性说明

| 名称 | 类型 | 作用 |
| :--- | :--- | :--- |
| type() | 类 | 指定返回数据的类型，需为方法返回类型或子类或实现类。 |
| staticMethod() | 字符串 | 指定使用静态方法构造返回值。 |
| value() | 字符串数组 | 设置返回值，当返回类型为对象时需要有对应构造函数。 |

```
public class CaseReturn {

    public boolean returnPrimitive(@Return("false") @NotNull String name,
                                   @Return("true") @NumberRule(min = "0.0") double price,
                                   boolean putaway) {
        System.out.println("returnPrimitive");
        return true;
    }

    public Integer returnBasic(@Return("0") Product product,
                               @Throw(message = "null") @SizeRule(min = 0) List<String> param) {
        System.out.println("returnBasic");
        return -1;
    }

    public Capacity returnObject(@Return({"test", "true"})  @NotNull String name,
                                 @Return @NumberRule(min = "0") Byte type) {
        System.out.println("returnObject");
        return new Capacity();
    }

    public Product returnInterface(@Return(type = Item.class) @NotBlank String name) {
        System.out.println("returnInterface");
        return null;
    }

    public Product useStaticMethod(@Return(type = CaseReturn.class, staticMethod = "getProduct") @SizeRule(min = 1) String name,
                                   @Return(value = "test", type = CaseReturn.class, staticMethod = "getProduct") @NotNull Integer id) {
        System.out.println("useStaticMethod");
        return null;
    }

    public static Product getProduct() {
        return new Item();
    }

    public static Product getProduct(String name) {
        return new Item(name);
    }

    static class Item implements Product {

        ...
    }
}
```

经过编译后的方法如下

```
public boolean returnPrimitive(String name, double price, boolean putaway) {
    if (name == null) {
        return false;
    } else if (price < 0.0D) {
        return true;
    } else {
        System.out.println("returnPrimitive");
        return true;
    }
}

public Integer returnBasic(Product product, List<String> param) {
    if (product == null) {
        return 0;
    } else {
        String mvar_0 = product.invalid0();
        if (mvar_0 != null) {
            return 0;
        } else if (param == null) {
            throw new IllegalArgumentException("null, cause param is null");
        } else if (param.size() < 0) {
            throw new IllegalArgumentException("null, cause param.size() less than 0");
        } else {
            System.out.println("returnBasic");
            return -1;
        }
    }
}

public Capacity returnObject(String name, Byte type) {
    if (name == null) {
        return new Capacity("test", true);
    } else if (type == null) {
        return new Capacity();
    } else if (type < 0) {
        return new Capacity();
    } else {
        System.out.println("returnObject");
        return new Capacity();
    }
}

public Product returnInterface(String name) {
    if (name == null) {
        return new CaseReturn.Item();
    } else if (Param.isBlank(name)) {
        return new CaseReturn.Item();
    } else {
        System.out.println("returnInterface");
        return null;
    }
}

public Product useStaticMethod(String name, Integer id) {
    if (name == null) {
        return getProduct();
    } else if (name.length() < 1) {
        return getProduct();
    } else if (id == null) {
        return getProduct("test");
    } else {
        System.out.println("useStaticMethod");
        return null;
    }
}
```

#### 混合使用
```
public class CaseCombine {

    public boolean returnPrimitive(@Return("false") Product product,
                                   @Throw Counter price) {
        System.out.println("returnPrimitive");
        return true;
    }

    public Product returnObject(@Throw Person person,
                                @Return(type = CaseReturn.Item.class) @NotBlank String name,
                                @Return(type = CaseReturn.class, staticMethod = "getProduct") Capacity capacity) {
        System.out.println("returnPrimitive");
        return null;
    }
}
```

经过编译后方法的如下

```
public boolean returnPrimitive(Product product, Counter price) {
    if (product == null) {
        return false;
    } else {
        String mvar_0 = product.invalid0();
        if (mvar_0 != null) {
            return false;
        } else if (price == null) {
            throw new IllegalArgumentException("Invalid input parameter, cause price is null");
        } else {
            mvar_0 = price.invalid0();
            if (mvar_0 != null) {
                throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
            } else {
                System.out.println("returnPrimitive");
                return true;
            }
        }
    }
}

public Product returnObject(Person person, String name, Capacity capacity) {
    if (person == null) {
        throw new IllegalArgumentException("Invalid input parameter, cause person is null");
    } else {
        String mvar_0 = person.invalid0();
        if (mvar_0 != null) {
            throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
        } else if (name == null) {
            return new Item();
        } else if (Param.isBlank(name)) {
            return new Item();
        } else if (capacity == null) {
            return CaseReturn.getProduct();
        } else {
            mvar_0 = capacity.invalid0();
            if (mvar_0 != null) {
                return CaseReturn.getProduct();
            } else {
                System.out.println("returnPrimitive");
                return null;
            }
        }
    }
}
```

#### 继承使用

```
@Throw
public class CaseInherit {

    public boolean customRule(Product product, Capacity capacity) {
        System.out.println("customRule");
        return true;
    }

    @Return({"test", "true"})
    public Capacity useReturn(@NotBlank String name,
                              @Throw Counter counter) {
        System.out.println("useReturn");
        return null;
    }

    @Variable("tmp0")
    @Throw(value = UnsupportedOperationException.class)
    public void excludeParam(@Exclusive Product product,
                             @SizeRule(min = 5) int[] ids) {
        System.out.println("excludeParam");
    }

    @Exclusive
    public int excludeMethod(@Return("-1") Product product) {
        return 0;
    }
}
```

经过编译后方法的如下

```
public boolean customRule(Product product, Capacity capacity) {
    if (product == null) {
        throw new IllegalArgumentException("Invalid input parameter, cause product is null");
    } else {
        String mvar_0 = product.invalid0();
        if (mvar_0 != null) {
            throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
        } else if (capacity == null) {
            throw new IllegalArgumentException("Invalid input parameter, cause capacity is null");
        } else {
            mvar_0 = capacity.invalid0();
            if (mvar_0 != null) {
                throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
            } else {
                System.out.println("customRule");
                return true;
            }
        }
    }
}

public Capacity useReturn(String name, Counter counter) {
    if (name == null) {
        return new Capacity("test", true);
    } else if (Param.isBlank(name)) {
        return new Capacity("test", true);
    } else if (counter == null) {
        throw new IllegalArgumentException("Invalid input parameter, cause counter is null");
    } else {
        String mvar_0 = counter.invalid0();
        if (mvar_0 != null) {
            throw new IllegalArgumentException("Invalid input parameter, cause " + mvar_0);
        } else {
            System.out.println("useReturn");
            return null;
        }
    }
}

public void excludeParam(Product product, int[] ids) {
    if (ids == null) {
        throw new UnsupportedOperationException("Invalid input parameter, cause ids is null");
    } else if (ids.length < 5) {
        throw new UnsupportedOperationException("Invalid input parameter, cause ids.length less than 5");
    } else {
        System.out.println("excludeParam");
    }
}

public int excludeMethod(Product product) {
    return 0;
}
```


