1. Redis的数据结构
五种基本的数据类型
- String 
  - 常见的应用场景 缓存 计数器 分布式锁
  - value 可以是数字 可以是二进制序列化的数据
  - 常用命令: set key value  get key del key incr key 
  - setnx key value 
- List
  -•
  特点：

•
有序、可重复

•
基于双向链表实现，所以支持从头部/尾部快速插入和删除

•
可用来做简单的消息队列（但功能有限）

•
常用命令：

•
LPUSH key value1 value2（从左侧插入）

•
RPUSH key value

•
LPOP key（从左边弹出）

•
RPOP key

•
LRANGE key 0 -1（获取全部元素）
- Hash
   - 底层存储结构类似与Map<String,Map<K,V>>
   - 常见命令:
    HSET user:1 name "Alice" age 25
  • HGET user:1 name
  • HGETALL user:1
  • HKEYS user:1, HVALUES user:1
   - 
- Set
- Sorted Set