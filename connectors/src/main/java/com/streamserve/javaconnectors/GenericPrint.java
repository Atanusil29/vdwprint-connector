package com.streamserve.javaconnectors;

import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.Base64;
import java.util.concurrent.TimeUnit;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import streamserve.connector.StrsConfigVals;
import streamserve.connector.StrsConnectable;
import streamserve.connector.StrsServiceable;

public class GenericPrint implements StrsConnectable {

	static final String CONNECTOR_NAME = "Physical print";
	static final String PROPERTYNAME_FILEPATH = "File path";
	static final String PROPERTYNAME_REPORTKEY = "Report key";
	static final String PROPERTYNAME_PRINTERNAME = "Printer name";

	String m_filePath;
	String m_reportKey;
	String m_printerName;

	StrsServiceable m_service;

	public void readConfigVals(StrsConfigVals configVals) {
		String filePath = configVals.getValue(PROPERTYNAME_FILEPATH);
		if (filePath.length() > 0) {
			m_filePath = filePath;
		}
		String reportKey = configVals.getValue(PROPERTYNAME_REPORTKEY);
		if (reportKey.length() > 0) {
			m_reportKey = reportKey;
		}
		String printerName = configVals.getValue(PROPERTYNAME_PRINTERNAME);
		if (printerName.length() > 0) {
			m_printerName = printerName;
		}
		if (m_service == null) {
			m_service = configVals.getStrsService();
		}

	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method at the end of the Process, Document or
	 * Job. use this method to performed the final delivery. If the connector
	 * supports runtime properties, these are passed in the ConfigVals object.
	 */
	@Override
	public boolean strsoClose(StrsConfigVals configVals) throws RemoteException {
		// TODO Auto-generated method stub
		try {
			readConfigVals(configVals);

			log(StrsServiceable.MSG_INFO, 1, "#### generic-print-connector #### --- File path: " + m_filePath);
			log(StrsServiceable.MSG_INFO, 1, "#### generic-print-connector #### --- Report key: " + m_reportKey);
			log(StrsServiceable.MSG_INFO, 1, "#### generic-print-connector #### --- Printer name: " + m_printerName);

			PrintService myPrintService = findPrintService(m_printerName);
			PrinterJob job = PrinterJob.getPrinterJob();
			TimeUnit.SECONDS.sleep(4);
			File file = new File(m_filePath);
			PDDocument doc = Loader.loadPDF(file);
			job.setPrintService(myPrintService);
			job.setPageable(new PDFPageable(doc));
			//PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
			//patts.add(Sides.ONE_SIDED);
			job.print();
		} catch (IOException e) {
			e.printStackTrace();
			log(StrsServiceable.MSG_ERROR, 1,
					"#### generic-print-connector #### --- IOExcewption! " + e.getLocalizedMessage());

		} catch (PrinterException e) {
			log(StrsServiceable.MSG_ERROR, 1,
					"#### generic-print-connector #### --- Couldn't print! " + e.getMessage());
		}catch (InterruptedException e) {
			log(StrsServiceable.MSG_ERROR, 1,
					"#### generic-print-connector #### --- \file time exception print! " + e.getMessage());
		}

		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method when all data has been delivered by the
	 * output connector and before the connector is removed. Use this method to
	 * release the resources used by the connector.
	 */
	@Override
	public boolean strsoEndJob() throws RemoteException {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method each time it starts processing output
	 * data. can be used to initialize resources according to connector properties
	 * set in Design Center. The properties are passed in the ConfigVals object and
	 * can be accessed with getValue method.
	 */
	@Override
	public boolean strsoOpen(StrsConfigVals configVals) throws RemoteException {
		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * The StreamServer calls this method directly after the connector has been
	 * created. Use this method to initialize resources according to the connector
	 * properties set in Design Center. The properties are passed in the ConfigVals
	 * object and can be accessed with getValue method.
	 */
	@Override
	public boolean strsoStartJob(StrsConfigVals configVals) throws RemoteException {
		try {
			readConfigVals(configVals);
		} catch (Exception e) {
			log(StrsServiceable.MSG_ERROR, 1, e.getLocalizedMessage());
			return false;
		}
		return true;
	}

	/**
	 * StrsConnectable implementation
	 * 
	 * This method is called between a pair of strsoOpen() and strsoClose() calls.
	 * It can be called several times or only once, depending on the amount of data
	 * to be written. Each strsoWrite() call provides buffered output data.
	 */
	@Override
	public boolean strsoWrite(byte[] bytes) throws RemoteException {
		return true;
	}

	protected void log(int msgType, int loglevel, String message) throws RemoteException {
		if (m_service != null) {
			m_service.writeMsg(msgType, loglevel, CONNECTOR_NAME + ": " + message);
		}
	}

	/**
	 * Function called to print pdf
	 */
	public PrintService findPrintService(String printerName) throws RemoteException {
		PrintService result = null;
		PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
		for (PrintService printService : printServices) {
			log(StrsServiceable.MSG_INFO, 1,
					"#### generic-print-connector #### --- before if loop : Print service name: "
							+ printService.getName());
			// System.out.println(printService.getName());
			if (printService.getName().trim().equals(printerName)) {
				// System.out.println(printService.getName());
				log(StrsServiceable.MSG_INFO, 1,
						"#### generic-print-connector #### --- Print service name: " + printService.getName());
				result = printService;
				break;
			}
		}
		// System.out.println(result);
		// log(StrsServiceable.MSG_ERROR, 1, "#### generic-print-connector #### ---
		// Couldn't find printer!" + result);
		return result;
	}

}
