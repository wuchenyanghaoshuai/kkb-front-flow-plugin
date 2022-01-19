import com.kaikeba.k8s.FrontDeployPlugin


/**
 * 插件ID
 * 
 * @return
 */
def id() {
	return "kkb_k8s_front_deploy"
}

/**
 * 插件描述
 * 
 * @return
 */
def desc() {
	return "开课吧滚动更新插件，基于 http://git.kaikeba.cn/smallcourse/delivery.git 框架"
}

/**
 * 插件实例
 * 
 * @return
 */
def instance() {
	return new FrontDeployPlugin()
}