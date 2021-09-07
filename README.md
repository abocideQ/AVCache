#### AVCahce
``` 
CacheProxy.Builder(context).build()
CacheProxy.proxyUrl(url)
CacheProxy.preload(url)
```
#### 奇怪的知识又增加了1
![Image text](https://github.com/ABCDQ123/AVCache/blob/main/app/image/okhtttp.png)
![Image text](https://github.com/ABCDQ123/AVCache/blob/main/app/image/recyclerview.png)
#### 奇怪的知识又增加了2
```
一. JAVA 双亲委派
双亲委派的父子并不是继承关系

1.CustomClassLoader(自定义de类加载器)

2.AppClassLoader (应用程序类加载器，加载*Java环境变量CLASSPATH所指定的路径下的类库 ，
  而CLASSPATH所指定的路径可以通过 System.getProperty("java.class.path") 获取，该变量可被覆盖)

3.ExtClassLoader(扩展类加载器，会加载 $JAVA_HOME/jre/lib/ext 的类库)

4.BootStrapClassLoader(启动类加载器，JVM启动时创建，加载$JAVA_HOME/JRE/LIB下面的类库,只加载一次，防篡改)

如果类已经加载了，就不用再加载 Class<?> c = findLoadedClass(name);
if (c==null)
CustomClassLoader 委派父加载器->AppClassLoader 委派父加载器->ExtClassLoader 委派父加载器->BootStrapClassLoader
未找到-> ExtClassLoader 未找到-> AppClassLoader 未找到->CustomClassLoader 未找到 -> Exception

二. Android 双亲委派
Android屏蔽了ClassLoader的findClass加载方法
Android有两个类加载器PathClassLoader,DexclassLoader都继承于BaseDexClassLoader

PathClassLoader 和 DexClassLoader 都能加载外部的 dex／apk
PathClassLoader：只能使用系统默认位置
DexClassLoader：可以指定 optimizedDirectory

BaseDexClassLoader 构造函数：
dexPath: 需要加载的文件列表，文件可以是包含了 classes.dex 的 JAR/APK/ZIP，也可以直接使用 classes.dex 文件，多个文件用 “:” 分割
optimizedDirectory: 存放优化后的 dex，可以为空
libraryPath: 存放需要加载的 native 库的目录
parent: 父ClassLoader
BaseDexClassLoader 的运行方式: 传入 dex 文件->优化->保存优化后的 dex 文件到 optimizedDirectory 目录。

1.热修复
使用DexClassLoader：能够加载未安装的jar/apk/dex
DexClassLoader ->DexPathlist->dexElements[]
将新的dex包与dexElement[] 合并放在数组最前面成一个新的dex,旧的dex就不会生效
通过反射将 原来的dexElements[] 替换掉，这样就可不会加载旧的带有bug的class文件。
```

