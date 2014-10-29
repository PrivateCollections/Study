package org.fenixsoft.neonat.ui;

import javax.servlet.Servlet;

import org.eclipse.equinox.http.helper.BundleEntryHttpContext;
import org.eclipse.equinox.jsp.jasper.JspServlet;
import org.fenixsoft.neonat.useradmin.UserAdminAuthenticationHttpContext;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.util.tracker.ServiceTracker;

/**
 * ע��Servlet��JSP�;�̬��Դ
 * 
 * @author IcyFenix
 */
public class HttpServiceTracker extends ServiceTracker<HttpService, HttpService> {

	private BundleContext context;

	public HttpServiceTracker(BundleContext context) {
		super(context, HttpService.class, null);
		this.context = context;
	}

	@Override
	public HttpService addingService(ServiceReference<HttpService> reference) {
		HttpService hs = context.getService(reference);
		// ע��/WebContentĿ¼ΪWeb��Ŀ¼���������Է�������ľ�̬��Դ
		HttpContext hc = new BundleEntryHttpContext(context.getBundle(), "/WebContent");
		// ע��/WebContent�����*.jsp�ļ�������JSPҳ��ſ���
		Servlet jspServlet = new JspServlet(context.getBundle(), "/WebContent");
		try {
			hs.registerResources("/", "/", hc);
			//hs.registerServlet("/*.jsp", jspServlet, null, null);
			hs.registerServlet(
					"/*.jsp",
					jspServlet,
					null,
					new UserAdminAuthenticationHttpContext(hs
							.createDefaultHttpContext()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.addingService(reference);
	}
}