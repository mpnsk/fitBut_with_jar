FIT BUT submission:
=======
for The 2020 Contest  
The Scenario: Agents Assemble
   
Created by members of  
Faculty of Information Technology  
Brno University of Technology

Team
-------
[Vaclav Uhlir](mailto:iuhlir@fit.vutbr.cz)    
[Frantisek Zboril](mailto:zborilf@fit.vutbr.cz)  
[Frantisek Vidensky](mailto:ividen@fit.vutbr.cz)  
[Martin Sustek](mailto:isustek@fit.vutbr.cz)

Contest page: https://multiagentcontest.org/2020/

HowTo:
----
Run:
1. Server
2. Client

Server:
--------
1. Compile or download from https://github.com/agentcontest/massim_2019/releases/download/v2.1/massim-2019-2.0-bin.tar.gz
2. In directory `...\server>`
run: 
```cmd
"c:\Program Files\Java\jdk-13.0.2\bin\java.exe"  -jar target\server-2020-2.0-jar-with-dependencies.jar --monitor -conf conf\SampleConfig.json
```

(or speed-run:)
```
"c:\Program Files\Java\jdk-13.0.2\bin\java.exe"  -jar target\server-2020-2.0-jar-with-dependencies.jar --monitor -conf conf\SpeedTest.json
```


Client:
--------

Run configuration:  
Main class: `fitBut.Main`  
Working directory `...massim_2020\fitBut\`

Logger settings:
-----------------
[QoL] Import temples  
(Old IntelliJ) File -> Import Settings -> `...\fitBut\conf\settings.zip`  
(2020.1+) File-> Manage IDE Settings -> Import Settings -> `...\fitBut\conf\settings.zip`

log can be inserted by typing:  
`logi` then Enter/Tab will produce  `HorseRider.inform(TAG, "action: ...");`  
similarly `loge` for error print (and. `logee` with Throwable)
  
`logwtf` - what a terrible fail   
`logd` - debug  
`logi` - inform  
`logw`/`logwe` - warn  
`loge`/`logee` - error


logs will be in `fitBut/logs`

syntax for editor marking:

^\[([^]]+)]([^:]*):([^:]*):(.*)$

and error log coloring:  
^\s*(e(rror)?|SEVERE)\s*$

![ideolog](ideolog.jpg?raw=true "ideolog")

Amount of messages printed can be changed in `utils/HorseRider`:`DEBUG_level = 4`;   
 


