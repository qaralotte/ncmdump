# ncmdump - 网易云音乐 NCM 转换

## 如何使用

在终端输入

``` bash
java -jar ncmdump.jar xx.ncm
```

结果文件: xx.flac (或者 xx.mp3)  
其中 xx.ncm 是**要转换的文件**  
文件输出的路径同输入的路径 (见输出信息)

## 用到的工具库
* [alibaba/fastjson2](https://github.com/alibaba/fastjson2)
* [ijabz/jaudiotagger](https://bitbucket.org/ijabz/jaudiotagger/src/master/)

## 参考

[anonymous5l/ncmdump](https://github.com/anonymous5l/ncmdump) - 原版 ncmdump  
[(简书)网易云音乐ncm编解码探究记录](https://www.jianshu.com/p/ec5977ef383a) - ncm 文件解析   
