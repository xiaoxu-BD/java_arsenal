# -*- coding: utf-8 -*-
import paramiko, sys
ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('192.168.200.128', username='tingqq7z', password='xiaoxu', timeout=10)

sql = sys.stdin.read()
# Use a here-doc to avoid quoting hell
cmd = "docker exec -i mysql-8 mysql -uroot -pNFTurbo666 2026mysql_ds"
stdin, stdout, stderr = ssh.exec_command(cmd)
stdin.write(sql)
stdin.channel.shutdown_write()
print(stdout.read().decode(), end='')
err = stderr.read().decode()
if err:
    print(err, end='', file=sys.stderr)
ssh.close()
