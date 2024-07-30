package Orders;
import com.ib.client.*;

public class orderPriceCondition extends DefaultEWrapper {

    private EReaderSignal readerSignal;
    private EClientSocket clientSocket;
    protected int currentOrderId = -1;

    public orderPriceCondition() {
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
        orderPriceCondition wrapper = new orderPriceCondition();

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
        }).start();

        Thread.sleep(1000);
        m_client.reqIds(-1);

        Thread.sleep(1000);

    //double condPrice = 100.4;

    //! [price_condition]
        //Conditions have to be created via the OrderCondition.Create
        PriceCondition priceCondition = (PriceCondition) OrderCondition.create(OrderConditionType.Price);
        //When this contract...
        priceCondition.conId(654370386);
        //traded on this exchange
        priceCondition.exchange("SMART");
        //has a price above/below
        priceCondition.isMore(true);
        //this quantity
        priceCondition.price(100.5);
        //AND | OR next condition (will be ignored if no more conditions are added)
        priceCondition.conjunctionConnection(false);
        // priceCondition.triggerMethod(7);
    //! [price_condition]

        Contract contract = new Contract();
        contract.symbol("AAPL");
        contract.secType("STK");
        contract.exchange("SMART");
        contract.currency("USD");

        Order order = new Order();
        order.usePriceMgmtAlgo(true);
        order.outsideRth(true);
        order.action("BUY");
        order.orderType("REL");
        order.conditionsCancelOrder(false);
        order.conditionsIgnoreRth(true);


        order.lmtPrice(100.5);
        order.auxPrice(0.40);
        order.tif("GTC");
        order.totalQuantity(Decimal.get(1));
        order.conditions().add(priceCondition);
        order.transmit(true);
        m_client.placeOrder(wrapper.getCurrentOrderId(), contract, order);


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
