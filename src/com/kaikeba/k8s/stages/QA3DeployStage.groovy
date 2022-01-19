package com.kaikeba.k8s.stages

import com.kaikeba.k8s.stages.PullScriptStage

/**
 * 部署QA环境
 */
class QA3DeployStage extends PullScriptStage {


    def config
    def backup
    QA3DeployStage(project, stageName, backup) {

        super(project, stageName)

        this.config = project.config
        this.backup = backup
    }

    def run() {
        this.script.echo "config:${this.config}"
        def nameSpace = "kkb-test3"


 
        def deploy_name = this.deployName("test")
        def sub_dir = this.config.subdir
        def version = this.config.version
        if (sub_dir ==null || sub_dir==""){
            sub_dir="."
        }
        this.script.node('jenkins-slave-k8s') {
            this.script.checkout this.script.scm

            // 拉取脚本
            pullScript()

            // 部署测试环境
            def serverEnv = []
            serverEnv.add("K8S_OPTS= --kubeconfig=/root/.kube/kkb-test/kubectl_config_test")
            serverEnv.add("K8S_NAME_SPACE=${nameSpace}")
            serverEnv.add("K8S_DEPLOY_YML=" + "${sub_dir}" + "/release/test3/k8s/deploy.yml")
            serverEnv.add("K8S_SVC_YML=" + "${sub_dir}" + "/release/test3/k8s/svc.yml")
            serverEnv.add("K8S_DEPLOY_NAME=" + "${deploy_name}")
            serverEnv.add("DOCKER_TAG=" + "test3-${version}")

            this.script.echo "QADeployStage serverEnv:${serverEnv}"

            this.script.withEnv(serverEnv) {
                if (this.backup){
                    this.script.sh "kubectl \$K8S_OPTS get deploy -n \$K8S_NAME_SPACE \$K8S_DEPLOY_NAME -o yaml>/home/jenkins/deploy_backups/\$K8S_DEPLOY_NAME.\$K8S_NAME_SPACE.\$(date +%Y%m%d_%H%M%S).yaml"
                }
                this.script.sh "${deploy_script_dir}/test3/deploy.sh"
            }
        }
    }

}

