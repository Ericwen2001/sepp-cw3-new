/**
 *
 */

package shield;

import java.io.IOException;
import java.util.Objects;

public class CateringCompanyClientImp implements CateringCompanyClient {
    private final String endpoint;
    private String name;
    private String postCode;
    private boolean isRegistered;

    public CateringCompanyClientImp(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Register the business to the server. At the same time initialize the object.
     * @param name name of the business
     * @param postCode post code of the business
     * @return true if the business is successfully registered.
     */
    @Override
    public boolean registerCateringCompany(String name, String postCode) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(postCode);
        String request = "/registerCateringCompany?business_name=" + name + "&postcode=" + postCode;
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
        String request = "/updateOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;
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
    public String getPostCode() {
        return postCode;
    }
}
