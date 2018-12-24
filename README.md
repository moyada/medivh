# 方法校验生成器

自定义注解处理器，在编译时期对语法树做修改，增加方法入参校验逻辑。

目前支持版本为 JDK6 ~ JDK8

## 如何使用

1. 安装配置 Maven 依赖

编译安装对应版本依赖 `mvn clean install [-P jdk(6|7|8)]` 

```
<dependencies>
    <dependency>
        <groupId>cn.moyada</groupId>
        <artifactId>function-validator</artifactId>
        <version>1.0-SNAPSHOT</version>
        <classifier>jdk8</classifier>
        <scope>provided</scope>
    </dependency>
<dependencies/>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>1.8</source>
                <target>1.8</target>
                <showWarnings>true</showWarnings>
            </configuration>
        </plugin>
    </plugins>
</build>
```

2. 在类上定义校验规则，于使用该类为入参方法上开启校验逻辑，编译后执行即可查看效果。

| 注解 | 作用域 | 效果 |
| :---- | :----- | :---- |
| cn.moyada.function.validator.annotation.Rule | 类属性 | 设置类属性的校验规则 |
| cn.moyada.function.validator.annotation.Validation | 普通方法 | 开启方法校验逻辑 |
| cn.moyada.function.validator.annotation.Check | 方法参数 | 设置参数的校验逻辑 |

例如

```
public class Service {

    @Validation
    public void go(@Check(invalid = RuntimeException.class) Args args,
                    @Check(message = "something error", nullable = true) Info info,
                    @Check String name,
                    int num) {
        String fad = null;
        System.out.println(args);
        System.out.println(info);
        System.out.println(name);
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