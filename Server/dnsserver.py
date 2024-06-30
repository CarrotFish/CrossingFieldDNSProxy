#Created By CarrotFish1024
#Stellamist Starter 17


import socket
import threading
import os, traceback

try:
    import func_timeout
except:
    os.system('python -m setup.py')
    import func_timeout

ip = '0.0.0.0'
port = 6001

remote_ip = '114.114.114.114'
remote_port = 53

links = []
server = None

def jiami(r:bytes)->bytes:
    #rdata = list(r)
    #for i in range(len(rdata)):
        #a = rdata[i]
        #rdata[i] = (~a).to_bytes(1, 'little')
    return r #不进行加密

@func_timeout.func_set_timeout(60.0)
def getdns(csocket:socket.socket, addr, banPrint=False):
    global server, links
    if banPrint:
        print = lambda x:None
    try:
        print('recieve from %s:%d' % (addr[0], addr[1]))
        # 接受数据
        request = csocket.recv(4096)
        if addr[0] not in links:
            links.append(addr[0])
        client = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        #数据加密
        request = jiami(request)
        client.sendto(request, (remote_ip, remote_port))
        data = client.recv(4096)
        print('回传中...')
        csocket.send(jiami(data))
        print('操作完毕')
        csocket.close()
    except Exception as e:
        print(str(e))

def getdns2(csocket:socket.socket, addr, banPrint=False):
    try:
        getdns(csocket, addr, banPrint)
    except:
        csocket.close()

def main_thread():
    global server, links
    while True:
        try:
            csocket, addr = server.accept()
            threading.Thread(target=getdns2, args=(csocket, addr, True)).start()
        except Exception as e:
            #print(str(e))
            pass
def main_console():
    global server, links, settings
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((ip, port))
    server.listen(20)
    mt = threading.Thread(target=main_thread)
    mt.start()
    print('CarrotFishStudio DNS Proxy - on %s:%d' % (ip, port))
    print('[Warning] Unsafe Server Side')
    while True:
        cmd = input('>>> ')
        if cmd in ('exit', 'quit'):
            os._exit(0)

main_console()
