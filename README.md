## 易步易趋 ##
**易步易趋（不是错别字，我喜欢这么称呼它）** 是一个模块化的分布式的服务器端应用框架，可以作为多种服务器的后端使用，可以配置为定时推送或者即时消息发送。开发它的初衷是作为我的APP应用的服务器，同时也准备作为手游服务器的基础。

### 主要特性 ###
**模块化的设计**

不同的服务使用消息总线通信。因为使用了[Vert.x](http://vertx.io/)作为底层，所以理论上每个服务都可以用不同的语言来开发。设计的初衷是命令服务由开发者习惯的语言开发。

**灵活的部署方式**

config.json文件是应用的配置文件，里面定义了各种服务的具体实现类，实例的个数以及异步/同步工作方式等参数。根据实际需要可以对服务的实例数量等进行调整以适应具体业务的需要。
storage.json文件是应用的存储结构描述，以及初始化数据。系统可以根据配置的表结构自动创建数据库表，并添加初始化数据。根据需要可以强制重新创建数据库。

**分布式**

因为使用了[Vert.x](http://vertx.io/)所以应用本身天然就是支持分布式部署的。暂时没有提供Vert.x集群的部署方式，正在实践中。

**独有特性**

不宕机更改运行服务实例数量，不宕机增加新的命令执行服务及更改原有命令执行服务路由。

### 使用的技术 ###
- **[Vert.x](http://vertx.io/)** 一个神奇的轻量的消息总线服务，现在的稳定版本是3.0
- **[Cassandra](http://cassandra.apache.org/)** 分布式的列存储，为了满足应用数据上的扩展存储需要在Cassandra与PostgresSQL之间最终选择了Cassandra(版本2.2.2)。
- **[Elasticsearch](http://www.elastic.co/)** 用于日志存储，可以利用其强大的检索功能对日志进行分析(版本1.7.3)。
- **[Hazelcast](http://hazelcast.org/)** 用于session缓存以及必要的集群间信息的共享，虽然Vert.x本身也是使用Hazelcast来共享数据，但是为了使应用自身的数据共享更加明确和可定制还是创建了自己的Hazelcast实例。

### 使用的语言 ###
- **[Groovy](http://www.groovy-lang.org/)**
使用这种JVM语言可以极大的减少代码量，但是它的动态类型也会给编码带来一些困扰；但是总的来说它还是提高了效率。如果您觉得没有必要去学习它或者对于它实在不感冒，那么你可以将源码当作java代码来读，修改的时候也可以直接写java代码，一样可以编译执行。

### 后续的改进计划 ###
- 容器化部署
- 集群改进
- 支持RESTful

### 如何运行 ###
- 启动如下服务：
**[Cassandra](http://cassandra.apache.org/)2.2.2**，**[Elasticsearch](http://www.elastic.co/)1.7.3** 当然您可以根据您的需要修改端口以及其他的实例参数。幸运的是它们都可以在下载后直接启动。
- 修改应用中的配置文件，它们的位置: **src\main\resources\org\jianyi\yibuyiqu**
下面有两个配置文件：
config.json是应用的主要配置文件，配置了每个必要服务的具体实现以及他们启动需要的参数。主要关注的两个服务是CassandraService以及ElasticService。如果您没有修改过 **[Cassandra](http://cassandra.apache.org/)**以及**[Elasticsearch](http://www.elastic.co/)** 的配置那么可能您不需要进行修改，如果您修改过，那么请根据修改的具体端口等参数进行配置。
storage.json文件描述了数据库的结构以及初始化数据的内容。其中table节点是表的描述，common属性描述的是所有的数据表共有的属性，users属性描述的是users表的特有属性。如果你要使用某个列进行查询那么请将它添加到表的index属性中。下面的data属性中是每个表的初始化数据，对于各位高手来说看懂应该很容易。
- 启动应用
找到Application.groovy直接运行就可以启动服务了。它会自动在你的 **[Cassandra](http://cassandra.apache.org/)** 中创建keyspace和collection，并且创建初始化用户user。在 **[Elasticsearch](http://www.elastic.co/)** 中创建index。
- 验证是否可用
**src\main\resources\webroot** 目录中是一个可以部署的静态web应用。部署它到你熟悉的服务器中，然后请求index.html页面会有一个输入框和一个按钮。
在输入框中输入{"command":"login","username":"user","password":"user"}点击按钮，会弹出一个登录成功的提示

### 如何开发 ###
- 参考登录命令服务来开发自己的服务，接收的命令格式根据需要定制。
- 增加一个独立的类似于Application.groovy的verticle，读取特定的配置文件来部署自己的业务服务

