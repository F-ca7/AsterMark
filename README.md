为研究与解决关系型数据库中的数据版权问题，我&Kun设计并开发了一款名为“AsterMark”、用于嵌入/提取关系型数据库水印的桌面应用软件。（获得[第八届中国软件杯一等奖](http://www.cnsoftbei.com/plus/view.php?aid=452)）

1. 通过参考相关学术文献，对于数值型、文本型两大类型数据实现了共五种水印嵌入提取算法；
2. 实现了LSB(最低有效位)修改算法，保证了数据可用性；
3. 实现了基于最优化的水印算法，使得水印难以被人为剔除；
4. 实现了空格嵌入、符号修改算法，在保证文本内容不变的情况下嵌入水印；
5. 实现了基于词性逆序数的文本修改算法，保证文本语义不变，同时使得水印难以被察觉；
6. 适配当前主流关系型数据库，包括：Mysql、PostgreSql、Sql
   Server。

## 核心算法介绍

---------------

最优化算法

算法分为水印信息嵌入算法与水印信息提取算法。

水印嵌入算法

流程如下：

![](https://s2.ax1x.com/2019/09/03/nkS3PU.png)

Graph1

算法描述：

数据集D通过水印嵌入函数映射成被添加水印数据集Dw，其中，水印嵌入函数需要提供一个只有数据分享者才有资格拥有的密钥Ks，以及唯一标识这个数据集的水印信息。以上的嵌入还受约束条件G的影响。

### 水印嵌入过程

**第一步：数据集划分。**根据密钥Ks与指定的主键，使用Hash算法得到被Hash离散的主键值，根据模取的结果进行分组，数据集D被划分成m个分组。伪代码如下：

**Algorithm: get_partitions**

Input: Data set **D**, Secret Key **Ks**, Number of partitions **m**, Key of the
tuple **p**

Output: Partitions **S0,...,Sm-1**

for i from 0 to m-1
**Si** = {}
for each Tuple **r** ∈ D,
partition(**r**) = H(**Ks**\|\|H(**r.P**\|\|**Ks**)) mod **m**
insert **r** into S_partition(**r**)
return **S0,...,Sm-1**

其中，Hash算法的选取决定了分组中数据元组的散列情况。由于被散列值是主键与密钥的拼接，类型多为字符串，这里我们使用散列效果较好的BKDRHash散列函数。

**第二步：水印嵌入。**在不影响数据可用性的前提下，通过调整分组中每一个元组数据，将水印的一位嵌入到该分组中。

一位水印嵌入：对于给定的含有m个数据元组的分组Si，视其为m维向量。找出一个m维向量△i，与Si向量相加，获得Sim向量，此向量可被认为是被添加水印后的分组Sim。

其中△i向量需要根据约束条件集G去寻找。在此，定义隐藏函数，其中，Si为已知量，△i为变量。当待嵌入水印位为1时，求该函数最大值时，△i对应的值；当待嵌入水印位为0时，求该函数的最小值时，△i对应的值。最后根据所求结果，对原数据进行修改。伪代码如下：

**Algorithm: encode_single_bit**

Input: Partition **Si**, Bit **bi**, Constraints set **Gi**, Secret parameters
set **γ**，Statistics **Xmax**, **Xmin**, Minimum member number **ξ**

Output: Data set **Si+Δi\***


```
if ( |Si|\<ξ ) then return Si
if ( bi==1 ) then
maximize( Θγ(Si+Δi) ) subject to Gi
insert Θγ(Si+Δi) into Xmax
else
minimize( Θγ(Si+Δi) ) subject to Gi
insert Θγ(Si+Δi) into Xmin
return Si+Δi
```

对于隐藏函数的选取，我们使用

![](https://s2.ax1x.com/2019/09/03/nkSUq1.png)

针对该函数进行的最大最小化求解，我们使用模式搜索算法。

模式搜索算法伪代码：

**Algorithm: pattern_search**

Input: Partition **origin_partition**, Bit **bit**, Evaluation function
**sigmoid()**, Length of step **step_length**, Rate of decay **decay_rate**,
Accurate **accurate**, Number of turns **turn_num**

Output: Partition **new_partition**
while step_length \> precision,
**accurate_direction** = [0,0,0,...,0]

```
searchByAxis:
while turn < turn_num
 for each data in the Si
 new_data = origin_data + step_length
 if sigmoid(new_data) > sigmoid(origin_data) and bit == 1
 origin_data = new_data
 accurate_direction[origin_data's index] = 1
 if sigmoid(new_data) < sigmoid**(origin_data) and bit == 0
 origin_data = new_data
 accurate_direction[origin_data's index] = 1
 step_length = step_length\decay_rate
 turn++
```

```
searchByPattern
 new_partition = origin_partition + accurate\accurate_direction
 if sigmoid(new_partition) > sigmoid(origin_partition) and bit = 1
 	origin_partition = new_partition
 if sigmoid(new_partition) > sigmoid(origin_partition) andbit = 0
 	origin_partition = new_partition
```



**第三步：最优阈值求解：**

定义：解码错误概率

要求最优阈值，即求出错概率最小时，T的值。因此，当一次导函数等于0时，且二次导函数大于零时，T的值为最优阈值

通过chi-square检测，并且服从正态分布。因此，最后化简可得

![](https://s2.ax1x.com/2019/09/03/nkSrGD.png)

### 水印提取过程

第一步：根据提供的Ks密钥，对于待提取数据集，划分为m个分组。此处，算法同水印嵌入过程，故不再赘述。

第二步：根据水印嵌入过程中计算得到的最优阈值，与分组中的每个数据元组进行比较，利用投票机制，求出该元组嵌入的水印位的值。

伪代码如下：

**Algorithm: detect_watermark**

Input: Watermarked data set **Dw**, Number of partitions **m**, Secret parameter
**c**, Minimum member number **ξ**, Secret parameter set **Ks**, Thouhold
**T\***, Watermark length **l**

Output: Detected watermark **Wd**

```
ones[0,..., l-1] = [0,...,0]
zeros[0,..., l-1] = [0,...,0]
S0,...,Sm-1 = get_partitions(Dw,Ks,m)
for j from 0 to m-1
	if( |Sj| >= ξ )
    i = j mod l
    value = Θ( Sj, 0, c)
    if value >= T
    	ones[i] = ones[i] + 1
    else
    	zeros[i] = zeros[i] + 1
for j from 0 to l-1
    if ones[j] > zeros[j]
    	Wd[j]=1
    else if ones[j] < zeros[j]
    	Wd[j]=0
    else
   		Wd[j]='x'
return Wd
```

## 软件界面截图

---------------

- 连接主数据库

![](https://s2.ax1x.com/2019/11/12/MlovYq.png)

- 数据表展示
![](https://s2.ax1x.com/2019/11/12/Mloxf0.png)

- 嵌入水印
![](https://s2.ax1x.com/2019/11/12/MlTplT.png)

- 嵌入结果展示
![](https://s2.ax1x.com/2019/11/12/MlTCXF.png)

- 提取水印
![](https://s2.ax1x.com/2019/11/12/MlTk79.png)

- 提取结果展示
![](https://s2.ax1x.com/2019/11/12/Ml7P8P.png)

