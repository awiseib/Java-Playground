package Orders;

import com.ib.client.*;

public class bracketOrderTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	public int currentOrderId = -1;
	
	public bracketOrderTest() {
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
		bracketOrderTest wrapper = new bracketOrderTest();
		
		final EClientSocket m_client = wrapper.getClient();
		final EReaderSignal m_signal = wrapper.getSignal();
		
		int port = 7496;
        
		m_client.eConnect("127.0.0.1", port, 2);
		//! [connect]
		//! [ereader]
		final EReader reader = new EReader(m_client, m_signal);   
		
		reader.start();
		//An additional thread is created in this program design to empty the messaging queue
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

			int orderId = wrapper.getCurrentOrderId();

		Contract contract = new Contract();
		contract.symbol("AAPL");
		contract.secType("STK");
		contract.exchange("SMART");
		contract.currency("USD");

        Order parent = new Order();
		parent.orderId(wrapper.getCurrentOrderId());
        parent.action("BUY");
        parent.orderType("LMT");
		parent.lmtPrice(147);
        parent.totalQuantity(Decimal.get(10));
		parent.transmit(false);

        Order profitTaker = new Order();
		profitTaker.parentId(parent.orderId());
		profitTaker.orderId(parent.orderId());
        profitTaker.action("SELL");
        profitTaker.orderType("LMT");
		profitTaker.lmtPrice(137);
        profitTaker.totalQuantity(Decimal.get(10));
		profitTaker.transmit(false);

        Order stopLoss = new Order();
		stopLoss.parentId(orderId);
		stopLoss.orderId(parent.orderId());
        stopLoss.action("SELL");
        stopLoss.orderType("STP");
		stopLoss.auxPrice(155);
        stopLoss.totalQuantity(Decimal.get(10));
		profitTaker.transmit(true);

        m_client.placeOrder(parent.orderId() , contract, parent);
        m_client.placeOrder(profitTaker.orderId(), contract, profitTaker);
        m_client.placeOrder(stopLoss.orderId(), contract, stopLoss);

		Thread.sleep(100000);
		m_client.eDisconnect();
	}

	@Override
	public void nextValidId(int orderId){
		currentOrderId = orderId;
	}
	
	@Override
	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
		System.out.println(EWrapperMsgGenerator.openOrder(orderId, contract, order, orderState));
	}
	
	@Override
	public void openOrderEnd() {
		System.out.println("Open Order End: " + EWrapperMsgGenerator.openOrderEnd());
	}
	
	@Override
	public void orderStatus(int orderId, String status, Decimal filled, Decimal remaining, double avgFillPrice, int permId, int parentId,
			double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
		System.out.println(EWrapperMsgGenerator.orderStatus( orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld, mktCapPrice));
	}
	
	@Override
	public void execDetails(int reqId, Contract contract, Execution execution) {
		System.out.println(EWrapperMsgGenerator.execDetails( reqId, contract, execution));
	}
	
	@Override
	public void execDetailsEnd(int reqId) {
		System.out.println("Exec Details End: " + EWrapperMsgGenerator.execDetailsEnd( reqId));
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