package nl.tue.bpmgame.bpmgamegui.services;

import javax.servlet.ServletContext;

import org.springframework.stereotype.Component;
import org.springframework.web.context.ServletContextAware;

@Component
public class ConstantProviderService implements ServletContextAware {
	public static final String userRegister = "/user/register";
	public static final String userPassword = "/user/password";
	public static final String userSignin = "/user/signin";
	public static final String userSignout = "/user/signout";
	public static final String userConfirm = "/user/confirm";
	public static final String userChangePassword = "/user/changepassword";
	
	public static final String groupManage = "/group/manage";
	public static final String groupCreate = "/group/create";
	public static final String groupLeave = "/group/leave";
	public static final String groupAdd = "/group/add";
	
	public static final String modelUpload = "/model/upload";
	public static final String modelLogDownload = "/model/logdownload";
	public static final String modelDownload = "/model/download";
	public static final String modelStats = "/model/stats";
	public static final String modelCases = "/model/cases";

	public static final String version = "2.0.4";

	public void setServletContext(ServletContext servletContext) {
		servletContext.setAttribute("constants", this);
	}
	
	public String getUserRegister() {
		return userRegister;
	}

	public String getUserPassword() {
		return userPassword;
	}

	public String getUserSignin() {
		return userSignin;
	}

	public String getUserSignout() {
		return userSignout;
	}
	
	public String getUserChangePassword() {
		return userChangePassword;
	}
	
	public String getGroupManage() {
		return groupManage;
	}

	public String getGroupCreate() {
		return groupCreate;
	}

	public String getGroupLeave() {
		return groupLeave;
	}

	public String getGroupAdd() {
		return groupAdd;
	}

	public String getModelUpload() {
		return modelUpload;
	}
	
	public String getModelLogDownload() {
		return modelLogDownload;
	}

	public String getModelDownload() {
		return modelDownload;
	}

	public String getModelStats() {
		return modelStats;
	}

	public String getModelCases() {
		return modelCases;
	}

	public String getVersion() {
		return version;
	}
}
