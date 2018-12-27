# Medivh

[![Build Status](https://travis-ci.org/moyada/medivh.svg?branch=master)](https://travis-ci.org/moyada/medivh)
![version](https://img.shields.io/badge/java-%3E%3D6-red.svg)
![java lifecycle](https://img.shields.io/badge/java%20lifecycle-compile-lightgrey.svg)
[![maven](https://img.shields.io/badge/maven%20central-0.1.1-green.svg)](https://search.maven.org/search?q=g:io.github.moyada%20AND%20a:medivh)
[![license](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/moyada/medivh/blob/master/LICENSE)
 
自定义注解处理器，通过在 `编译期` 对语法树进行修改，达到增加方法入参校验逻辑的功能。

目前支持校验的属性有 基础类型及其包装类、String、数组、集合。

支持 JDK6 以上版本。

## 使用说明

通过简单的定义注解实现校验规则，即可对方法开启校验逻辑

| 注解 | 作用域 | 效果 |
| :---- | :----- | :---- |
| io.moyada.medivh.annotation.Rule | 类属性 | 设置类属性的校验规则 |
| io.moyada.medivh.annotation.Verify | 普通方法 | 开启方法校验逻辑 |
| io.moyada.medivh.annotation.Check | 方法参数 | 设置参数的校验逻辑 |

#### 注解属性说明

| 属性 | 作用 |
| :--- | :--- |
| Rule.nullable() | 是否允许参数为空，primitive 类型无效 |
| Rule.min() | 设置数字类型属性的最小允许数值 |
| Rule.max() | 设置数字类型属性的最大允许数值 |
| Rule.maxLength() | 设置 String、数组、集合 类型属性的最大允许长度或容量 |
| Check.invalid() | 参数校验失败时抛出异常类，需要拥有字符串构造方法 |
| Check.message() | 异常信息头 |
| Check.nullable() | 参数是否可为空 |

#### 示例 [编译结果](#编译后逻辑)

```
public class MyApp {

    @Verify
    public void go(@Check(invalid = RuntimeException.class) Args args,
                    @Check(message = "something error", nullable = true) Info info,
                    @Check String name,
                    int num) {
        // process
        ...
    }

    class Args {

        @Rule(min = 10, max = 2000)
        int id;

        @Rule
        HashMap<String, Object> param;

        @Rule(maxLength = 5, nullable = true)
        String[] value;
    }

    class Info {

        @Rule(maxLength = 20)
        String name;

        @Rule(min = -250, max = 500, nullable = true)
        Double price;
        
        @Rule(maxLength = 10)
        List<String> extra;
    }
}
```

### Maven 工程

1. 在目标工程中需配置校验器依赖

```
<dependencies>
    <dependency>
        <groupId>io.github.moyada</groupId>
        <artifactId>medivh</artifactId>
        <version>0.1.1</version>
        <scope>provided</scope>
    </dependency>
<dependencies/>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <showWarnings>true</showWarnings>
            </configuration>
        </plugin>
    </plugins>
</build>
```

2. 执行 `mvn clean compile` 进行编译

### 普通工程

1. 创建处理器 jar 包

进入工程主目录，执行命令创建 jar 包。或者[下载](https://github.com/moyada/medivh/releases)已创建的 jar 包使用。

```
git clone git@github.com:moyada/medivh.git
cd medivh

target_dir=$PWD/target

if [ ! -d $target_dir ];then
mkdir $target_dir
fi

meta_dir=$(find . -name "META-INF") 
cp -R $meta_dir $target_dir

cd src/main/java

javac -proc:none -cp $JAVA_HOME/lib/tools.jar -d $target_dir $(find . -name "*.java")

cd $target_dir

jar cvf medivh.jar .
```

2. 编译目标源文件

```
javac -cp medivh.jar MyApp.java
```

#### 编译后逻辑

如[示例](#示例)中的方法，经过编译后的内容将会如下所示，并为配置属性规则的类生成校验方法

```
public void go(Args args, Info info, String name, int num) {
    if (args == null) {
        throw new RuntimeException("invalid argument while attempting to access io.moyada.MyApp.go(), because ".concat("args is null"));
    } else {
        String _MSG = args.invalid0();
        if (_MSG != null) {
            throw new RuntimeException("invalid argument while attempting to access io.moyada.MyApp.go(), because ".concat(_MSG));
        } else {
            if (info != null) {
                _MSG = info.invalid0();
                if (_MSG != null) {
                    throw new IllegalArgumentException("something error while attempting to access io.moyada.MyApp.go(), because ".concat(_MSG));
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