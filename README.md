# ElasticsearchTree
es搜索


![](https://i.imgur.com/0ujSKwB.jpg)

<pre>
Elasticsearch
   ElasticSearch基本概念
      1）索引
	      是ES存放数据的地方。索引可以理解为关系型数据库中的数据表
	  2）文档
          文档是ES中存储的主要实体，ES中的文档可以理解为关系型数据库中的一行记录
      3）文档类型
      5）节点和集群
          ES可以作为一个独立的搜索服务器工作，多台服务器组成一个集群，每台服务器称为一个节点node,可以通过索引
          分片将海量的
        数据分割分不到不同节点。通过副本机制可以实现更高的性能和更好强的可用性。
	  6）分片
          当需要存储大规模文档时，由于RAM限制，硬盘容量等的限制，仅适用一个节点是不够的，另一个问题是一个节点的计算
        能力达不到所期望的复杂功能的要求。在这种情况下，可以将数据切分，每部分是一个单独的Apache Lucene索引，称为分
        片。每个分片可以被存储在不同节点。当需要查询一个由多个分片构成的索引时，ES将该查询发送到相关的分片，并将结果合并。
      7）副本
          为了提高查询的吞吐量或实现高可用性，可以开启分片副本功能。副本分片是对原始分片的一个精确拷贝，原始分片被
          称为主分片。对索引的所有修改操作都直接作用在主分片上，每个主分片可以有零个或者多个副本分片。当主分片丢失时，
          集群可以将一个副本分片提升为新的主分片。

      1）安装与运行
         1）安装Java
         2）与ES交互的主要接口是基于HTTP协议和REST API。
	     3）集群名称&&节点名称
         cluster.name 保存集群的名称，通过集群名称可以区分不同的器群，配置具有相同名称的节点将尝试形成一个集群。
         node.name 节点名称。用户可以不指定节点名称，ES会自动为节点选择一个唯一名称，每次启动时都会选择，导致的结
                   果就是每次启动，节点的名称都会变化。	

    2) 数据操作（put,get/delete）
    1）更新文档
        ES中更新文档是非常复杂的工作，ES必须先提取文档，从"_source"字段获取数据，移除旧文档，应用变更，并作为一个
        新文档创建索引。   

    3）创建索引与配置映射
        ES是一个无模式的搜索引擎并且可以自动匹配数据的结构。但是认为定义和控制数据结构是更好的方式。	
        ES的索引是ES存储数据的一种逻辑结构。可以把它想象成数据库中的包含行和列的表。行是索引中的一个文档，列是索引中
        的字段。ES可以同时运行多个索引。
        模式映射：
            用来定义索引结构。
	        例如：
	        {
		        "mappings":{
			        "bonaparte":{
				        "properties":{
					        "id":{
						        "type":"long",
							    "store":"yes"
						    },
                            "name":{
                                 "type": "text",
                                 "similarity": "BM25",
                                 "fields": {
                                      "like": {
                                         "type": "text",
                                         "analyzer": "artist_name_like_analyzer"
                                     }
                                } 
                            }
					    }
				    }
			    }
		    }
            文件命名为 ponaparte.json
        分析器：
            分析器以某种方式来分析数据或者查询语句---例如当我们基于空格和小写字母来划分单次时，可以不用担心用
            户输入的下划线和大写字母。在创建索引及搜索时，ES允许使用不同的分析器，因此可以在搜索的不同阶段选择
            不同的数据处理方式。使用分析器只需要在相应的字段属性中指定分析器的名称即可。
     	    ES内置的分析器：
               1）standard
	           2) simple
	           3) whitespace
	           5) stop
	           6) keyword
	           7) pattern
	           8) language
	           9) snowball
            除上面的分析器外，ES还支持自定义分析器。自定义分析器需要在映射文件中添加一个settings部分，
               "settings":{
	               "index":{
		               "analysis":{
			               "analyzer":{
				               "sb":{
					               ...
					           }
				            }
			            }
		          }
	          }

    5）动态映射与模板
       1）类型确定机制
          ES通过查看定义某文档的JSON格式就能猜测到文档结构。类型推定

    6）路由选择
       当某个文档需要索引时，ES查看文档的ID以选择应该索引的分片，默认情况下，ES计算文档ID的hash值，并基于
       该hash值将文档放在某个可用的主分片中，然后这些文档被复制到副本分片。

    8）搜索数据
       搜索和索引的过程
       1）索引过程
	        准备发送数据到ES的文档并在索引中存储文档的过程
	   2）搜索过程
            匹配满足查询条件文档的过程	 
       3）分析过程
       预备字段内容，并将其转换为可以写入Lucene索引的词项（term）的过程。
	        1）词条化
			2）过滤
       5）分析器
            分析器是带有零个或多个过滤器的分词器。
            分析过程在索引过程和分析过程都会用到	
       6) 查询DSL
            将一个查询的请求封装为一个json格式的对象，并发送给ES,这个JSON对象称之为查询DSL
	        1) 简单查询
                1) 搜索类型
                    1）query_and_fetch: 通常是最快也是最简单的搜索类型，查询语句在所
                    有需检查的分片上并行执行，并且所有分片返回结果的规模为size参数的
                    取值，因此该查询返回的文档数据的最大值为size * 分片数目。
	                2）query_then_fetch:查询语句首先得到将文档排序所需的信息，然后得
                    到要获取的文档内容的相关分片，该类型返回的文档数目最大为size的大小
	                3）dfs_query_and_fetch:该类搜索类似于query_and_fetch,除了完
                    成query_and_fetch的工作外，还执行初始查询阶段，该阶段计算分布式的
                    词频以更精确的返回文档打分
	                5）dfs_query_then_fetch：该类型类似于query_then_fetch，除了完
                    成query_then_fetch的工作外，还执行初始查询阶段，该阶段计算分布式
                    词频，以更好完成文档打分
	                6）count:这是一种特殊的搜索类型，只返回匹配查询的文档数目
	                7）scan：这也是一种特殊的搜索类型，该类搜索仅用于预计查询会返回大量
                    结果的时候，它与通常的查询有些不同，因为发送了第一个请求后，
            2) 基本查询

    9）搜索优化 
       1）加权查询
           boost:权值
       2）在映射中定义加权
       3）同义词规则
       5）搜索不同语言的区分处理
       6）使用跨度查询

    10）组合索引，分析，搜索
       1) 使用嵌套文档&&父子关系	
       2) 批量索引以加快索引过程

    11）统计，相似度
    12）集群管理
       当ES根据配置分配所有分片和副本时，集群时可以全面投入使用的，此时都是绿色的
       当出现以下颜色
          1）黄色：主分片已经分配完成，已经做好处理请求的准备，但是某些副本尚未完成分配。
	      2）红色：ES集群尚未准备就绪，其中至少一个主分片没有准备好，
	 当只有一个节点却同时有多个副本时，ES很明显处于黄色状态，因为没有其他节点来放置这些副本。

     Google elasticsearch-head插件
