package com.kaikeba.k8s.stages

import com.kaikeba.k8s.stages.PullScriptStage

/**
 * 构建front镜像
 */
class BuildImageStage extends PullScriptStage {


    def config
    def deployEnv
    BuildImageStage(project, stageName, deployEnv) {
        super(project, stageName)
        this.config = project.config
        this.deployEnv = deployEnv
    }

    def run() {

        def imageName = this.config.name
        def version = this.config.version
        def registryUrl = ""
        def registry
        def registryLogIn
        def registryNS
        this.script.echo "config:${this.config}"
        this.script.currentBuild.description="${this.script.params.CHANGE_TYPE}:${this.script.env.gitlabSourceBranch}"
        this.script.node('jenkins-slave-k8s') {

            this.script.checkout this.script.scm
            this.deploy_script_dir = "release"
            // 拉取脚本
            pullScript()
            def deployEnv = this.deployEnv
            if (deployEnv=="dev"||deployEnv=="test"||deployEnv=="test2"||deployEnv=="test3"||deployEnv=="test4"||deployEnv=="test5"){
                registryUrl = "192.168.1.8"
                registryLogIn = "fb4b2752-1983-4729-a992-5b22b7b9241c"
                registryNS = "xiaoke"
            } else if(deployEnv=="prod"||deployEnv=="pre"){
                registryUrl = "kkb-registry.cn-beijing.cr.aliyuncs.com"
                registryLogIn = "kkb_aliyun_docker_registry_login"
                registryNS = "wuchenyang"
            }
            registry = "http://"+registryUrl
            //按环境区分image
            version = "${deployEnv}-${version}"
            def imageExist = this.script.sh(returnStdout: true, script: "docker images | grep  ${imageName} | grep ${version} | tr -d '\n'")
            if (imageExist != ""){
                // 构建镜像tag存在,略过
                this.script.echo "The image is exists, skip build"
            } else {
                // 构建镜像
                this.script.docker.withRegistry(registry, registryLogIn) {
                    def serverEnv = []
                    serverEnv.add("DOCKER_REGISTRY_URL=${registryUrl}")
                    serverEnv.add("DOCKER_NAME_SPACE=${registryNS}")
                    serverEnv.add("DOCKER_IMAGE_NAME=" + "${imageName}")
                    serverEnv.add("DOCKER_TAG=" + "${version}")
                    this.script.echo "${deployEnv}BuildImageStage serverEnv:${serverEnv}"
                    this.script.withEnv(serverEnv) {
                        try {
                            this.script.sh 'docker pull $(cat ./release/Dockerfile |grep FROM |awk  NR==1 |awk \'{print $2}\' )'
                            this.script.sh "${this.deploy_script_dir}/build.sh ${deployEnv} ${version}"
                        } catch (e) {
                            if (this.script.env.gitlabSourceRepoHomepage) {
                                this.sendToNvWa()
                                this.script.sh "exit 1"
                            }
                        }
                    }
                    this.script.withEnv(serverEnv) {
                        try {
                            this.script.sh "${this.deploy_script_dir}/push.sh ${version}"
                        } catch (e) {
                                this.script.sh "exit 1"
                        }
                    }
                    this.script.withEnv(serverEnv) {
                        try {
                            this.script.echo "docker rmi registry.cn-wulanchabu.aliyuncs.com/wuchenyang/${imageName}:base "
                            this.script.sh "docker rmi registry.cn-wulanchabu.aliyuncs.com/wuchenyang/${imageName}:base "
                            // this.script.sh "docker rmi $(docker images |grep base |awk -F " "  '{print $1":"$2}') " 
                        } catch (e) {
                                this.script.sh "exit 0"
                        }
                    }
                }
            }

        }
    }
  def sendToNvWa() {
    def branch = this.script.env.gitlabSourceBranch
    def jenkinsUrl = this.script.env.BUILD_URL
    def commitId = this.script.sh(returnStdout: true, script: "git rev-parse HEAD|tr -d '\n'")
    def gitUrl = "${this.script.env.gitlabSourceRepoHomepage}/commit/${commitId}"
    def appName = this.config.name
    this.script.sh """
    curl --location --request POST 'https://testkmsapi.kaikeba.com/v1/logs/compile/error' \
        --header 'Content-Type: application/x-www-form-urlencoded' \
        --data-urlencode 'withoutfigdata= {"app_id":"68e09ffb3bde4e8b35e6bf99137107b5","branch": "${branch}","app_name": "${appName}","content": "${jenkinsUrl}","commit_id": "${commitId}","git_url": "${gitUrl}"}'
   """
  }
}

