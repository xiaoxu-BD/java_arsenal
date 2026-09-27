# -*- coding: utf-8 -*-
"""
模拟其他开发组的 MQ 推送(接入节点二):
构造与线上 MQ 完全一致的消息实体,POST 到 /api/messages。
该接口与未来真实 MQ Listener(RocketMQ/RabbitMQ/Kafka)调用同一个
WechatMessageConsumer.onMessage,所以这里模拟 = 线上推送,幂等/校验/落库行为完全相同。

用法:
  python push_mq.py --room ROOM_001 --count 3              # 随机发送人/内容推 3 条
  python push_mq.py --room ROOM_001 --count 5 --interval 3 # 每 3 秒推 1 条,共 5 条
  python push_mq.py --room ROOM_002 --from 张三 --content "今天有3票货物需要申报"
  python push_mq.py --room ROOM_001 --msgid FIXED-ID --content x   # 固定 msgid,重复执行可验证幂等(DUPLICATED)
  python push_mq.py --room ROOM_004 --count 1              # 未配置群,应返回 IGNORED_ROOM_NOT_CONFIGURED

注意: Windows 控制台传中文参数可能乱码(GBK),演示建议用随机模式,自定义文案改下方 SENDERS/CONTENTS 列表。
"""
import argparse
import json
import random
import time
import urllib.request

BASE_URL = 'http://127.0.0.1:18099'

SENDERS = ['张三', '李四', '王五', '赵六', '钱七', '孙八']
CONTENTS = [
    '今天有{n}票货物需要申报',
    '其中一票单证还没到，下午补齐',
    '资料已经补充完成，请审核',
    '货物{n}已经完成申报，请知悉',
    '注意：今天截单时间提前到 17:00',
    '今日暂无异常',
]


def push(base_url, room, sender, content, msgid=None):
    body = {
        'msgid': msgid or ('SIM-' + time.strftime('%H%M%S') + '-' + uuid4()),
        'action': 'send',
        'from': sender,
        'tolist': ['icefog'],
        'roomid': room,
        'msgtime': int(time.time() * 1000),
        'msgtype': 'text',
        'text': {'content': content},
    }
    req = urllib.request.Request(
        base_url + '/api/messages',
        data=json.dumps(body, ensure_ascii=False).encode('utf-8'),
        method='POST',
        headers={'Content-Type': 'application/json; charset=utf-8'})
    with urllib.request.urlopen(req, timeout=30) as resp:
        result = json.loads(resp.read().decode('utf-8'))
    data = result.get('data') or {}
    print('[%s] %s %-2s: %-24s -> %s' % (
        time.strftime('%H:%M:%S'), room, sender,
        body['text']['content'][:22],
        data.get('consumeResult', result.get('message', 'ERROR'))))
    return result


def uuid4():
    return format(random.getrandbits(48), '012x')


def main():
    ap = argparse.ArgumentParser(description='模拟企业微信群消息 MQ 推送')
    ap.add_argument('--room', required=True, help='roomid,如 ROOM_001')
    ap.add_argument('--from', dest='sender', default=None, help='发送人(不填则随机)')
    ap.add_argument('--content', default=None, help='消息内容(不填则随机)')
    ap.add_argument('--msgid', default=None, help='指定 msgid(重复执行可验证幂等)')
    ap.add_argument('--count', type=int, default=1, help='推送条数,默认 1')
    ap.add_argument('--interval', type=float, default=1.0, help='多条推送间隔秒数')
    ap.add_argument('--base', default=BASE_URL, help='服务地址')
    args = ap.parse_args()

    for i in range(args.count):
        sender = args.sender or random.choice(SENDERS)
        content = args.content or random.choice(CONTENTS).replace('{n}', str(random.randint(1, 9)))
        push(args.base, args.room, sender, content, args.msgid)
        if i < args.count - 1:
            time.sleep(args.interval)


if __name__ == '__main__':
    main()