</pre>

<pre>
  基于Restful风格的接口调用方式导入
  基于Rocketmq消息的数据导入
</pre>
  

![](https://i.imgur.com/gtNBaTg.png)


<pre>
ES 索引存储原理：

      1）不变性：
               写到磁盘的倒序索引是不变的：自从写到磁盘就再也不变。 这会有很多好处：
                   1）不需要添加锁，不存在写操作，因为不存在多线程更改数据。
                   2）提高读性能。一旦索引被内核的文件系统做了cache，绝大多数的读操作会直接从
                                内存而不需要经过磁盘。
                   3）提升其他缓存的性能。其他的缓存在该索引的生命周期内保持有效，减少磁盘
                                       I/O和计算消耗。

               索引的不变性也有缺点。如果想让新修改过的文档可以被搜索到，必须重新构建整个索引
           ，这在一个Index可以容纳的数据量和一个索引可以更新的频率上都是一个限制。

          
           如何在不丢失不变形的好处下让倒排索引可以更改？答案是：使用不只一个的索引。新添加的
      索引来反映新的更改来代替重新所有倒序索引的方案。

      Segment工作流程：
                     1：新的文档在内存中组织。
                     2：每隔一段时间，buffer将会被提交：申城一个新的segment（一个额外的
                        新的，倒排索引）并被写到磁盘，同时一个新的提交点被写入磁盘，包含新的
                        segment的名称，磁盘fsync，所有在内核文件系统中的数据等待被写入磁盘
                        ，来保障他们被物理写入。
                     3：新的segment被打开，使它包含的文档可以被索引。
                     5：内存中的buffer将被清理，准备接收新的文档。

      删除和更新：
                segments是不变的，所以文档不能从旧的segments中删除，也不能在旧的segments
      中更新来映射一个新的文档版本。取而代之的是，每一个提交点都会包含一个.del文件，列巨额了
      哪一个segment的哪一个文档已经被删除了，当一个文档被“删除了”，它仅仅是在.del文件里被标记
      了一下，被“删除”的文档依旧可以被索引到，但是它将会在最终结果返回时被移除掉。

                文档的更新同理：当文档更新时，旧版本的文档将会被标记为删除，新版本的文档在
      新的segment中建立索引。也许新旧版本的文档都会本检索到，但是旧版本的文档会在最终结果返
      回时被移除
</pre>

<pre>
实时索引
          在上述的per-segment搜索的机制下，新的文档会在分钟级内被索引，但是还不够快。 瓶颈
     在磁盘。将新的segment提交到磁盘需要fsync来保障物理写入。但是fsync是很耗时的。它不能在每
     次文档更新时就被调用，否则性能会很低。 现在需要一种轻便的方式能使新的文档可以被索引，这
     就意味着不能使用fsync来保障。 在ES和物理磁盘之间是内核的文件系统缓存。之前的描述中,在内
     存中索引的文档会被写入到一个新的segment。但是现在我们将segment首先写入到内核的文件系统
     缓存，这个过程很轻量，然后再flush到磁盘，这个过程很耗时。但是一旦一个segment文件在内核
     的缓存中，它可以被打开被读取。
</pre>

<pre>
更新持久化
    
         不使用fsync将数据flush到磁盘，我们不能保障在断电后或者进程死掉后数据不丢失。ES是可
     靠的，它可以保障数据被持久化到磁盘。一个完全的提交会将segments写入到磁盘，并且写一个提
     交点，列出所有已知的segments。当ES启动或者重新打开一个index时，它会利用这个提交点来决
     定哪些segments属于当前的shard。 如果在提交点时，文档被修改会怎么样？

         translog日志提供了一个所有还未被flush到磁盘的操作的持久化记录。当ES启动的时候，它
     会使用最新的commit point从磁盘恢复所有已有的segments，然后将重现所有在translog里面的
     操作来添加更新，这些更新发生在最新的一次commit的记录之后还未被fsync。

         translog日志也可以用来提供实时的CRUD。当你试图通过文档ID来读取、更新、删除一个文
     档时，它会首先检查translog日志看看有没有最新的更新，然后再从响应的segment中获得文档。
     这意味着它每次都会对最新版本的文档做操作，并且是实时的。  
</pre>

<pre>
Segment合并
         通过每隔一秒的自动刷新机制会创建一个新的segment，用不了多久就会有很多的
     segment。segment会消耗系统的文件句柄，内存，CPU时钟。最重要的是，每一次请求都会依次检查
     所有的segment。segment越多，检索就会越慢。

         ES通过在后台merge这些segment的方式解决这个问题。小的segment merge到大的，
     大的merge到更大的。。。

         这个过程也是那些被”删除”的文档真正被清除出文件系统的过程，因为被标记为删除的文档不
     会被拷贝到大的segment中。
</pre>

<pre>
Segment详解：

      Inverted Index:
            1:一个有序的数据字典Dictionary（包括单次Term和它出现的频率）
            2：与单次Term对应的Postings（即存在这个单次的文件）

      Stored Fields：
            当我们需要查找包含某个特定标题内容的文件时，Inverted Index就不能很好的解决这个
      问题，所以Lucene提供了另外一种数据结构Stored Fields来解决这个问题。本质上，Stored Fields
      是一个简单的键值对key-value。默认情况下，ES会存储整个文件的JSON source;

      Document Values:
            即使这样，我们发现以上结构仍然无法解决诸如：排序，聚合，facet，因为我们可能会要读取大量的
      不需要的信息。所以另一种数据结构解决了此问题：Document Values。这种结构本质上就是一个列式的存储，
      它高度优化了具有相同类型的数据的存储结构。
</pre>

###存储索引的流程

![](https://i.imgur.com/XEnRNx6.png)

<pre>
ElasticSearch深度分页问题

     常见深度分页方式 from + size
         es默认采用的分页方式是from + size的形式，在深度分页的情况下，这种使用方式效率是
      很低的，比如 from = 5000, size = 10,es需要在各个分片上匹配排序并得到5010条数据，
      然后在结果集中取最后的10条数据返回。这种方式类似于mongo的skip + size;

         除了效率上的问题，还有一个无法解决的问题是。es目前支持最大的skip值是max_result_window,
      默认为10000，也就是当from+size > max_result_window时，es将返回错误。当然可以零时修改该
      最大值，但是这种方式只能暂时解决问题，当es的使用越来越多，数据量越来越大，深度分页的场景
      越来越复杂时，不能解决该问题。

      方案1： scroll
             原理：
                    对某次查询生成一个游标scroll_id，后续的查询只需要根据这个游标去取数据，直到
                 结果集中返回的hits字段为空，就表示遍历结束，scroll_id的生成可以理解为一个零时
                 的历史快照，在此之后的增删改查等操作不会影响到这个快照的结果。

      方案2： search_after
             原理：
                    与scroll相比，它是根据上一页的最后一条数据来确定下一页的位置，同时在分页的过程
                 中，如果有索引数据的增删改查，这些变更也会实时的反映到游标上。
</pre>

<pre>
详细分析一个文档的索引过程
</pre>

<pre>
详细描述一个文档的查询过程
</pre>