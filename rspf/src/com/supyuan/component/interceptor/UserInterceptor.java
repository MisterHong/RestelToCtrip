package com.supyuan.component.interceptor;

import com.jfinal.aop.Interceptor;
import com.jfinal.aop.Invocation;
import com.jfinal.core.Controller;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.supyuan.component.base.BaseProjectController;
import com.supyuan.component.util.JFlyFoxUtils;
import com.supyuan.component.util.Md5Utils;
import com.supyuan.jfinal.component.util.Attr;
import com.supyuan.system.auth.SysAuth;
import com.supyuan.system.menu.SysMenu;
import com.supyuan.system.user.SysUser;
import com.supyuan.util.StrUtils;
import org.apache.commons.lang.StringUtils;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.List;

/**
 * 用户认证拦截器
 *
 * @author yaosir
 */
public class UserInterceptor implements Interceptor {

    private static final Log log = Log.getLog(UserInterceptor.class);

	@SuppressWarnings({ "rawtypes"})
	public void intercept(Invocation ai) {

        Controller controller = ai.getController();

        HttpServletRequest request = controller.getRequest();
        String referrer = request.getHeader("referer");
        String site = "http://" + request.getServerName();

        StringBuffer paramsSb = new StringBuffer("");
        Enumeration enu = request.getParameterNames();
        if (null != enu) 
        {
            while (enu.hasMoreElements()) {
                String paraName = (String) enu.nextElement();
                String paraVal = request.getParameter(paraName);
                paramsSb.append(paraName + ":" + paraVal + ", ");
            }
        }

        String ipFromNginx = request.getHeader("X-Real-IP");
        log.warn("RemoteAddr" + request.getRemoteAddr());
        log.debug("####IP:" + ipFromNginx + "\t port:" + request.getRemotePort() + "\t 请求路径:"
                + request.getRequestURI() + ", 参数:{" + paramsSb.toString() + "}");

        log.warn("ipFromNginx" + ipFromNginx);

        if (referrer == null || !referrer.startsWith(site)) {
            log.warn("####非法的请求");
        }

        String tmpPath = ai.getActionKey();

        if (tmpPath.startsWith("/")) {
            tmpPath = tmpPath.substring(1, tmpPath.length());
        }
        if (tmpPath.endsWith("/")) {
            tmpPath = tmpPath.substring(0, tmpPath.length() - 1);
        }
        
        // 每次访问获取session，没有可以从cookie取
        SysUser user = null;

        if (controller instanceof BaseProjectController) {
            user = (SysUser) ((BaseProjectController) controller).getSessionUser();
        } else {
            user = controller.getSessionAttr(Attr.SESSION_NAME);
        }

        if (!(request.getRequestURI().contains("/order")) && JFlyFoxUtils.isBack(tmpPath)) {

            //修复未登录情况时，直接前往某个路由报错问题
            if ((user == null || user.getUserid() <= 0) && (tmpPath.indexOf("/") > -1 || "home".equals(tmpPath))) {
                controller.redirect("/logout");
                return;
            }

            if (user == null || user.getUserid() <= 0) {
                controller.redirect("/trans");
                return;
            }
            // 这里展示控制第三方用户和前端用户不能登录后台
            int usertype = user.getInt("usertype");
            if (usertype == 4 // 第三方用户
                    || usertype == 3) { // 前端用户
                controller.redirect("/trans/auth");
                return;
            }

            // 判断url是否有权限
            if (!(request.getRequestURI().contains("/order")) && !urlAuth(controller, tmpPath)) {
                controller.redirect("/trans/auth");
                return;
            }

			/*String pathInfo = request.getServletPath();
            if(!StringUtils.isNotBlank(pathInfo)) {
				pathInfo = request.getRequestURI();
			}*/
        }
        // 判断功能操作是否有权限
        if (!(request.getRequestURI().contains("/order")) && !uriAuth(user, tmpPath, controller)) {

            String script = "history.back();";
            controller.setAttr("script", script);
            controller.render("/pages/template/message.html");
            return;
        }
        ai.invoke();
    }

    @SuppressWarnings("static-access")
    /**
     * 特殊请求登录
     * @return user
     */
	protected SysUser SpecialLogin() {
    	// 初始化数据字典Map
		String username = "admin";
		@SuppressWarnings("unused")
		String password = "123456";
		SysUser user_temp = SysUser.dao.findFirstByWhere(" where username = ? " //
				+ " and usertype in ( " + JFlyFoxUtils.USER_TYPE_ADMIN + "," + JFlyFoxUtils.USER_TYPE_NORMAL + ")",
				username);
		@SuppressWarnings("unused")
		String md5Password = "";
		try {
			String userPassword = user_temp.get("password");
			String decryptPassword = JFlyFoxUtils.passwordDecrypt(userPassword);
			//System.out.println(">> " + decryptPassword);
			md5Password = new Md5Utils().getMD5(decryptPassword).toLowerCase();
		} catch (Exception e) {
			log.error("认证异常", e);
			return null;
		}
		return user_temp;
	}
    
    /**
     * 输入流转字符串
     * @param is
     * @return
     * @throws IOException
     */
    public String inputStream2String(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		int i = -1;
		while ((i = is.read()) != -1) {
			baos.write(i);
		}
		return baos.toString();
	}
    
    /**
     * 判断Url是否有权限
     * <p>
     * 2016年12月18日 上午12:12:51
     *
     * @param controller
     * @param tmpPath
     * @return
     */
    protected boolean urlAuth(Controller controller, String tmpPath) {
        List<SysMenu> list = controller.getSessionAttr("nomenu");
        // nomenuList 应该是size等于0，而不是空
        if (list == null) {
            return false;
        }

        for (SysMenu sysMenu : list) {
            String url = sysMenu.getStr("url");
            if (StrUtils.isEmpty(url)) {
                continue;
            }
            if (tmpPath.startsWith(url)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 权限验证
     *
     * @param user
     * @param tmpPath
     * @return
     */
    protected boolean uriAuth(SysUser user, String tmpPath, Controller controller) {
        SysAuth userAuth = SysAuth.dao.findFirstByWhere("WHERE webapp_id = ? AND uri = ?", 99, tmpPath);
        boolean has = false;
        if (null != userAuth && !"T".equalsIgnoreCase(userAuth.getStr("is_halt"))) {
            Integer userId = 1;
            Integer webappId = 99;
            if (null != user) {
                userId = user.getUserID();
            }

            String sql = "SELECT t.* FROM sys_auth_user t WHERE t.webapp_id = ?  AND t.user_id = ?";
            Record rd = Db.findFirst(sql, webappId, userId);

            if (null != rd) {
                String cacheString = rd.getStr("cache_string");
                if (userId == 1 && "*".equals(cacheString)) {
                    has = true;
                } else if (StringUtils.isNotBlank(cacheString)) {
                    cacheString = ",".concat(cacheString).concat(",");
                    if (cacheString.indexOf(",".concat(Long.toString(userAuth.getInt("id")).concat(","))) != -1) {
                        has = true;
                    }
                }
            }
        } else {
            has = true;
        }
        if (!has) {
            controller.setAttr("msg", "[" + userAuth.getStr("descript") + "]" + "权限不足!");
            return false;
        }

        return true;
    }
}
