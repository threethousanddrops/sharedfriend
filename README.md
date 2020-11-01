# sharedfriend

代码编写以及运行环境：vscode（本地）+BDKIT

运行web与output截图：
![web](https://github.com/threethousanddrops/sharedfriend/blob/main/images/web.png)
![web](https://github.com/threethousanddrops/sharedfriend/blob/main/images/output.png)



实验设计思路：
此题要求求两人之间的共同好友，原信息是<人，该人的好友>，想法是设计两次mapreduce过程，得到共同的好友。
① 第一个mapreduce求出每一个人都是哪些人的共同好友，输出的key为person，输出的value为拥有此人作为好友的人。比如输出就有：100作为key值，200,300,400,500,600作为value值，代表200,300,400,500,600都有共同好友100.
② 第二个mapreduce通过对于value值两重循环的遍历，把每两个拥有共同好友的人作为key,第一个mapreduce传入的key值也就是他们的共同好友作为value输出，输出格式为（例如：）（[100,200]，[300,400]），代表100和200拥有共同好友300和400.


实验操作过程遇到的问题以及解决：
①最开始的思路是建立在比较两个人的共同好友上，利用string的判断寻找共同的字符串（好友），讲person当作key，person拥有的好友作为value，类比wordcount的实现，可以将要求共同好友的两个人设置为相同的key值，然后在reduce里将一个key对应的两个value值进行比较寻找，只需要一个job就可以完成工作。而reduce过程中直接获取两个value的值，然后通过FindCommon进行相同字符串（也就是相同好友）的遍历搜索过程，如果没有相同字符串则返回null即没有共同好友。在实验之后确实可以输出共同好友，但是经过检查发现存在盲区，如果A和B不是好友，那么就无法找出他们的共同好友，运行截图如下：
![web](https://github.com/threethousanddrops/sharedfriend/blob/main/images/falseoutput.png)

错误原因在于input传入mapreduce的时候针对每一行的人（也就是每一个person），只能与他的好友产生person对，进而比较他们有无共同好友（即有无公共字符串），但是若两个人不是好友，则无法产生key值对，经过再设一个mapreduce工作或者遍历所有的key值进行比较的试验之后没能找到完美解决此问题的办法，只能转换思路重新开始。
②用新的思路实验过程中，在调整输出格式的时候遇到了和wordcount过程中一样的问题，最后将key的类型设置为text，在context.write过程中将符号格式和outvalue直接设置为key，这样不影响key值的mapreduce过程，同时又能实现标准格式的输出。
③和wordcount一样，在设置输出路径时预先检查输出文件夹是否已经存在，若已经存在则删掉后重新建立，以免在测试过程中建立过多的output文件夹占用内存。
④第一个mapreduce的输出路径问题上，最初的想法是建立一个临时文件夹直接传入，通过命令行指定临时文件夹位置或者预先设立一个tmpdir，但是在delete过程中遇到了问题，最后直接在代码中设置绝对路径指定临时输出文件夹，使得第一个mapreduce的输出可以在此处紧接着被第二个mapreduce读入，第二个mapreduce也结束之后直接删除临时文件夹，命令行也不用考虑临时文件夹的路径问题。
⑤最初输出的outputkey和value的值都是对应且数量正确的，但没有按照顺序输出，显得非常凌乱，最开始的考虑是在第二个job的reduce里对于key的person对进行排序处理，但是没能成功实现，后来发现可以直接在第二个mapreduce工作的map过程中对于传入的value值进行排序，排序后再通过双重循环生成person对输出，可以达到按序输出的效果。

