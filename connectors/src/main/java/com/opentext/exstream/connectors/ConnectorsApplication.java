package com.opentext.exstream.connectors;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.printing.PDFPageable;
import streamserve.connector.StrsConfigVals;
import streamserve.connector.StrsConnectable;
import streamserve.connector.StrsServiceable;

import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.RemoteException;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;

public class ConnectorsApplication implements StrsConnectable{
	
	
	static final String CONNECTOR_NAME = "Physical print";
	static final String PROPERTYNAME_FILEPATH = "File path";
	static final String PROPERTYNAME_REPORTKEY = "Report key";
	static final String PROPERTYNAME_PRINTERNAME = "Printer name";

	String m_filePath;
	String m_reportKey;
	String m_printerName;
	File m_outFile;
	OutputStream m_outStream;

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
	 *  The StreamServer calls this method at the end of the Process, Document or Job. 
	 *  use this method to performed the final delivery.
	 *  If the connector supports runtime properties, these are passed in the ConfigVals object. 
	 */
	@Override
	public boolean strsoClose(StrsConfigVals configVals) throws RemoteException {
		// TODO Auto-generated method stub
		try{
				readConfigVals(configVals);
				
				m_outStream.close();
				PrintService myPrintService =  findPrintService(m_printerName);
				PrinterJob job = PrinterJob.getPrinterJob();
			    File file = new File(m_filePath);
			    PDDocument doc = null;
				try {
					doc = Loader.loadPDF(file);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			    try{
			    job.setPrintService(myPrintService);
			    job.setPageable(new PDFPageable(doc));
			    PrintRequestAttributeSet patts = new HashPrintRequestAttributeSet();
			    patts.add(Sides.ONE_SIDED);
			    job.print(patts);
			    }catch (Exception e){
			    	System.out.println(e.getMessage());
			    	log(StrsServiceable.MSG_ERROR, 1, "Couldn't find printer!"+e.getMessage());
			    }	
		}catch(Exception f) {
			log(StrsServiceable.MSG_ERROR, 1, "Couldn't get into the service itself!"+f.getMessage());
		}
		
		return true;
	}
	
	/**
	 * StrsConnectable implementation
	 * 
	 *  The StreamServer calls this method when all data has been delivered by the output connector and before
	 *  the connector is removed. Use this method to release the resources used by the connector.
	 */
	@Override
	public boolean strsoEndJob() throws RemoteException {
		// TODO Auto-generated method stub
		return true;
	}
	
	/**
	 * StrsConnectable implementation
	 * 
	 * 	The StreamServer calls this method each time it starts processing output data.
	 *  can be used to initialize resources according to connector properties set in Design Center.
	 *  The properties are passed in the ConfigVals object and can be accessed with getValue method.
	*/
	@Override
	public boolean strsoOpen(StrsConfigVals configVals) throws RemoteException {
		try {
			readConfigVals(configVals);
			log(StrsServiceable.MSG_INFO, 1, "PDF started creating : Response received");
			log(StrsServiceable.MSG_INFO, 1, "PDF path : "+m_filePath);
			log(StrsServiceable.MSG_INFO, 1, "Report key : "+m_reportKey);
			log(StrsServiceable.MSG_INFO, 1, "Printer name : "+m_printerName);
			
			m_outFile = new File(m_filePath);
			if ( m_outFile.getParentFile() != null) {
				m_outFile.getParentFile().mkdirs();
			}
				m_outStream = new FileOutputStream(m_outFile.getAbsolutePath());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log(StrsServiceable.MSG_ERROR, 1, e.getLocalizedMessage());
				return false;
			}
		return true;
	}
	
	/**
	 * StrsConnectable implementation
	 * 
	 * 	The StreamServer calls this method directly after the connector has been created.
	 *  Use this method to initialize resources according to the connector properties set in Design Center.
	 *  The properties are passed in the ConfigVals object and can be accessed with getValue method.
	*/
	@Override
	public boolean strsoStartJob(StrsConfigVals configVals)
			throws RemoteException {
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
	 *  This method is called between a pair of strsoOpen() and strsoClose() calls. It can be called several times or only once,
	 *  depending on the amount of data to be written. Each strsoWrite() call provides buffered output data.
	 */
	@Override
	public boolean strsoWrite(byte[] bytes) throws RemoteException {
		try {
			m_outStream.write(bytes);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	protected void log(int msgType, int loglevel, String message)
			throws RemoteException {
		if (m_service != null) {
			m_service.writeMsg(msgType, loglevel, CONNECTOR_NAME + ": "
					+ message);
		}
	}
	
	/**
	 * Function called to print pdf
	 */
	public PrintService findPrintService(String printerName) throws RemoteException {
		PrintService result = null;
	    PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
	    for (PrintService printService : printServices) {
	    	System.out.println(printService.getName());
	        if (printService.getName().trim().equals(printerName)) {
	        	System.out.println(printService.getName());
	        	log(StrsServiceable.MSG_INFO, 1, printService.getName());
	        	result = printService;
	        }
	    }
	    System.out.println(result);
	    log(StrsServiceable.MSG_ERROR, 1, "Couldn't find printer!"+result);
	    return result;
	}
}
