/**
 *
 */

package shield;

import java.io.IOException;

public class SupermarketClientImp implements SupermarketClient {
  private String endpoint;
  private String name;
  private String postCode;
  private boolean isRegistered;
  public SupermarketClientImp(String endpoint) {
  }

  @Override
  public boolean registerSupermarket(String name, String postCode) {
    String request = "/registerSupermarket?business_name=" + name + "&postcode=" + postCode;
    try {
      ClientIO.doGETRequest(endpoint+request);
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
  @Override
  public boolean recordSupermarketOrder(String CHI, int orderNumber) {
    String request = "recordSupermarketOrder?individual_id=" + CHI + "&order_number=" + orderNumber + "&supermarket_business_name=" + name + "&supermarket_postcode=" + postCode;
    try {
      String respond = ClientIO.doGETRequest(endpoint+request);
      if(respond.equals("True")){
        return true;
      }
      else{
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

  // **UPDATE**
  @Override
  public boolean updateOrderStatus(int orderNumber, String status) {
    String request = "/updateSupermarketOrderStatus?order_id=" + orderNumber + "&newStatus=" + status;
    try {
      String respond = ClientIO.doGETRequest(endpoint+request);
      if(respond.equals("True")){
        return true;
      }
      else{
        return false;
      }
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }
  }

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
