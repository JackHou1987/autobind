# 特性

#### 1. 支持复杂嵌套Javabean对象之间转换
#### 2. 支持复杂泛型对象之间转换
#### 3. 支持通用常见Java类型转换
#### 4. 转换属性提供高度可配置并精细到字段级
#### 5. 提供简单易用API
#### 6. 提供基于字节码生成本地代码功能，以提升转换性能
#### 7. 持续完善中...

# 主要模块

#### 1. Type Module: 封装了原始java.lang.relect.Type并结合Spring ResolvableType提供各种类型及泛型支持
#### 2. Converter Module: 提供了用户自定义Converter和内置Converter，支持各种类型自定义转换
#### 3. Config Module: 提供可配置能力，精细控制转换过程并通过自定义校验器及EL表达式校验提供校验功能
#### 4. BeanMapper Module: 封装JavaBean对象与JavaBean对象或者JavaBean对象与Map之间转换功能
#### 5. Performance Module: 封装了基于字节码生成native code能力





