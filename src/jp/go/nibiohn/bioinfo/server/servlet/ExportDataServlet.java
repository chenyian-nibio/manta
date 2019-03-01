package jp.go.nibiohn.bioinfo.server.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class ExportDataServlet extends HttpServlet {

	@SuppressWarnings("unused")
	private String encoding = "utf-8";

	public ExportDataServlet() {
		super();
	}

	/**
	 * @see HttpServlet#init()
	 */
	@Override
	public void init() throws ServletException {
		super.init();
	}

	/**
	 * see HttpServlet#init(ServletConfig)
	 */
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String initParameterEncoding = config.getInitParameter("encoding");
		if (initParameterEncoding != null) {
			setEncoding(initParameterEncoding);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		export(request, response);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		export(request, response);
	}

	private void export(HttpServletRequest request, HttpServletResponse response) throws IOException {
//		String fileName = request.getParameter("fileName");
//		response.setContentType("text/plain");
//		response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
//		GutFloraServiceImpl service = new GutFloraServiceImpl();
//
//		String content = "";
//		response.setContentLength(content.length());
//		IOUtils.write(content, response.getOutputStream(), encoding);
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
}
