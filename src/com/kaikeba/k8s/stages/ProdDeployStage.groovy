package com.kaikeba.k8s.stages

import com.kaikeba.k8s.stages.PullScriptStage

/**
 * 部署Prod环境
 */
class ProdDeployStage extends PullScriptStage {

    def config
    def backup
    ProdDeployStage(project, stageName, backup) {
        super(project, stageName)
        this.config = project.config
        this.backup = backup
    }

    def run() {
        this.script.echo "config:${this.config}"
		def nameSpace = "kkb-xk-prod"

        def category = this.config.category
        def map = this.config.k8s_namespaces

        if (map) {

            def tempNameSpace = map['prod']

            if (tempNameSpace) {

                nameSpace = tempNameSpace

            } else if (category) {

                nameSpace = "kkb-${category}-prod"
            }

        } else if (category) {

            nameSpace = "kkb-${category}-prod"
        }


        def deploy_name = this.deployName("prod")
        def sub_dir = this.config.subdir
        def version = this.config.version
        if (sub_dir ==null || sub_dir==""){
            sub_dir="."
        }
        this.script.node('jenkins-slave-k8s') {
            this.script.checkout this.script.scm

            // 拉取脚本
            pullScript()

            // 部署prod环境
            def serverEnv = []
            serverEnv.add("K8S_OPTS= --kubeconfig=/root/.kube/kkb-prod/kubectl_config_prod ")
            serverEnv.add("K8S_NAME_SPACE=${nameSpace}")
            serverEnv.add("K8S_DEPLOY_YML=" + "${sub_dir}" + "/release/prod/k8s/deploy.yml")
            serverEnv.add("K8S_SVC_YML=" + "${sub_dir}" + "/release/prod/k8s/svc.yml")
            serverEnv.add("K8S_DEPLOY_NAME=" + "${deploy_name}")
            serverEnv.add("DOCKER_TAG=" + "prod-${version}")

            this.script.echo "ProdDeployStage serverEnv:${serverEnv}"

            this.script.withEnv(serverEnv) {
              try {
                if (this.backup){
                    this.script.sh "kubectl \$K8S_OPTS get deploy -n \$K8S_NAME_SPACE \$K8S_DEPLOY_NAME -o yaml>/data/app/jenkins/deploy_backups/\$K8S_DEPLOY_NAME.\$K8S_NAME_SPACE.\$(date +%Y%m%d_%H%M%S).yaml"
                }
              } catch (err) {
                    this.script.echo "${deploy_name} not found maybe first blood"
                }
                this.script.sh "${deploy_script_dir}/prod/rollout.sh"
            }
        }
    }

}

