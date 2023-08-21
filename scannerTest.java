import java.util.*;

import javax.swing.text.html.HTML.Tag;

import com.ib.client.*;

public class scannerTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public scannerTest() {
		readerSignal = new EJavaSignal();
		clientSocket = new EClientSocket(this, readerSignal);
	}
	
	public EClientSocket getClient() {
		return clientSocket;
	}
	
	public EReaderSignal getSignal() {
		return readerSignal;
	}
	
	public int getCurrentOrderId() {
		return currentOrderId;
	}	
	public static void main(String[] args) throws InterruptedException {
		scannerTest wrapper = new scannerTest();
		
		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		
		int port = 7497;
		m_client.eConnect("127.0.0.1", port, 1001);

		final EReader reader = new EReader(m_client, m_signal);   
		
		reader.start();
		
		new Thread(() -> {
		    while (m_client.isConnected()) {
		        m_signal.waitForSignal();
		        try {
		            reader.processMsgs();
		        } catch (Exception e) {
		            System.out.println("Exception: "+e.getMessage());
		        }
		    }
		}).start();Thread.sleep(1000);

        ScannerSubscription sub = new ScannerSubscription();
        sub.instrument("STK");
        sub.locationCode("STK.US.MAJOR");
        sub.scanCode("TOP_PERC_GAIN");
		
		TagValue t1 = new TagValue("volumeAbove", "10000");
		TagValue t2 = new TagValue("marketCapBelow1e6", "1000");
		TagValue t3 = new TagValue("priceAbove", "1");

		List<TagValue> TagValues = Arrays.asList(t1, t2, t3);

		m_client.reqScannerSubscription(1234, sub, null, TagValues);


		Thread.sleep(1000);
		m_client.eDisconnect();
	}
    
   //! [scannerdata]
   @Override
   public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
       System.out.println("ScannerData: " + EWrapperMsgGenerator.scannerData(reqId, rank, contractDetails, distance, benchmark, projection, legsStr));
   }
   //! [scannerdata]
   
   //! [scannerdataend]
   @Override
   public void scannerDataEnd(int reqId) {
       System.out.println("ScannerDataEnd: " + EWrapperMsgGenerator.scannerDataEnd(reqId));
   }
   //! [scannerdataend]
	
	@Override
	public void error(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String str = "Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg;
		if (advancedOrderRejectJson != null) {
			str += (", AdvancedOrderRejectJson: " + advancedOrderRejectJson);
		}
		System.out.println(str + "\n");
	}
   
}