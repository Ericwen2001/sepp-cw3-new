/**
 *
 */

package shield;

import java.io.IOException;
import java.util.Objects;

public class SupermarketClientImp implements SupermarketClient {
    private final String endpoint;
    private String name;
    private String postCode;
    private boolean isRegistered;

    public SupermarketClientImp(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Register the business to the server. At the same time initialize the object.
     * @param name name of the business
     * @param postCode post code of the business
     * @return true if the business is successfully registered.
     */
    @Override
    public boolean registerSupermarket(String name, String postCode) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(postCode);
        String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;
        try {
            ClientIO.doGETRequest(endpoint + request);
            this.name = name;
            this.postCode = postCode;
            this.isRegistered = true;
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // **UPDATE2** ADDED METHOD

    /**
     * This method is for supermarkets to record a target order.
     * @param CHI CHI number of the shielding individual associated with this order
     * @param orderNumber the order number
     * @return true if the target order is successfully recorded.
     */
    @Override
    public boolean recordSupermarketOrder(String CHI, int orderNumber) {
        Objects.requireNonNull(CHI);
        Objects.requireNonNull(orderNumber);
        Objects.requireNonNull(name);
        Objects.requireNonNull(postCode);
        String request = "/recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber + "&supermarket_business_name=" + name + "&supermarket_postcode=" + postCode;
        try {
            String respond = ClientIO.doGETRequest(endpoint + request);

            return respond.equals("True");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // **UPDATE**

    /**
     * Update the target order to the target status. Status can only be one of "packed/dispatched/delivered".
     * @param orderNumber the order number
     * @param status status of the order for the requested number
     * @return true if the status of the order is successfully updated.
     */
    @Override
    public boolean updateOrderStatus(int orderNumber, String status) {
        Objects.requireNonNull(orderNumber);
        Objects.requireNonNull(status);
        String request = "/updateSupermarketOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;
        try {
            String respond = ClientIO.doGETRequest(endpoint + request);
            return respond.equals("True");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * @return whether the business using this client is registered.
     */
    @Override
    public boolean isRegistered() {
        return isRegistered;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPostCode() {
        return postCode;
    }

    @Override
    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }
}
