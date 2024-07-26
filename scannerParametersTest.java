
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;   // Import the FileWriter class

import com.ib.client.*;

public class scannerParametersTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public scannerParametersTest() {
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
		return currentOrderId+=1;
	}	

	public static void main(String[] args) throws InterruptedException {
		scannerParametersTest wrapper = new scannerParametersTest();
		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		
		int port = 7496;
        
		m_client.eConnect("127.0.0.1", port, 2);
		
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

		m_client.reqScannerParameters();

		Thread.sleep(100000);
		m_client.eDisconnect();
	}

	@Override
	public void nextValidId(int orderId){
		currentOrderId = orderId;
	}
	
	@Override
	public void error(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String str = "Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg;
		if (advancedOrderRejectJson != null) {
			str += (", AdvancedOrderRejectJson: " + advancedOrderRejectJson);
		}
		System.out.println(str + "\n");
	}
	
   @Override
   public void scannerParameters(String xml) {
       System.out.println("OVERRIDE SUCCESS!?!?");

		try {
			FileWriter myWriter = new FileWriter("scannerParameters.txt");
			myWriter.write(xml);
			myWriter.close();
			System.out.println("Successfully wrote to the file.");
		} catch (IOException e) {
			System.out.println("An error occurred.");
			e.printStackTrace();
		}
   }
}