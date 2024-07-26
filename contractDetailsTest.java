
import com.ib.client.*;

public class contractDetailsTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public contractDetailsTest() {
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
		contractDetailsTest wrapper = new contractDetailsTest();
		
		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		
		int port = 7496;

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

		// FIGI Contract
		// Contract contract = new Contract();
		// contract.secId("BBG000B9XRY4");
		// contract.secType("FIGI");
		// contract.exchange("SMART");

		// // Stock Contract
		// Contract contract = new Contract();
		// contract.symbol("AAPL");
		// contract.secType("STK");
		// contract.exchange("SMART");
		// contract.currency("USD");
		// contract.primaryExch("ISLAND");

		// Futures Contract
		Contract contract = new Contract();
		contract.symbol("SGB");
		contract.secType("FUT");
		contract.exchange("SGX");
		contract.currency("JPY");
		contract.lastTradeDateOrContractMonth("202312");

		// Options Contract
		// Contract contract = new Contract();
		// contract.symbol("SPX");
		// contract.secType("OPT");
		// contract.exchange("SMART");
		// contract.currency("USD");
		// contract.lastTradeDateOrContractMonth("202303");
		// contract.strike(3840);
		// contract.right("P");
		// contract.tradingClass("SPX");

		m_client.reqContractDetails(1234, contract);


		Thread.sleep(1000);
		m_client.eDisconnect();
	}

	@Override
	public void nextValidId(int orderId){
		currentOrderId = orderId;
	}
   
	//! [contractdetails]
	@Override
	public void contractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println(EWrapperMsgGenerator.contractDetails(reqId, contractDetails)); 
	}
	//! [contractdetails]
	@Override
	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
		System.out.println(EWrapperMsgGenerator.bondContractDetails(reqId, contractDetails)); 
	}
	//! [contractdetailsend]
	@Override
	public void contractDetailsEnd(int reqId) {
		System.out.println("Contract Details End: " + EWrapperMsgGenerator.contractDetailsEnd(reqId));
	}
	//! [contractdetailsend]

	@Override
	public void error(int id, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String str = "Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg;
		if (advancedOrderRejectJson != null) {
			str += (", AdvancedOrderRejectJson: " + advancedOrderRejectJson);
		}
		System.out.println(str + "\n");
	}
}