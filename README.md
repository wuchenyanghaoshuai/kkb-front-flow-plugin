# kkb front k8s deploy plugin
# 此pipeline为前端部署脚本
```
1. 需要修改的地方为: AccessControlStage.groovy
第52行: 添加一个为admin的jenkins的账号,此处我添加了一个devops
```
![image](https://user-images.githubusercontent.com/39818267/150089242-a1f38721-79cd-4fff-83f7-96ebabfcc5be.png)

```
2.修改BuildImageStage.groovy
修改37行附近: dev || test 环境修改为自己的harbor地址即可
resigtryLogin 这个位置是修改为jenkisn的token,创建一个凭据，账号能连上harbor即可，然后把token复制过来
prod环境我这边使用的是阿里云的景象仓库，
```
![image](https://user-images.githubusercontent.com/39818267/150089450-b5dc4596-ea5a-4d86-a083-18bfba516026.png)
![image](https://user-images.githubusercontent.com/39818267/150089900-cc624b51-6be4-47e9-8e55-910a05aef27a.png)
![image](https://user-images.githubusercontent.com/39818267/150090005-858879b2-0436-4381-9f1d-51ab1fddd682.png)

```
3.修改PullScriptStage.groovy
修改25-27行: 需要修改一下git地址，然后在该gitlab上创建一个kkb-release组
credentialsId 这块的token是 在jenkins创建一个账号去gitlab里去拉代码的
```
![image](https://user-images.githubusercontent.com/39818267/150105382-335e89c5-c366-4525-a723-a2174361dc34.png)
![image](https://user-images.githubusercontent.com/39818267/150105494-21f687a4-0ca0-4207-8fb1-93c7892a694b.png)
![image](https://user-images.githubusercontent.com/39818267/150105521-5a659d3c-a722-4482-a675-8eedefe6cc19.png)
