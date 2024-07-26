
import java.util.ArrayList;
import com.ib.client.*;

public class comboOrderTest extends DefaultEWrapper {

	private EReaderSignal readerSignal;
	private EClientSocket clientSocket;
	protected int currentOrderId = -1;
	
	public comboOrderTest() {
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
		comboOrderTest wrapper = new comboOrderTest();
		
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
		contract.symbol("AAPL,TSLA");
		contract.secType("BAG");
		contract.exchange("SMART");
		contract.currency("USD");

		ComboLeg leg1 = new ComboLeg();
        leg1.conid(76792991);
        leg1.ratio(1);
        leg1.action("BUY");
        leg1.exchange("SMART");
		
		ComboLeg leg2 = new ComboLeg();
        leg2.conid(265598);
        leg2.ratio(1);
        leg2.action("SELL");
        leg2.exchange("SMART");

		contract.comboLegs(new ArrayList<ComboLeg>());
		contract.comboLegs().add(leg1);
		contract.comboLegs().add(leg2);

        Order order = new Order();
        order.action("BUY");
        order.orderType("LMT");
		order.lmtPrice(80);
        order.totalQuantity(Decimal.get(10));
		order.tif("GTC");

		order.smartComboRoutingParams(new ArrayList<>());
		order.smartComboRoutingParams().add(new TagValue("NonGuaranteed", "1"));

		m_client.placeOrder(orderId , contract, order);


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