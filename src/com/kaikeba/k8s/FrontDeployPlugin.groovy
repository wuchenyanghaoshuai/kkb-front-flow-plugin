package com.kaikeba.k8s

import org.yubing.delivery.Project
import org.yubing.delivery.plugin.Plugin
import com.kaikeba.k8s.stages.BuildBaseImageStage
import com.kaikeba.k8s.stages.BuildImageStage
import com.kaikeba.k8s.stages.DevDeployStage
import com.kaikeba.k8s.stages.QADeployStage
import com.kaikeba.k8s.stages.QA2DeployStage
import com.kaikeba.k8s.stages.QA3DeployStage
import com.kaikeba.k8s.stages.QA4DeployStage
import com.kaikeba.k8s.stages.QA5DeployStage
import com.kaikeba.k8s.stages.PreDeployStage
import com.kaikeba.k8s.stages.ProdDeployStage
import com.kaikeba.k8s.stages.AccessControlStage
import com.kaikeba.k8s.stages.ConfirmMessgerStage
import com.kaikeba.k8s.stages.AutoMergeStage
import com.kaikeba.k8s.stages.ConfirmDeployOKStage
import com.kaikeba.k8s.stages.UndoDeployStage
import com.kaikeba.k8s.stages.UploadImageStage
import com.kaikeba.k8s.stages.MasterCheckStage

class FrontDeployPlugin implements Plugin<Project> {

    def wholeFlow(project) {
        project.pipeline("WHOLE_FLOW", [
            // DeployDev
            // "构建dev镜像",
            // "上传dev镜像",
            // "确认部署Dev",
            // "部署Dev环境",
            "BuildDevImage",
            "DeployDev",
            "BuildTestImage",
            //"UploadTestImage",
            "DeployQA",
            // DeployPreProd
            //"BuildPreImage",
            //"UploadPreImage",
            // DeployProd
            //"AccessControl",
            //"ConfirmDeployProd",
            //"BuildProdImage",
            //"UploadProdImage",
            //"DeployProd",
            //"ConfirmDeployOK",
            //"UndoDeployStage",
            //"AutoMergeStage"
        ])
    }

    def deployDev1Flow(project) {
        project.pipeline("DEPLOY_Dev", [
            // 构建基镜像
            "构建基础镜像",
            // DeployDev
//            "MasterCheck",
//            "BuildDevImage",
//            "UploadDevImage",
            "确认部署Dev",
//            "DeployDev"
        ])
    }
    def deployDevFlow(project) {
        project.pipeline("DEPLOY_DEV", [
            "BuildDevImage",
            "DeployDev"
        ])
    }

    def deployQAFlow(project) {
        project.pipeline("DEPLOY_QA", [
            "BuildTestImage",
            "DeployQA"
        ])
    }
    def gitPushFlow(project) {
        project.pipeline("GIT_PUSH", [
            // 构建基镜像
            "构建基础镜像",
            // DeployDev
            "构建dev镜像",
            "上传dev镜像",
            "确认部署Dev",
            "部署Dev环境",
            // DeployQA
            "BuildTestImage",
            "UploadTestImage",
            "ConfirmDeployQA",
            "DeployQA"
        ])
    }

    def pushFlow(project) {
        def gitpushflow = project.pipeline("GIT_PUSH")
        if (project.env.gitlabTargetBranch == "develop") {
	         project.pipeline("PUSH", gitpushflow)
        }
    }

    def deployQA2Flow(project) {
        project.pipeline("DEPLOY_QA2", [
            "BuildTestImage2",
            "DeployQA2"
        ])
    }

    def deployQA3Flow(project) {
        project.pipeline("DEPLOY_QA3", [
            "BuildTestImage3",
            "DeployQA3"
        ])
    }
    def deployQA4Flow(project) {
        project.pipeline("DEPLOY_QA4", [
            "BuildTestImage4",
            "DeployQA4"
        ])
    }
    def deployQA5Flow(project) {
        project.pipeline("DEPLOY_QA5", [
            "BuildTestImage5",
            "DeployQA5"
        ])
    }

    def deployPreProdFlow(project) {
        project.pipeline("DEPLOY_PreProd", [
            // 构建基镜像
            "构建基础镜像",
            // DeployDev
            "构建dev镜像",
            "上传dev镜像",
            "确认部署Dev",
            "部署Dev环境",
            // DeployQA
            "构建test镜像",
            "上传test镜像",
            "确认部署QA",
            "部署QA环境",
            // DeployPreProd
            "BuildPreImage",
//            "UploadPreImage",
            "ConfirmDeployPreProd",
            "DeployPreProd",
            "ConfirmPreDeployOK",
            "UndoPreDeployStage"
        ])
    }

    def deployProdFlow(project) {
        project.pipeline("DEPLOY_PROD", [
            // 构建基镜像
            "构建基础镜像",
            // DeployDev
            "构建dev镜像",
            "上传dev镜像",
            "确认部署Dev",
            "部署Dev环境",
            // DeployQA
            "构建test镜像",
            "上传test镜像",
            "确认部署QA",
            "部署QA环境",
            // DeployPreProd
            "构建pre镜像",
            "上传pre镜像",
            "确认部署预生产",
            "部署预生产环境",
            // DeployProd
            "MasterCheck",
            "BuildProdImage",
            "ConfirmDeployProd",
            "AccessControl",
            "DeployProd",
            "ConfirmDeployOK",
            "UndoDeployStage",
            "AutoMergeStage"
        ])
    }

