package com.kaikeba.k8s.stages

import org.yubing.delivery.Stage

/**
 *	确认消息
 */
class AccessControlStage extends Stage {
	def version
	def commitId

	def script
	def config

	AccessControlStage(project, stageName) {
		super(project, stageName)

		this.script = project.script
		this.config = project.config
	}

	def getUserId(){
		if (this.script.env.CHANGE_AUTHOR){
			return this.script.env.CHANGE_AUTHOR
		}
		return this.script.env.USER_ID
	}

	def getLatestCommit(){
		this.script.checkout this.script.scm
		def commitid = this.script.sh(returnStdout: true, script: "git rev-parse HEAD")
		return commitid
	}

	def run() {
		this.version = config.version
		this.commitId = config.gitCommitId

		this.script.node('jenkins-slave-k8s') {
			this.innerRun()
		}
	}

	def innerRun() {
		def userId = getUserId()
		def gitlabBranch = this.script.env.gitlabBranch
		def gitlabActionType = this.script.env.gitlabActionType



		// 权限白名单
		def ciWhiteList = ['devops-kkb','devops']
		def gitlabWhiteList = []
		def biWhiteList = ["kkb-mini-kkb-finance-bi-web-server","kkb-finance-bi-web-server ","kkb-prod-big-data-bi-pharaoh-daily","hky-prod-big-data-bi-user-data-flow-map","hky-prod-big-data-bi-phospherus","hky-prod-big-data-bi-web","hky-prod-big-data-bi-serviceagent", "kkb-competitor-analytics-web-server", "kkb-prod-big-data-bi-user-data-flow-map"]
		biWhiteList.each{  
			if (this.script.env.BUILD_URL.contains(it)) {
				ciWhiteList.add('hky-bi')
			}
		}
//		def platWhiteList = ["kkb-plat-www", "kkb-plat-home-app","kkb-plat-home-live","kkb-plat-cms","kkb-plat-open","kkb-monitor-nvwa",
//                        "kkb-plat-home-land","kkb-plat-fec-august","kkb-plat-fec-act","kkb-plat-fec-group","kkb-plat-fec-kaixba",
//                        "kkb-plat-fec-cybertron-ms","kkb-plat-fig-ssr"]

		def platWhiteList = ["kkb-plat-fig-ssr","kkb-prod-big-data-bi-finance-bi-web-server"]

		platWhiteList.each{  
			if (this.script.env.BUILD_URL.contains(it)) {
				ciWhiteList.add('hk-kkb')
				ciWhiteList.add('hky-bi')
			}
		}
		def whiteList = ciWhiteList + gitlabWhiteList

		// 校验用户权限
		if (whiteList.contains(userId)){
			this.script.echo "userid: ${userId},权限检验成功,准备上线"
		} else {
			this.script.echo "userid: ${userId},账号权限校验失败，停止上线"
			this.script.sh 'exit 1' 
		}

		// gitlabWhiteList中的用户触发的构建只能上线develop或master分支
		// 校验最新代码
		//if (this.commitId != getLatestCommit()){
		//	this.script.input message: '当前上线版本非最新commitid,是否确认上线？'
		//}
	}
}
