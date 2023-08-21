import com.ib.client.*;

public class historicDataTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public historicDataTest() {
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
		historicDataTest wrapper = new historicDataTest();
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

		Contract contract = new Contract();
		contract.symbol("AAPL");
		contract.secType("STK");
		contract.exchange("SMART");
		contract.currency("USD");

		m_client.reqHistoricalData(1234, contract, "", "1 D", "1 hour", "Trades", 0, 1, false, null);


		Thread.sleep(1000);
		m_client.eDisconnect();
	}

	
   @Override
   public void historicalData(int reqId, Bar bar) {
		System.out.println("HistoricalData:  " + EWrapperMsgGenerator.historicalData(reqId, bar.time(), bar.open(), bar.high(), bar.low(), bar.close(), bar.volume(), bar.count(), bar.wap()));
    
   }
   
   @Override
   public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {
       System.out.println("HistoricalDataEnd. " + EWrapperMsgGenerator.historicalDataEnd(reqId, startDateStr, endDateStr));
   }
}