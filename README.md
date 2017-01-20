# remote_x
Network tunnel penetration agent.

---------------

### PING-PONG STEPM
```
Pong =(channelId)=> Ping =(periodicPing)=> Ping =(CMD[PING])=> CommandParse =(CMD_sock[PONG])=> Pong
                                              ^                                                   |
                                              |                                                   |
                                              |                                                   v
                                   (periodicPing)== Ping <=(Bus[/channel/ping/" + channelId])= CommandParse
```                                   

---------------

### DATA PROXY
```
User =(CONNECT REQUEST)=> ProxyClient =(Bus[/connect/choose])=> ProxyClient =(CMD[NEW_CONNECT])=> CommandParse =(CMD_sock[NEW_CONNECT])=> Pong
                     ^     /   ^                                                                                                            |
                      \___/    |                                                                                                            |
                               |                                                                                                            v
                     (CONNECT RESPONSE)= ProxyClient <=(CMD_sock[DATA])= CommandParse <=(CMD[DATA])= ProxyServer <=(Bus[/connect/new])= CommandParse
```


