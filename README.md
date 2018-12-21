# 方法校验生成器

自定义注解处理器，在编译时期对语法树做修改，增加方法入参校验逻辑。

目前支持版本为 JDK8

## 使用方式

在类上定义校验规则，于使用该类为入参方法上开启校验逻辑，编译后执行即可查看效果。

| 注解 | 作用域 | 效果 |
| :---- | :----- | :---- |
| cn.moyada.function.validator.annotation.Rule | 类属性 | 设置类属性的校验规则 |
| cn.moyada.function.validator.annotation.Validation | 方法 | 开启方法校验逻辑 |
| cn.moyada.function.validator.annotation.Check | 方法参数 | 设置参数的校验逻辑 | 


