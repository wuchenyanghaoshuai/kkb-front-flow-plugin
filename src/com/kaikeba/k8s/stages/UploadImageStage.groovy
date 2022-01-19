package com.kaikeba.k8s.stages

import java.io.Serializable
import org.yubing.delivery.Stage

/**
 * 	上传镜像
 */
class UploadImageStage extends Stage {
    def serverName
    def version
    def script
    def config
    def deployEnv

    UploadImageStage(project, stageName, deployEnv) {
        super(project, stageName)
        this.script = project.script
        this.config = project.config
        this.deployEnv = deployEnv
    }

    def run() {
        this.serverName = config.name
        this.version = config.version

        def skipBuild = this.config.skipBuild
        def registryUrl = ""
        def registry
        def registryLogIn
        def registryNS
        this.script.echo "UploadImageStage skipBuild:${skipBuild}"

        if(!skipBuild) {
            this.script.node('jenkins-slave-k8s') {
                if (this.deployEnv=="test"||this.deployEnv=="dev"){
                    registryUrl = "192.168.100.36:1179"
                    registryLogIn = "kkb_docker_registry_login"
                    registryNS = "xiaoke"
                } else if(this.deployEnv=="prod"||this.deployEnv=="pre"){
                    registryUrl = "kkb-registry.cn-beijing.cr.aliyuncs.com"
                    registryLogIn = "kkb_aliyun_docker_registry_login"
                    registryNS = "kaikeba"
                }
                registry = "http://"+registryUrl

                this.pushImage(registry, registryLogIn, registryUrl, registryNS, this.serverName)

                this.deleteLocalImages("${registryUrl}/${registryNS}/${this.serverName}")
            }
        }
    }

    def deleteLocalImages(imageName){

        this.script.echo "删除本地镜像"
        this.script.sh "release/clean.sh ${imageName}"
        // this.script.sh "docker rmi 60.205.211.195:1179/kkb-xiaoke/${serverName}-server:${version}"
        // this.script.sh "docker rmi  kkb-registry.cn-beijing.cr.aliyuncs.com/kkb-xiaoke/${serverName}-server:${version}"
    }

    def pushImage(registry, registryLogIn, registryUrl, registryNS, serverName) {
        def version = this.version
        def docker = this.script.docker

        // We are pushing to a private secure Docker registry in this demo.
        // 'docker-registry-login' is the username/password credentials ID as defined in Jenkins Credentials.
        // This is used to authenticate the Docker client to the registry.
        docker.withRegistry(registry, registryLogIn) {

            // withRegistry 会在当前stage里设置环境变量 DOCKER_REGISTRY_URL = http://192.168.100.36:1179
            // 如果需要覆盖 DOCKER_REGISTRY_URL，需要在 withRegistry 内部重新定义DOCKER_REGISTRY_URL环境变量，
            // 不能在 withRegistry 外部定义DOCKER_REGISTRY_URL环境变量

            def serverEnv = []
            serverEnv.add("DOCKER_REGISTRY_URL=${registryUrl}")
            serverEnv.add("DOCKER_NAME_SPACE=${registryNS}")
            serverEnv.add("DOCKER_IMAGE_NAME=" + "${serverName}")
            serverEnv.add("DOCKER_TAG=" + "${version}")
            this.script.withEnv(serverEnv) {
                // 上传镜像
                this.script.sh "release/push.sh ${deployEnv}-${version}"
            }
        }
    }

    // def pushReleaseImage() {
    //     def version = this.version
    //     def docker = this.script.docker
    //     docker.withRegistry('http://kkb-registry.cn-beijing.cr.aliyuncs.com', 'kkb_aliyun_docker_registry_login') {

    //         def serverEnv = []
    //         serverEnv.add("DOCKER_REGISTRY_URL=kkb-registry.cn-beijing.cr.aliyuncs.com/kkb-xiaoke")
    //         this.script.echo "serverEnv:" + serverEnv

    //         this.script.withEnv(serverEnv) {

    //             this.script.sh "./release/push.sh ${version}"

    //         }
    //     }
    // }
}
