import socket
import socketserver
import threading

class Item:
    def __init__(self, x, y, typ, player=None, picked=False):
        self.x = x
        self.y = y
        self.type = typ
        self.picked = picked
        #id
        self.owner = player
    
    def pick(self, player):
        self.x = -600
        self.y = -600
        self.owner = player
        self.picked = True
        return True

class Player:
    def __init__(self, id_, ip, nickname, x=0, y=0, hp=100):
        self.nickname = nickname
        self.id = id_
        self.ip = ip

        self.x = 0
        self.x_rate = 5
        self.y = 0
        self.y_rate = 5
        self.hp = 100

        self.inventory = {0:None, 1:None, 2:None,
                          3:None, 4:None, 5:None,
                          6:None, 7:None, 8:None}

class GameInstance:
    def __init__(self):
        self.counter = -1
        self.counter_items = 0
        self.players = {}
        self.ids_to_ips = {}
        self.ips_to_ids = {}
        self.items = {}
        #example item load
        self.items.update({self.counter_items: Item(300, 400, 3)})
        self.counter_items += 1
    
    def ping(self, s):
        b = bytes("b", "utf-8")
        return bytes(b+s)
    
    def join(self, s, ip):
        self.counter += 1
        #for i in self.players.keys():
        #    if self.players[i].ip == ip:
        if ip in self.ips_to_ids.keys():
            return bytes("jr%s" % self.ips_to_ids[ip], "utf-8")
        self.players.update({self.counter: Player(self.counter, ip, s)})
        self.ids_to_ips.update({self.counter: ip})
        self.ips_to_ids.update({ip: self.counter})
        return bytes("jc%s" % self.players[self.counter].id, "utf-8")

    def disconnect(self, ip):
        try:
            temp_id = self.ips_to_ids[ip]
            del self.ips_to_ids[ip]
            del self.ids_to_ips[temp_id]
            del self.players[temp_id]
            return bytes("dc%s" % temp_id, "utf-8")
        except ValueError:
            pass
        return bytes("dr", "utf-8")

    def update_pos(self, s, ip):
        print(s)
        try:
            pos = str(s, "utf-8").split(",")
            id_ = self.ips_to_ids[ip]
            self.players[id_].x = int(pos[0])
            self.players[id_].y = int(pos[1])
            return bytes("pc", "utf-8")+s
        except KeyError:
            pass
        return bytes("pr", "utf-8")+s
    
    def heal(self, s, ip):
        try:
            #no
            s=0
            self.players[self.ips_to_ids[ip]].hp += s
            return bytes("hc%s" % s, "utf-8")
        except KeyError:
            pass
        return bytes("hr", "utf-8")+s

    def pick(self, s, ip):
        try:
            a = str(s, "utf-8").split(",")
            player = self.players[self.ips_to_ids[ip]]
            radius = 50
            id_, slot = int(a[0]), int(a[1])
            if player.x-radius < self.items[id_].x < player.x+radius:
                print("i")
                if player.y-radius < self.items[id_].y < player.y+radius:
                    print("j")
                    self.items[id_].pick(player)
                    if player.inventory[slot] == None:
                        print("gowno")
                        player.inventory.update({slot: self.items[id_]})
                        return bytes("ic", "utf-8")+s
        except KeyError:
            pass
        return bytes("ir", "utf-8")+s

    def firing_vector(self, vector, ip):
        print("Vector: %s" % vector)
        #w,h = vector.split(b',')
        #print("%s|%s" % (w,h))
        if ip in self.ips_to_ids.keys():
            return bytes("fc%s" % vector, "utf-8")
        else:
            return bytes("fr%s" % vector, "utf-8")

class Handler(socketserver.BaseRequestHandler):
    game = GameInstance()
    def handle(self):
        try:
            while True:
                self.data = self.request.recv(64)
                print("%s's data: " % self.client_address[0])
                print("\t%s" % self.data)
                modifier = self.data[0]
                only_data = self.data[1:]
                print(modifier, only_data)
               #ping
                if modifier == 97:
                    x = self.game.ping(only_data)
                    print(x)
                    self.request.sendall(x)
               #join
                if modifier == 106:
                    x = self.game.join(only_data, self.client_address[0])
                    print(x)
                    self.request.sendall(x)
                #disconnect
                if modifier == 100:
                    x = self.game.disconnect(self.client_address[0])
                    print(x)
                    self.request.sendall(x)
                #pos
                if modifier == 112:
                    x = self.game.update_pos(only_data, self.client_address[0])
                    print(x)
                    self.request.sendall(x)
                #firing_vector
                if modifier == 102:
                    x = self.game.firing_vector(only_data, self.client_address[0])
                #heal
                if modifier == 104:
                    x = self.game.heal(only_data, self.client_address[0])
                    print(x)
                    self.request.sendall(x)
                if modifier == 105:
                    x = self.game.pick(only_data, self.client_address[0])
                    print(x)
                    self.request.sendall(x)
        except IndexError:
            pass
        print("Disconnected from %s" % self.client_address[0])
        ##send the same shit, just upped
        #self.request.sendall(self.data.upper())

if __name__ == "__main__":
    host, port = "localhost", 9999
    print("Server started!")
    server = socketserver.TCPServer((host, port), Handler)
    server.serve_forever()
