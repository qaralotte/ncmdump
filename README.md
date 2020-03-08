## ncmdump - 网易云音乐NCM转换

* #### 用法

在终端输入

```bash
javac -d Executor.java
java io.qaralotte.ncmdump.Executor a.ncm b.ncm
```

其中a.ncm和b.ncm是要转换的文件  
输出文件的路径是和输入的一样

* #### 所需的工具库

因为java本身没有处理json的类，所以我用了[google/gson](https://github.com/google/gson)

* #### 参考

[**liwenbo-gh/ncmdump**](https://github.com/liwenbo-gh/ncmdump) - 我参考的同样用java实现的ncmdump  
[**anonymous5l/ncmdump**](https://github.com/anonymous5l/ncmdump) - 原版ncmdump  
[**(简书)网易云音乐ncm编解码探究记录**](https://www.jianshu.com/p/ec5977ef383a) - ncmdump文件解析
