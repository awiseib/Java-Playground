
import java.io.IOException;
// import java.nio.file.Files;
// import java.nio.file.Path;
import java.io.File;
import java.util.Scanner;

import com.ib.client.*;

public class replaceFaTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public replaceFaTest() {
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
		replaceFaTest wrapper = new replaceFaTest();
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

		try {
			// Path path = Path.of("D:\\Code\\Java Playground\\faData_Groups.xml");
			// String groups_xml = Files.readString(path);

			File fa_data_file = new File("D:\\Code\\Java Playground\\faData_Groups.xml");
			Scanner fa_data_reader = new Scanner(fa_data_file);
			String fa_data = fa_data_reader.nextLine();
			fa_data_reader.close();

			m_client.replaceFA(1234, 1, fa_data);
		}
		catch (IOException e) {
			System.out.println("Exception");;
		}

		Thread.sleep(1000);
		m_client.eDisconnect();
	}

	@Override
	public void replaceFAEnd(int reqId, String text) {
		System.out.println("ReplaceFa End: " + text + "\n");
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
	
}