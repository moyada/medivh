# 方法校验生成器

自定义注解处理器，在编译时期对语法树做修改，增加方法入参校验逻辑。

目前支持版本为 JDK7 以上

## 如何使用

1. 安装配置 Maven 依赖

源代码工程中编译安装对应版本依赖 `mvn clean install [-P jdk7]` ，或者[下载](https://github.com/moyada/function-validator/releases)至本地使用 

2. 配置对应编译器版本的校验器依赖

```
<dependencies>
    <dependency>
        <groupId>cn.moyada</groupId>
        <artifactId>function-validator</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>provided</scope>
        <classifier>jdk7</classifier> // 如果jdk版本为 1.7.xx
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

* java9 及以上

```
<dependencies>
    <dependency>
        <groupId>cn.moyada</groupId>
        <artifactId>function-validator</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>provided</scope>
        <exclusions>
            <exclusion>
                <groupId>com.sun</groupId>
                <artifactId>tools</artifactId>
            </exclusion>
        </exclusions>
    </dependency>
</dependencies>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>${java.version}</source>
                <target>${java.version}</target>
                <showWarnings>true</showWarnings>
                <fork>true</fork>
            </configuration>
        </plugin>
    </plugins>
</build>
```

3. 定义校验规则，对方法开启校验逻辑

| 注解 | 作用域 | 效果 |
| :---- | :----- | :---- |
| cn.moyada.function.validator.annotation.Rule | 类属性 | 设置类属性的校验规则 |
| cn.moyada.function.validator.annotation.Validation | 普通方法 | 开启方法校验逻辑 |
| cn.moyada.function.validator.annotation.Check | 方法参数 | 设置参数的校验逻辑 |

示例

```
public class Service {

    @Validation
    public void go(@Check(invalid = RuntimeException.class) Args args,
                    @Check(message = "something error", nullable = true) Info info,
                    @Check String name,
                    int num) {
        // process
        ...
    }

    class Args {

        @Rule(maxLength = 20)
        String name;

        @Rule(maxLength = 5, nullable = true)
        String type;

        @Rule(min = 40, max = 200)
        int value;
    }

    class Info {

        @Rule(maxLength = 20)
        String type;

        @Rule(min = -250, max = 500, nullable = true)
        Double price;
    }
}
```

3. 执行 `mvn clean compile`

查看编译后 class 文件反编译结果为

```
public void go(Args args, Info info, String name, int num) {
    if (args == null) {
        throw new RuntimeException("invalid argument while attempting to access com.moyada.permission.ProcessorTest.say(), cuz ".concat("args is null"));
    } else {
        String _MSG = args.invalid0();
        if (_MSG != null) {
            throw new RuntimeException("invalid argument while attempting to access com.moyada.permission.ProcessorTest.say(), cuz ".concat(_MSG));
        } else {
            if (info != null) {
                _MSG = info.invalid0();
                if (_MSG != null) {
                    throw new IllegalArgumentException("something error while attempting to access com.moyada.permission.ProcessorTest.say(), cuz ".concat(_MSG));
                }
            }

            if (name == null) {
                throw new IllegalArgumentException("invalid argument while attempting to access com.moyada.permission.ProcessorTest.say(), cuz ".concat("name is null"));
            } else {
                // process
                ...
            }
        }
    }
}
``` 