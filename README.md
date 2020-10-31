# sharedfriend

代码编写以及运行环境：vscode（本地）+BDKIT

运行web与output截图：
![web](https://github.com/threethousanddrops/sharedfriend/blob/main/images/web.png)
![output](https://github.com/threethousanddrops/sharedfriend/blob/main/images/output.png)


实验设计思路：
此题旨在求两人之间的共同好友，原信息是<人，该人的所有好友>，想法是将指定两个人的好友进行比较，得到共同的好友。
①map阶段，将key设置为<[><personi,	personj><]>的形式，value依照输入不变，将输入整理为person对与好友的形式，由于好友是互相的，则会产生两个相同的key，如[100,200]，而value的值分别是100的好友、200的好友。
②reduce阶段，此时value有两个值，分别是key值中两个person各自的好友，建立FindCommon函数，找出两个好友list中相同的部分，也就是两个人的共同好友进行输出。


实验操作过程遇到的问题以及解决：
①最开始的思路只是建立在比较两个人的共同好友上，利用string的判断寻找共同的字符串（好友），讲person当作key，person拥有的好友作为value，类比wordcount的实现，可以将要求共同好友的两个人设置为相同的key值，然后在reduce里将一个key对应的两个value值进行比较寻找，只需要一个job就可以完成工作。
②在调整输出格式的时候遇到了和wordcount过程中一样的问题，最后将key的类型设置为text，在setmap的key值的时候就将符号预先设置，这样不影响key值的mapreduc过程，同时又能实现标准格式的输出。
③reduce过程中直接获取两个value的值，然后通过FindCommon进行相同字符串（也就是相同好友）的遍历搜索过程，如果没有相同字符串则返回null即没有共同好友。
④和wordcount一样，在设置输出路径时预先检查输出文件夹是否已经存在，若已经存在则删掉后重新建立，以免在测试过程中建立过多的output文件夹占用内存。

实验改进设想：
在实验设计过程中有想过建立两个job，进行两次mapreduce，首先求出每一个人都是哪些人的共同好友，再把这些有共同好友的人作为key,其好友作为value输出，操作过程中遇到困难于是用了较为简便的方法直接比较两个人的好友找到相同部分的方法，下一步可以考虑利用两次mapreduce实现共同好友的查找。
