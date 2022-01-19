package com.kaikeba.k8s.stages

import com.kaikeba.k8s.stages.PullScriptStage

/**
 * 构建front镜像
 */
class BuildBaseImageStage extends PullScriptStage {


    def config

    BuildBaseImageStage(project, stageName) {

        super(project, stageName)

        this.config = project.config

    }

    def run() {

        def image_name = this.config.name
        def sub_dir = this.config.subdir
        def version = "base"
        def jdk_version = this.config.jdk_version
        this.script.echo "config:${this.config}"


        this.script.node('jenkins-slave-k8s') {

            this.script.checkout this.script.scm

            this.deploy_script_dir = "release"

            // 拉取脚本
            pullScript()

            // 构建QA环境镜像
            this.script.docker.withRegistry('registry.cn-wulanchabu.aliyuncs.com', 'kkb_docker_registry_login') {
                def serverEnv = []
                serverEnv.add("DOCKER_REGISTRY_URL=registry.cn-wulanchabu.aliyuncs.com")
                serverEnv.add("DOCKER_NAME_SPACE=xiaoke")
                serverEnv.add("DOCKER_IMAGE_NAME=" + "${image_name}")
                serverEnv.add("DOCKER_TAG=" + "${version}")
                // serverEnv.add("DOCKER_FILE=" + "${this.deploy_script_dir}" + "/mos/docker_build/")

                this.script.echo "QABuildImageStage serverEnv:${serverEnv}"

                this.script.withEnv(serverEnv) {
//                    this.script.sh 'docker pull $(cat ./release/Dockerfile |grep FROM |awk  NR==1 |awk \'{print $2}\' )'
                    this.script.sh "${this.deploy_script_dir}/base.sh"
                    // 上传镜像
                    this.script.sh "release/push.sh ${version}"
                }
            }
        }
    }

}

