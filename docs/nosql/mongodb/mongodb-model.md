# MongoDB 建模

MongoDB 的数据模式是一种灵活模式，关系型数据库要求你在插入数据之前必须先定义好一个表的模式结构，而 MongoDB 的集合则并不限制 document 结构。这种灵活性让对象和数据库文档之间的映射变得很容易。即使数据记录之间有很大的变化，每个文档也可以很好的映射到各条不同的记录。 当然在实际使用中，同一个集合中的文档往往都有一个比较类似的结构。

数据模型设计中最具挑战性的是在应用程序需求，数据库引擎性能要求和数据读写模式之间做权衡考量。当设计数据模型的时候，一定要考虑应用程序对数据的使用模式（如查询，更新和处理）以及数据本身的天然结构。

## MongoDB 数据建模入门

> 参考：https://docs.mongodb.com/guides/server/introduction/#what-you-ll-need

### （一）定义数据集

当需要建立数据存储时，您的首要任务是思考以下问题：我想存储哪些数据？这些字段之间如何关联？

假设这样一个场景：我们需要建立数据库以跟踪物料及其数量，大小，标签和等级。

如果是存储在 RDBMS，可能以下的数据表：

| name     | quantity | size        | status | tags                     | rating |
| :------- | :------- | :---------- | :----- | :----------------------- | :----- |
| journal  | 25       | 14x21,cm    | A      | brown, lined             | 9      |
| notebook | 50       | 8.5x11,in   | A      | college-ruled,perforated | 8      |
| paper    | 100      | 8.5x11,in   | D      | watercolor               | 10     |
| planner  | 75       | 22.85x30,cm | D      | 2019                     | 10     |
| postcard | 45       | 10x,cm      | D      | double-sided,white       | 2      |

### （二）思考 JSON 结构

从上例中可以看出，表似乎是存储数据的好地方，但该数据集中的字段需要多个值，如果在单个列中建模，则不容易搜索或显示（对于 例如–大小和标签）。

在SQL数据库中，您可以通过创建关系表来解决此问题。

在MongoDB中，数据存储为文档。 这些文档以JSON（JavaScript对象表示法）格式存储在MongoDB中。 JSON文档支持嵌入式字段，因此相关数据和数据列表可以与文档一起存储，而不是与外部表一起存储。

JSON格式为键/值对。 在JSON文档中，字段名和值用冒号分隔，字段名和值对用逗号分隔，并且字段集封装在“大括号”（`{}`）中。

如果要开始对上面的行之一进行建模，例如此行：

| name     | quantity | size      | status | tags                     | rating |
| :------- | :------- | :-------- | :----- | :----------------------- | :----- |
| notebook | 50       | 8.5x11,in | A      | college-ruled,perforated | 8      |

您可以从name和quantity字段开始。 在JSON中，这些字段如下所示：

```json
{"name": "notebook", "qty": 50}
```

### （三）确定哪些字段作为嵌入式数据

接下来，需要确定哪些字段可能需要多个值。可以考虑将这些字段作为嵌入式文档或嵌入式文档中的 列表/数组 对象。

例如，在上面的示例中，size 可能包含三个字段：

```json
{ "h": 11, "w": 8.5, "uom": "in" }
```

And some items have multiple ratings, so `ratings` might be represented as a list of documents containing the field `scores`:

```json
[ { "score": 8 }, { "score": 9 } ]
```

And you might need to handle multiple tags per item. So you might store them in a list too.

```json
[ "college-ruled", "perforated" ]
```

Finally, a JSON document that stores an inventory item might look like this:

```json
{
 "name": "notebook",
 "qty": 50,
 "rating": [ { "score": 8 }, { "score": 9 } ],
 "size": { "height": 11, "width": 8.5, "unit": "in" },
 "status": "A",
 "tags": [ "college-ruled", "perforated"]
}
```

This looks very different from the tabular data structure you started with in Step 1.

## 文档结构

设计基于 MongoDB 的应用程序的数据模型时的关键就是选择合适的文档结构以及确定应用程序如何描述数据之间的关系。有两种方式可以用来描述这些关系：引用及内嵌。

### References - 引用

引用方式通过存储链接或者引用信息来实现两个不同文档之间的关联。应用程序可以通过解析这些数据库引用来访问相关数据。简单来讲，这就是规范化的数据模型。

![Data model using references to link documents. Both the ``contact`` document and the ``access`` document contain a reference to the ``user`` document.](https://mongoing.com/docs/_images/data-model-normalized.png)

### Embedded Data - 内嵌

内嵌方式指的是把相关联的数据保存在同一个文档结构之内。MongoDB的文档结构允许一个字段或者一个数组内的值为一个嵌套的文档。这种冗余的数据模型可以让应用程序在一个数据库操作内完成对相关数据的读取或修改。

![Data model with embedded fields that contain all related information.](https://mongoing.com/docs/_images/data-model-denormalized.png)

## 参考资料

- **官方**
  - [MongoDB 官网](https://www.mongodb.com/)
  - [MongoDBGithub](https://github.com/mongodb/mongo)
  - [MongoDB 官方免费教程](https://university.mongodb.com/)
- **教程**
  - [MongoDB 教程](https://www.runoob.com/mongodb/mongodb-tutorial.html)
  - [MongoDB 高手课](https://time.geekbang.org/course/intro/100040001)
