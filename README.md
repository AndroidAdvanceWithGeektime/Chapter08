# Chapter08

该项目展示了关闭掉虚拟机的 class verify 后对性能的影响。

开启前

![](before.png)

开启后

![](after.png)


注意
====
该例子尽量在 Dalvik 下执行，支持模拟器执行。

由于 Art 下的 verify 和 Dalivk 下的 verify 机制不一样，所以该例子在 art 下的效果并不明显