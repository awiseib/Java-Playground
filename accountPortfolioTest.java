
import com.ib.client.*;

public class accountPortfolioTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public accountPortfolioTest() {
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
		accountPortfolioTest wrapper = new accountPortfolioTest();
		
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

		m_client.reqAccountUpdates(true, "");

		Thread.sleep(1000);
		m_client.eDisconnect();
	}
   
	@Override
	public void nextValidId(int orderId){
		currentOrderId = orderId;
	}

	//! [updateaccountvalue]
	@Override
	public void updateAccountValue(String key, String value, String currency, String accountName) {
		System.out.println(EWrapperMsgGenerator.updateAccountValue( key, value, currency, accountName));
	}
	//! [updateaccountvalue]
	
	//! [updateportfolio]
	@Override
	public void updatePortfolio(Contract contract, Decimal position, double marketPrice, double marketValue, double averageCost,
			double unrealizedPNL, double realizedPNL, String accountName) {
		System.out.println(EWrapperMsgGenerator.updatePortfolio( contract, position, marketPrice, marketValue, averageCost, unrealizedPNL, realizedPNL, accountName));
	}
	//! [updateportfolio]
	
	//! [updateaccounttime]
	@Override
	public void updateAccountTime(String timeStamp) {
		System.out.println(EWrapperMsgGenerator.updateAccountTime( timeStamp));
	}
	//! [updateaccounttime]
	
	//! [accountdownloadend]
	@Override
	public void accountDownloadEnd(String accountName) {
		System.out.println(EWrapperMsgGenerator.accountDownloadEnd(accountName));
	}
	//! [accountdownloadend]
	
	@Override
	public void error(int id, long errorTime, int errorCode, String errorMsg, String advancedOrderRejectJson) {
		String str = "Error. Id: " + id + ", Code: " + errorCode + ", Msg: " + errorMsg;
		if (advancedOrderRejectJson != null) {
			str += (", AdvancedOrderRejectJson: " + advancedOrderRejectJson);
		}
		System.out.println(str + "\n");
	}
    
}