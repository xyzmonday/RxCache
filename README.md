# RxCache
这是一个基于RxJava2的三级缓存框架。2016年RxJava2发布后，查阅了大量的资料，总结了RxJava2的基本用法（可以见鄙人的RxJava2Demo项目）。那为什么要写这么一个项目了。
因为在学习Android基础的时候，刚开始接触的是Bitmap的缓存，然后是一些缓存框架。我推进一个我系统研究过的缓存框架:<br/>

> https://github.com/Trinea/android-common <br/>

以及一些基于RxJava1.x或者RxJava2.x的缓存框架:<br/>
> https://github.com/LittleFriendsGroup/KakaCache-RxJava  <br/>
> https://github.com/VictorAlbertos/RxCache <br/>

上面两个缓存框架，阅读起来比较困难，我只是阅读了他们的关键代码。那么一直在构想一套比较简单的基于RxJava2的框架，首先列出查阅的文章:

> http://www.jianshu.com/p/ab70e9286b8b <br/>
> http://blog.csdn.net/aishang5wpj/article/details/51692824 <br/>
> http://blog.csdn.net/qq_35064774/article/details/53449795 <br/>
> http://www.jianshu.com/p/1e9e8f4213f3 <br/>
> http://blog.csdn.net/dd864140130/article/details/52714272 <br/>


## RxCache设计图

 ![image](https://github.com/xyzmonday/RxCache/raw/master/screenshots/pic1.png)
