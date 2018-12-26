# 方法校验器

自定义注解处理器，在 `编译期` 对语法树做修改，增加方法入参校验逻辑。

支持对 基础类型及其包装类、String、数组、集合 的属性校验。

版本要求 JDK6 以上

## 如何使用

通过定义注解实现校验规则，即可对方法开启校验逻辑

| 注解 | 作用域 | 效果 |
| :---- | :----- | :---- |
| cn.moyada.method.validator.annotation.Rule | 类属性 | 设置类属性的校验规则 |
| cn.moyada.method.validator.annotation.Verify | 普通方法 | 开启方法校验逻辑 |
| cn.moyada.method.validator.annotation.Check | 方法参数 | 设置参数的校验逻辑 |

注解内部属性说明

| 属性 | 作用 |
| :--- | :--- |
| Rule.nullable() | 是否允许参数为空，primitive 类型无效 |
| Rule.min() | 设置数字类型属性的最小允许数值 |
| Rule.max() | 设置数字类型属性的最大允许数值 |
| Rule.maxLength() | 设置 String、数组、集合 类型属性的最大允许长度或容量 |
| Check.invalid() | 参数校验失败时抛出异常类，需要拥有字符串构造方法 |
| Check.message() | 异常信息头 |
| Check.nullable() | 参数是否可为空 |


<span id="示例">示例</span>

```
public class Service {

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


### 普通工程

1. 创建处理器 jar 包

进入工程主目录，执行命令创建 jar 包。或者[下载](https://github.com/moyada/method-validator/releases)已创建 jar 包使用。

```
target_dir=$(pwd)/target

if [ ! -d $target_dir ];then
mkdir $target_dir
fi

meta_dir=$(find . -name "META-INF") 
cp -R $meta_dir $target_dir

cd src/main/java

javac -proc:none -cp $JAVA_HOME/lib/tools.jar -d $target_dir $(find . -name "*.java")

cd $target_dir

jar cvf method-validator.jar .
```

2. 编译目标源文件

```
javac -cp method-validator.jar MyApp.java
```

### Maven 工程

1. 安装配置 Maven 依赖

在源码工程中编译安装校验器依赖 `mvn clean install` ，或者[下载](https://github.com/moyada/method-validator/releases)至本地引用

2. 在目标工程中需配置校验器依赖

```
<dependencies>
    <dependency>
        <groupId>cn.moyada</groupId>
        <artifactId>method-validator</artifactId>
        <version>1.0-SNAPSHOT</version>
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

3. 执行 `mvn clean compile` 进行编译

## 编译后逻辑

如示例 [Service.go](#示例) 方法，经过编译后内容将会为

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