# dappLogin
流程：
1.需要将sdk文件夹里面的jar引入的本地仓库
2.引入到本地仓库的指令：mvn install:install-file -Dfile=jar所处的路径 -DgroupId=ospn-old -DartifactId=ospn-old -Dversion=1.4 -Dpackaging=jar
3.修改sdk里面的配置文件，只需要修改ospn.properties和start.sh即可
4.项目打包出来后需要把libecSSL.so和opsnBase.jar和start.sh放在项目的同级目录，启动项目直接是执行命令: sh start.sh
5.前端代码在front里面