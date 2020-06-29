package com.example.demo.controller;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;
import org.springframework.stereotype.Component;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Component
public class ReportUtil {

	
	public static byte[]  gerarRelatorio(List<?> listaDados, String relatorio, ServletContext servletContext)
			throws Exception {

		JRBeanCollectionDataSource jrbcds = new JRBeanCollectionDataSource(listaDados);
		String caminhoJasper = servletContext.getRealPath("relatorios") + File.separator + relatorio + ".jasper";
		JasperPrint impressoraJasper = JasperFillManager.fillReport(caminhoJasper, new HashMap<>(), jrbcds);
		return JasperExportManager.exportReportToPdf(impressoraJasper);
	
	}
}
