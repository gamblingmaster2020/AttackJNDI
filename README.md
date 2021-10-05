## 工具介绍
当遇到JNDI注入的时候，如果靶机JDK版本过高，此时由于JDK的codebase限制，无法进行JNDI注入。

利用本工具，可以无视JDK版本进行JNDI注入，但是需要靶机存在可以利用的Gadget。

用法示例：

```
java -jar AttackJNDI.jar 8585 CommonsBeanutils1 calc
```

## 技术原理
JNDI反序列化过程中，使用javaSerializedData直接传递恶意序列化数据，而不是使用Reference Gadget，从而无视urlcodebase；

## 工具依赖包
- marshalsec
- ysoserial