import com.ib.client.*;

public class ImplExample {

	public static void main(String[] args) throws InterruptedException {
		EWrapperImpl wrapper = new EWrapperImpl();
		
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

        int orderId = wrapper.getCurrentOrderId();
		orderPlacement(wrapper.getClient(), orderId);


		Thread.sleep(100000);
		m_client.eDisconnect();
	}
	
	private static void orderPlacement(EClientSocket client, int orderId) {
		
		Contract contract = new Contract();
		contract.symbol("AAPL");
		contract.secType("STK");
		contract.exchange("SMART");
		contract.currency("USD");

        Order order = new Order();
        order.action("BUY");
        order.orderType("MKT");
        order.totalQuantity(Decimal.get(5));


		client.placeOrder(orderId , contract, order);
	}
}