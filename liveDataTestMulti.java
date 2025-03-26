
import java.io.IOException;  // Import the IOException class to handle errors
import java.io.FileWriter;   // Import the FileWriter class

import com.ib.client.*;

public class liveDataTestMulti extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public liveDataTestMulti() {
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
		liveDataTest wrapper = new liveDataTest();
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
		contract.secType("STK");
		contract.exchange("SMART");
		contract.currency("USD");
		
		// m_client.reqMarketDataType(4);
		
		// Request 1
		contract.symbol("AAPL");
		m_client.reqMktData(wrapper.getCurrentOrderId(), contract, "", false, false, null);
		
		// Request 2
		contract.symbol("TSLA");
		m_client.reqMktData(wrapper.getCurrentOrderId(), contract, "", false, false, null);
		
		// Request 3
		contract.symbol("F");
		m_client.reqMktData(wrapper.getCurrentOrderId(), contract, "", false, false, null);

		Thread.sleep(1000);
		m_client.eDisconnect();
	}

	@Override
	public void nextValidId(int orderId){
		currentOrderId = orderId;
	}

    //! [tickprice]
	@Override
	public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
		System.out.println("Tick Price: " + EWrapperMsgGenerator.tickPrice( tickerId, field, price, attribs));
	}
	//! [tickprice]
	
	//! [ticksize]
	@Override
	public void tickSize(int tickerId, int field, Decimal size) {
		System.out.println("Tick Size: " + EWrapperMsgGenerator.tickSize( tickerId, field, size));
	}
	//! [ticksize]
	
	//! [tickoptioncomputation]
	@Override
	public void tickOptionComputation(int tickerId, int field, int tickAttrib, double impliedVol, double delta, double optPrice,
			double pvDividend, double gamma, double vega, double theta, double undPrice) {
		// System.out.println("TickOptionComputation: " + EWrapperMsgGenerator.tickOptionComputation( tickerId, field, tickAttrib, impliedVol, delta, optPrice, pvDividend, gamma, vega, theta, undPrice));
	}
	//! [tickoptioncomputation]
	
	//! [tickgeneric]
	@Override
	public void tickGeneric(int tickerId, int tickType, double value) {
		// System.out.println("Tick Generic: " + EWrapperMsgGenerator.tickGeneric(tickerId, tickType, value));
	}
	//! [tickgeneric]
	
	//! [tickstring]
	@Override
	public void tickString(int tickerId, int tickType, String value) {
		// System.out.println("Tick String: " + EWrapperMsgGenerator.tickString(tickerId, tickType, value));
	}
	//! [tickstring]
	
	
	@Override
	public void error(int id, long errorTime, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String str = "Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg;
		if (advancedOrderRejectJson != null) {
			str += (", AdvancedOrderRejectJson: " + advancedOrderRejectJson);
		}
		System.out.println(str + "\n");
	}
	
}