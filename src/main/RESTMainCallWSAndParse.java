package main;

import generated.NewDataSet;
import generated.NewDataSet.Holidays;

import java.io.StringReader;
import java.util.List;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * Calls the RESTful web service at REST_URL defined below and process the XML.
 * 
 * Processing of the schema and generating the holiday classes is done per the 
 * video and before running this code.
 * 
 */
public class RESTMainCallWSAndParse {
	public static final String REST_URL = "http://www.holidaywebservice.com/Holidays/HolidayService.asmx/GetHolidaysForYear?countryCode=US&year=2018";
	public static final int OK_STATUS = Response.Status.OK.getStatusCode();

	/**
	 * Call the web service and display the response.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		// call the service and get the response object
		Response response = ClientBuilder.newClient()
				.target(REST_URL)
				.request(MediaType.APPLICATION_XML)
				.get();

		// process the response object
		StatusType status = response.getStatusInfo();
		int statusCode = status.getStatusCode();
		if (statusCode == OK_STATUS) {
			List<Holidays> holidays = parseXML(response.readEntity(String.class));
			printHolidays(holidays);
		} else {
			System.out.printf("Service returned status: \"%d %s\"\n", statusCode, status.getReasonPhrase());
		}

	}

	/**
	 * Parse the XML string.
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static List<Holidays> parseXML(String xml) throws Exception {
		// Get an unmarshaller.
		JAXBContext jc;
		jc = JAXBContext.newInstance("generated");
		Unmarshaller u = jc.createUnmarshaller();

		// Build a DOM.
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new InputSource(new StringReader(xml)));

		// Traverse the DOM until 'NewDataSet' is reached.
		Element subtree = doc.getDocumentElement();
		Node node = subtree.getElementsByTagName("NewDataSet")
				.item(0);

		// Unmarshal 'NewDataSet'.
		JAXBElement<NewDataSet> dataSet = u.unmarshal(node, NewDataSet.class);

		// Return the holidays.
		return dataSet.getValue()
				.getHolidays();
	}
	
	/**
	 * Print the holidays.
	 * @param holidays
	 */
	public static void printHolidays(List<Holidays> holidays){
				for (Holidays h : holidays) {
					System.out.printf("%30s: %d/%d/%d\n", h.getName(), h.getDate()
							.getMonth(), h.getDate()
							.getDay(), h.getDate()
							.getYear());
				}
	}

}
