package com.kaikeba.k8s.stages

import com.kaikeba.k8s.stages.PullScriptStage

/**
 * 部署Pre环境
 */
class PreDeployStage extends PullScriptStage {


    def config
    def backup
    PreDeployStage(project, stageName, backup) {

        super(project, stageName)

        this.config = project.config
        this.backup = backup
    }

    def run() {
        this.script.echo "config:${this.config}"
        def nameSpace = "kkb-xk-pre"

        def category = this.config.category
        def map = this.config.k8s_namespaces

        if (map) {

            def tempNameSpace = map['pre']

            if (tempNameSpace) {

                nameSpace = tempNameSpace

            } else if (category) {

                nameSpace = "kkb-${category}-pre"
            }

        } else if (category) {

            nameSpace = "kkb-${category}-pre"
        }

        def deploy_name = this.deployName("pre")
        def sub_dir = this.config.subdir
        def version = this.config.version
        if (sub_dir ==null || sub_dir==""){
            sub_dir="."
        }
        this.script.node('jenkins-slave-k8s') {
            this.script.checkout this.script.scm

            // 拉取脚本
            pullScript()

            // 部署pre环境
            def serverEnv = []
            serverEnv.add("K8S_OPTS= --kubeconfig=/root/.kube/kkb-pre/kubectl_config_pre ")
            serverEnv.add("K8S_NAME_SPACE=${nameSpace}")
            serverEnv.add("K8S_DEPLOY_YML=" + "${sub_dir}" + "/release/pre/k8s/deploy.yml")
            serverEnv.add("K8S_SVC_YML=" + "${sub_dir}" + "/release/pre/k8s/svc.yml")
            serverEnv.add("K8S_DEPLOY_NAME=" + "${deploy_name}")
            serverEnv.add("DOCKER_TAG=" + "pre-${version}")

            this.script.echo "PreDeployStage serverEnv:${serverEnv}"

            this.script.withEnv(serverEnv) {
                if (this.backup){
                    this.script.sh "kubectl \$K8S_OPTS get deploy -n \$K8S_NAME_SPACE \$K8S_DEPLOY_NAME -o yaml>/home/jenkins/deploy_backups/\$K8S_DEPLOY_NAME.\$K8S_NAME_SPACE.\$(date +%Y%m%d_%H%M%S).yaml"
                }
                this.script.sh "${deploy_script_dir}/pre/deploy.sh"
            }
        }
    }

}