    def rebaseFlow(project) {
        project.pipeline("REBASE", [
            // Build
            "BuildBaseImage"
        ])
    }

    def registerFlows(project) {
        this.wholeFlow(project);
        this.deployDevFlow(project);
        this.deployDev1Flow(project);
        this.deployQAFlow(project);
        this.deployQA2Flow(project);
        this.deployQA3Flow(project);
        this.deployQA4Flow(project);
        this.deployQA5Flow(project);
        this.deployPreProdFlow(project);
        this.deployProdFlow(project);
        this.rebaseFlow(project);
        this.gitPushFlow(project);
        this.pushFlow(project);
    }

    def registerStages(project) {
        // Build
        project.stage("BuildBaseImage", new BuildBaseImageStage(project,'构建基础镜像'))
        project.stage("BuildDevImage", new BuildImageStage(project,'构建dev镜像',"dev"))
        project.stage("BuildTestImage", new BuildImageStage(project,'构建test镜像',"test"))
        project.stage("BuildTestImage2", new BuildImageStage(project,'构建test2镜像',"test2"))
        project.stage("BuildTestImage3", new BuildImageStage(project,'构建test3镜像',"test3"))
        project.stage("BuildTestImage4", new BuildImageStage(project,'构建test4镜像',"test4"))
        project.stage("BuildTestImage5", new BuildImageStage(project,'构建test5镜像',"test5"))
        project.stage("BuildPreImage", new BuildImageStage(project,'构建pre镜像',"pre"))
        project.stage("BuildProdImage", new BuildImageStage(project,'构建prod镜像',"prod"))
        project.stage("UploadDevImage", new UploadImageStage(project,'上传dev镜像',"dev"))
        project.stage("UploadTestImage", new UploadImageStage(project,'上传test镜像',"test"))
        project.stage("UploadTestImage2", new UploadImageStage(project,'上传test镜像',"test2"))
        project.stage("UploadPreImage", new UploadImageStage(project,'上传pre镜像',"pre"))
        project.stage("UploadProdImage", new UploadImageStage(project,'上传prod镜像',"prod"))
        // Deploy QA
        project.stage("ConfirmDeployDev", new ConfirmMessgerStage(project, '确认部署Dev',"部署分支${project.params.gitlabSourceBranch}到Dev环境?"))
        project.stage("DeployDev", new DevDeployStage(project, '部署Dev环境', false))
        // Deploy QA
        project.stage("ConfirmDeployQA", new ConfirmMessgerStage(project, '确认部署QA',"部署分支${project.params.gitlabSourceBranch}到QA环境?"))
        project.stage("DeployQA", new QADeployStage(project, '部署QA环境', false))
        project.stage("DeployQA2", new QA2DeployStage(project, '部署QA2环境', false))
        project.stage("DeployQA3", new QA3DeployStage(project, '部署QA3环境', false))
        project.stage("DeployQA4", new QA4DeployStage(project, '部署QA4环境', false))
        project.stage("DeployQA5", new QA5DeployStage(project, '部署QA5环境', false))

        // DeployToPreProd
        project.stage("ConfirmDeployPreProd", new ConfirmMessgerStage(project, '确认部署预生产',"部署分支${project.params.gitlabSourceBranch}到预生产环境?"))
        project.stage("DeployPreProd", new PreDeployStage(project, '部署预生产', false))
        // DeployToProd
        project.stage("AccessControl", new AccessControlStage(project, '生产环境权限校验'))
        project.stage("ConfirmDeployProd", new ConfirmMessgerStage(project, '确认部署生产',"部署分支${project.params.gitlabSourceBranch}到生产环境?"))
        project.stage("DeployProd", new ProdDeployStage(project, '部署生产环境', true))
        // Test & Merge
        project.stage("ConfirmSmokeTestOK", new ConfirmMessgerStage(project, '冒烟测试',"分支${project.params.gitlabSourceBranch}冒烟测试通过?"))
        project.stage("AutoMergeStage",new AutoMergeStage(project, '合并分支'))
        // ok or undo is a question
        project.stage("ConfirmDeployOK", new ConfirmDeployOKStage(project, '上线结果',"prod"))
        project.stage("ConfirmPreDeployOK", new ConfirmDeployOKStage(project, '预发结果',"pre"))
        project.stage("UndoDeployStage",new UndoDeployStage(project, '回滚版本', 'prod'))
        project.stage("UndoPreDeployStage",new UndoDeployStage(project, '回滚版本', 'pre'))
        // check master branch containers online branch
        project.stage("MasterCheck", new MasterCheckStage(project,'检测上线分支',"prod"))
    }

    def apply(Project project) {
        project.log "apply flow plugin"

        this.registerFlows(project);
        this.registerStages(project);

        project.log "apply flow plugin ok!"
    }
}
