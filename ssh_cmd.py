import paramiko
import sys

ssh = paramiko.SSHClient()
ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
ssh.connect('192.168.200.128', username='tingqq7z', password='xiaoxu', timeout=10)

cmd = ' '.join(sys.argv[1:]) if len(sys.argv) > 1 else 'docker ps'
stdin, stdout, stderr = ssh.exec_command(cmd)
print(stdout.read().decode(), end='')
err = stderr.read().decode()
if err:
    print(err, end='', file=sys.stderr)
ssh.close()
