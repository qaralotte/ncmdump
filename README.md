## ncmdump - 网易云音乐NCM转换

* #### 用法

在终端输入

```bash
java -jar ncmdump.jar a.ncm b.ncm
```

其中a.ncm和b.ncm是要转换的文件  
文件输出的路径同输入的路径

* #### 所需的工具库

处理json所需的库: [google/gson](https://github.com/google/gson)   
处理音频元信息所需的库: [ijabz/jaudiotagger](https://bitbucket.org/ijabz/jaudiotagger/src/master/)

* #### 参考

[**liwenbo-gh/ncmdump**](https://github.com/liwenbo-gh/ncmdump) - 我参考的同样用java实现的ncmdump  
[**anonymous5l/ncmdump**](https://github.com/anonymous5l/ncmdump) - 原版ncmdump  
[**(简书)网易云音乐ncm编解码探究记录**](https://www.jianshu.com/p/ec5977ef383a) - ncmdump文件解析   
