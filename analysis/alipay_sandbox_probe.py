# -*- coding: utf-8 -*-
"""沙箱网关参数对比探针：定位 alipay.trade.page.pay 收银台 INVALID_PARAMETER 的具体原因"""
import re, base64, time, datetime, json, urllib.request, urllib.parse
from cryptography.hazmat.primitives.serialization import load_der_private_key
from cryptography.hazmat.primitives import hashes
from cryptography.hazmat.primitives.asymmetric import padding

CFG = r'E:/programs/java_arsenal/permission-system/src/main/resources/application-local.yml'
GW = 'https://openapi-sandbox.dl.alipaydev.com/gateway.do'

cfg = open(CFG, encoding='utf-8').read()
priv_b64 = re.search(r'privateKey:\s*(\S+)', cfg).group(1)
key = load_der_private_key(base64.b64decode(priv_b64), password=None)
APP_ID = re.search(r'appId:\s*(\S+)', cfg).group(1)

EXPIRE = (datetime.datetime.now() + datetime.timedelta(hours=24)).strftime('%Y-%m-%d %H:%M:%S')
SEQ = iter(range(100))


def out_no(n):
    return 'PAYTEST' + time.strftime('%H%M%S') + '%03d' % n


def base_biz(n, **over):
    d = {
        'out_trade_no': out_no(n),
        'product_code': 'FAST_INSTANT_TRADE_PAY',
        'subject': '履约单付款：test pay',
        'total_amount': '15962.00',
    }
    d.update(over)
    return json.dumps(d, ensure_ascii=False, separators=(',', ':'))


def sign_req(biz):
    ts = time.strftime('%Y-%m-%d %H:%M:%S')
    params = {
        'app_id': APP_ID, 'method': 'alipay.trade.page.pay', 'charset': 'UTF-8',
        'sign_type': 'RSA2', 'timestamp': ts, 'version': '1.0', 'format': 'json',
        'notify_url': 'http://124.222.192.3:16399/api/pay/notify',
        'return_url': 'http://localhost:13690/pay/success',
        'biz_content': biz,
    }
    content = '&'.join(f'{k}={params[k]}' for k in sorted(params))
    sig = key.sign(content.encode('utf-8'), padding.PKCS1v15(), hashes.SHA256())
    params['sign'] = base64.b64encode(sig).decode()
    return params


class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, req, fp, code, msg, headers, newurl):
        return None


OPENER = urllib.request.build_opener(NoRedirect)


def post(params):
    data = urllib.parse.urlencode(params).encode()
    req = urllib.request.Request(GW, data=data, headers={
        'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8',
        'User-Agent': 'Mozilla/5.0'})
    try:
        r = OPENER.open(req, timeout=20)
        return r.status, dict(r.headers), r.read().decode('utf-8', 'replace')
    except urllib.error.HTTPError as e:
        return e.code, dict(e.headers), e.read().decode('utf-8', 'replace')
    except Exception as e:
        return -1, {}, 'EXC: ' + str(e)


cases = [
    ('A 原样复制失败请求(含time_expire)', lambda n: base_biz(n, time_expire=EXPIRE)),
    ('A2 A的重复用例(验证确定性)',       lambda n: base_biz(n, time_expire=EXPIRE)),
    ('B 去掉time_expire',               lambda n: base_biz(n)),
    ('C 去time_expire+纯英文subject',   lambda n: base_biz(n, subject='test pay')),
    ('D 去time_expire+小额0.01',        lambda n: base_biz(n, total_amount='0.01')),
]

for name, fn in cases:
    status, headers, body = post(sign_req(fn(next(SEQ))))
    loc = headers.get('Location', headers.get('location', ''))
    bad = any(kw in body for kw in ('INVALID', '不识别', '钓鱼', '验签', '账户不存在', 'ERROR'))
    has_cashier = ('cashier' in body.lower() or 'alipay' in body.lower()) and len(body) > 1000
    if bad:
        tag = 'REJECTED'
    elif status == 302 or status == 301 or status == 303 or status == 307:
        tag = 'REDIRECT'
    elif has_cashier:
        tag = 'CASHIER_PAGE'
    elif len(body) == 0:
        tag = 'EMPTY'
    else:
        tag = 'OTHER'
    hint = ''
    for kw in ('错误码', 'sub_msg', 'subMsg', '钓鱼', '验签失败', '不识别'):
        idx = body.find(kw)
        if idx >= 0:
            hint = body[max(0, idx - 60):idx + 140].replace('\n', ' ')
            break
    snippet = (loc or body[:160]).replace('\n', ' ')
    print(f'[{tag}] {name} http={status} len={len(body)} loc/snippet={snippet[:220]} | {hint[:200]}')
